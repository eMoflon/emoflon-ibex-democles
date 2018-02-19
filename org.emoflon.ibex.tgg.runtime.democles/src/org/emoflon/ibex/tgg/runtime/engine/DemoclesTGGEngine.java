package org.emoflon.ibex.tgg.runtime.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emoflon.ibex.common.operational.IMatchObserver;
import org.emoflon.ibex.gt.democles.runtime.DemoclesGTEngine;
import org.emoflon.ibex.tgg.compiler.BlackPatternCompiler;
import org.emoflon.ibex.tgg.compiler.patterns.common.IBlackPattern;
import org.emoflon.ibex.tgg.operational.IBlackInterpreter;
import org.emoflon.ibex.tgg.operational.defaults.IbexOptions;
import org.emoflon.ibex.tgg.operational.matches.IMatch;
import org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.TGGAttributeConstraintAdornmentStrategy;
import org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.TGGAttributeConstraintModule;
import org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.TGGAttributeConstraintTypeModule;
import org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.TGGConstraintComponentBuilder;
import org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.TGGNativeOperationBuilder;
import org.gervarro.democles.common.DataFrame;
import org.gervarro.democles.common.IDataFrame;
import org.gervarro.democles.common.PatternMatcherPlugin;
import org.gervarro.democles.common.runtime.CategoryBasedQueueFactory;
import org.gervarro.democles.common.runtime.ListOperationBuilder;
import org.gervarro.democles.common.runtime.Task;
import org.gervarro.democles.common.runtime.VariableRuntime;
import org.gervarro.democles.constraint.CoreConstraintModule;
import org.gervarro.democles.constraint.emf.EMFConstraintModule;
import org.gervarro.democles.event.MatchEvent;
import org.gervarro.democles.incremental.emf.ModelDeltaCategorizer;
import org.gervarro.democles.incremental.emf.NotificationProcessor;
import org.gervarro.democles.interpreter.incremental.rete.RetePattern;
import org.gervarro.democles.interpreter.incremental.rete.RetePatternBody;
import org.gervarro.democles.interpreter.incremental.rete.RetePatternMatcherModule;
import org.gervarro.democles.notification.emf.BidirectionalReferenceFilter;
import org.gervarro.democles.notification.emf.EdgeDeltaFeeder;
import org.gervarro.democles.notification.emf.ReferenceToEdgeConverter;
import org.gervarro.democles.notification.emf.UndirectedEdgeToDirectedEdgeConverter;
import org.gervarro.democles.operation.RelationalOperationBuilder;
import org.gervarro.democles.operation.emf.DefaultEMFBatchAdornmentStrategy;
import org.gervarro.democles.operation.emf.DefaultEMFIncrementalAdornmentStrategy;
import org.gervarro.democles.operation.emf.EMFBatchOperationBuilder;
import org.gervarro.democles.operation.emf.EMFIdentifierProviderBuilder;
import org.gervarro.democles.operation.emf.EMFInterpretableIncrementalOperationBuilder;
import org.gervarro.democles.plan.incremental.builder.AdornedNativeOperationDrivenComponentBuilder;
import org.gervarro.democles.plan.incremental.builder.FilterComponentBuilder;
import org.gervarro.democles.plan.incremental.leaf.ReteSearchPlanAlgorithm;
import org.gervarro.democles.runtime.AdornedNativeOperationBuilder;
import org.gervarro.democles.runtime.InterpretableAdornedOperation;
import org.gervarro.democles.runtime.JavaIdentifierProvider;
import org.gervarro.democles.specification.emf.EMFDemoclesPatternMetamodelPlugin;
import org.gervarro.democles.specification.emf.EMFPatternBuilder;
import org.gervarro.democles.specification.emf.Pattern;
import org.gervarro.democles.specification.emf.TypeModule;
import org.gervarro.democles.specification.emf.constraint.EMFTypeModule;
import org.gervarro.democles.specification.emf.constraint.PatternInvocationTypeModule;
import org.gervarro.democles.specification.emf.constraint.RelationalTypeModule;
import org.gervarro.democles.specification.impl.DefaultPattern;
import org.gervarro.democles.specification.impl.DefaultPatternBody;
import org.gervarro.democles.specification.impl.DefaultPatternFactory;
import org.gervarro.democles.specification.impl.PatternInvocationConstraintModule;
import org.gervarro.notification.model.ModelDelta;

import IBeXLanguage.IBeXPatternSet;

/**
 * Engine for (bidirectional) graph transformations with Democles.
 */
public class DemoclesTGGEngine extends DemoclesGTEngine implements IBlackInterpreter {
	private Registry registry;
	private Collection<Pattern> patterns;
	private HashMap<IDataFrame, Collection<IMatch>> matches;
	private RetePatternMatcherModule retePatternMatcherModule;
	private EMFPatternBuilder<DefaultPattern, DefaultPatternBody> patternBuilder;
	private Collection<RetePattern> patternMatchers;
	protected IMatchObserver app;

	private IbexOptions options;
	private NotificationProcessor observer;

	@Override
	public void initialise(Registry registry, IMatchObserver app) {
		this.registry = registry;
		patterns = new ArrayList<>();
		matches = new HashMap<>();
		patternMatchers = new ArrayList<>();
		this.app = app;
	}

	public void setOptions(IbexOptions options) {
		this.options = options;
		createAndRegisterPatterns();
	}

	@Override
	public void monitor(ResourceSet rs) {
		if (options.debug()) {
			saveDemoclesPatterns(rs);
			printReteNetwork();
		}

		observer.install(rs);
	}

	private void createAndRegisterPatterns() {
		// Create EMF-based pattern specification
		initPatterns(null);

		// Democles configuration
		final EMFInterpretableIncrementalOperationBuilder<VariableRuntime> emfNativeOperationModule = configureDemocles();

		// Build the pattern matchers in 2 phases
		// 1) EMF-based to EMF-independent transformation
		final Collection<DefaultPattern> internalPatterns = patternBuilder.build(patterns);

		// 2) EMF-independent to pattern matcher runtime (i.e., Rete network)
		// transformation
		retePatternMatcherModule.build(internalPatterns.toArray(new DefaultPattern[internalPatterns.size()]));
		retePatternMatcherModule.getSession().setAutoCommitMode(false);

		// Attach match listener to pattern matchers
		retrievePatternMatchers();
		patternMatchers.forEach(pm -> pm.addEventListener(this));

		// Install model event listeners on the resource set
		EdgeDeltaFeeder edgeDeltaFeeder = new EdgeDeltaFeeder(emfNativeOperationModule);
		UndirectedEdgeToDirectedEdgeConverter undirectedEdgeToDirectedEdgeConverter = new UndirectedEdgeToDirectedEdgeConverter(
				edgeDeltaFeeder);
		ReferenceToEdgeConverter referenceToEdgeConverter = new ReferenceToEdgeConverter(
				undirectedEdgeToDirectedEdgeConverter);
		BidirectionalReferenceFilter bidirectionalReferenceFilter = new BidirectionalReferenceFilter(
				referenceToEdgeConverter);
		observer = new NotificationProcessor(bidirectionalReferenceFilter,
				new CategoryBasedQueueFactory<ModelDelta>(ModelDeltaCategorizer.INSTANCE));
	}

	private void printReteNetwork() {
		for (final RetePattern retePattern : retePatternMatcherModule.getPatterns()) {
			final List<RetePatternBody> bodies = retePattern.getBodies();
			for (int i = 0; i < bodies.size(); i++) {
				final RetePatternBody body = bodies.get(i);
				System.out.println(body.getHeader().toString() + " @ " + i + ": " + body.getRuntime().toString());
			}
		}
	}

	private void saveDemoclesPatterns(ResourceSet rs) {
		Resource r = rs
				.createResource(URI.createPlatformResourceURI(options.projectPath() + "/debug/patterns.xmi", true));
		r.getContents().addAll(patterns.stream().sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
				.collect(Collectors.toList()));
		try {
			r.save(null);
			rs.getResources().remove(r);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initPatterns(final IBeXPatternSet ibexPatternSet) {
		BlackPatternCompiler compiler = new BlackPatternCompiler(options);
		compiler.preparePatterns();

		IBlackToDemoclesPatternTransformation transformation = new IBlackToDemoclesPatternTransformation(this.options);
		for (String r : compiler.getRuleToPatternMap().keySet()) {
			for (IBlackPattern pattern : compiler.getRuleToPatternMap().get(r)) {
				if (IBlackToDemoclesPatternTransformation.patternIsNotEmpty(pattern)
						&& app.isPatternRelevantForCompiler(pattern.getName())) {
					transformation.ibexToDemocles(pattern);
				}
			}
		}
		this.patterns = transformation.getPatterns();
	}

	private void retrievePatternMatchers() {
		for (Pattern pattern : patterns) {
			if (app.isPatternRelevantForCompiler(pattern.getName())) {
				patternMatchers.add(retePatternMatcherModule.getPatternMatcher(getPatternID(pattern)));
			}
		}
	}

	private String getPatternID(Pattern pattern) {
		return PatternMatcherPlugin.getIdentifier(pattern.getName(), pattern.getSymbolicParameters().size());
	}

	private EMFInterpretableIncrementalOperationBuilder<VariableRuntime> configureDemocles() {
		final EMFConstraintModule emfTypeModule = new EMFConstraintModule(registry);
		final EMFTypeModule internalEMFTypeModule = new EMFTypeModule(emfTypeModule);
		final RelationalTypeModule internalRelationalTypeModule = new RelationalTypeModule(
				CoreConstraintModule.INSTANCE);

		patternBuilder = new EMFPatternBuilder<DefaultPattern, DefaultPatternBody>(new DefaultPatternFactory());
		final PatternInvocationConstraintModule<DefaultPattern, DefaultPatternBody> patternInvocationTypeModule = new PatternInvocationConstraintModule<DefaultPattern, DefaultPatternBody>(
				patternBuilder);
		final PatternInvocationTypeModule<DefaultPattern, DefaultPatternBody> internalPatternInvocationTypeModule = new PatternInvocationTypeModule<DefaultPattern, DefaultPatternBody>(
				patternInvocationTypeModule);
		patternBuilder.addConstraintTypeSwitch(internalPatternInvocationTypeModule.getConstraintTypeSwitch());
		patternBuilder.addConstraintTypeSwitch(internalRelationalTypeModule.getConstraintTypeSwitch());
		patternBuilder.addConstraintTypeSwitch(internalEMFTypeModule.getConstraintTypeSwitch());
		patternBuilder.addVariableTypeSwitch(internalEMFTypeModule.getVariableTypeSwitch());

		retePatternMatcherModule = new RetePatternMatcherModule();

		retePatternMatcherModule.setTaskQueueFactory(
				new CategoryBasedQueueFactory<Task>(org.gervarro.democles.runtime.IncrementalTaskCategorizer.INSTANCE));

		// EMF native
		// NativeOperation
		final EMFInterpretableIncrementalOperationBuilder<VariableRuntime> emfNativeOperationModule = new EMFInterpretableIncrementalOperationBuilder<VariableRuntime>(
				retePatternMatcherModule, emfTypeModule);
		// EMF batch
		final EMFBatchOperationBuilder<VariableRuntime> emfBatchOperationModule = new EMFBatchOperationBuilder<VariableRuntime>(
				emfNativeOperationModule, DefaultEMFBatchAdornmentStrategy.INSTANCE);
		final EMFIdentifierProviderBuilder<VariableRuntime> emfIdentifierProviderModule = new EMFIdentifierProviderBuilder<VariableRuntime>(
				JavaIdentifierProvider.INSTANCE);
		// Relational
		final ListOperationBuilder<InterpretableAdornedOperation, VariableRuntime> relationalOperationModule = new ListOperationBuilder<InterpretableAdornedOperation, VariableRuntime>(
				new RelationalOperationBuilder<VariableRuntime>());

		final ReteSearchPlanAlgorithm algorithm = new ReteSearchPlanAlgorithm();
		// EMF incremental
		final AdornedNativeOperationBuilder<VariableRuntime> emfIncrementalOperationModule = new AdornedNativeOperationBuilder<VariableRuntime>(
				emfNativeOperationModule, DefaultEMFIncrementalAdornmentStrategy.INSTANCE);
		// EMF component
		algorithm.addComponentBuilder(
				new AdornedNativeOperationDrivenComponentBuilder<VariableRuntime>(emfIncrementalOperationModule));
		// Relational component
		algorithm.addComponentBuilder(new FilterComponentBuilder<VariableRuntime>(relationalOperationModule));

		retePatternMatcherModule.setSearchPlanAlgorithm(algorithm);
		retePatternMatcherModule.addOperationBuilder(emfBatchOperationModule);
		retePatternMatcherModule.addOperationBuilder(relationalOperationModule);

		retePatternMatcherModule.addIdentifierProviderBuilder(emfIdentifierProviderModule);

		// TGG attribute constraints
		handleTGGAttributeConstraints(algorithm);

		return emfNativeOperationModule;
	}

	private void handleTGGAttributeConstraints(ReteSearchPlanAlgorithm algorithm) {
		if (!this.options.blackInterpSupportsAttrConstrs()) {
			return;
		}

		TGGAttributeConstraintAdornmentStrategy.INSTANCE.setIsModelGen(options.isModelGen());

		// Handle constraints for the EMF to Java transformation
		TGGAttributeConstraintModule.INSTANCE.registerConstraintTypes(options.constraintProvider());
		TypeModule<TGGAttributeConstraintModule> tggAttributeConstraintTypeModule = new TGGAttributeConstraintTypeModule(
				TGGAttributeConstraintModule.INSTANCE);
		patternBuilder.addConstraintTypeSwitch(tggAttributeConstraintTypeModule.getConstraintTypeSwitch());

		// Native operation
		final TGGNativeOperationBuilder<VariableRuntime> tggNativeOperationModule = new TGGNativeOperationBuilder<VariableRuntime>(
				options.constraintProvider());
		// Batch operations
		final EMFBatchOperationBuilder<VariableRuntime> tggBatchOperationModule = new EMFBatchOperationBuilder<VariableRuntime>(
				tggNativeOperationModule, TGGAttributeConstraintAdornmentStrategy.INSTANCE);
		retePatternMatcherModule.addOperationBuilder(tggBatchOperationModule);

		// Incremental operation
		algorithm.addComponentBuilder(new TGGConstraintComponentBuilder<VariableRuntime>(tggNativeOperationModule));
	}

	public void updateMatches() {
		// Trigger the Rete network
		retePatternMatcherModule.performIncrementalUpdates();
	}

	public void terminate() {
		if (patternMatchers != null)
			patternMatchers.forEach(pm -> pm.removeEventListener(this));
	}

	public void handleEvent(final MatchEvent event) {
		// React to events
		final String type = event.getEventType();
		final DataFrame frame = event.getMatching();

		Optional<Pattern> p = patterns.stream()
				.filter(pattern -> getPatternID(pattern).equals(event.getSource().toString())).findAny();

		p.ifPresent(pattern -> {
			// React to create
			if (type.contentEquals(MatchEvent.INSERT) && (!matches.keySet().contains(frame)
					|| matches.get(frame).stream().allMatch(m -> !m.getPatternName().equals(pattern.getName())))) {
				IMatch match = new DemoclesMatch(frame, pattern);
				if (matches.keySet().contains(frame)) {
					matches.get(frame).add(match);
				} else {
					matches.put(frame, new ArrayList<IMatch>(Arrays.asList(match)));
				}
				app.addMatch(match);
			}

			// React to delete
			if (type.equals(MatchEvent.DELETE)) {
				Collection<IMatch> matchList = matches.get(frame);
				Optional<IMatch> match = matchList == null ? Optional.empty()
						: matchList.stream().filter(m -> m.getPatternName().equals(pattern.getName())).findAny();

				match.ifPresent(m -> {
					app.removeMatch(m);
					if (matches.get(frame).size() > 1) {
						matches.get(frame).remove(m);
					} else {
						matches.remove(frame);
					}
				});
			}
		});
	}

	@Override
	public ResourceSet createAndPrepareResourceSet(final String workspacePath) {
		ResourceSet rs = createDefaultResourceSet();
		try {
			EMFDemoclesPatternMetamodelPlugin.setWorkspaceRootDirectory(rs, new File(workspacePath).getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rs;
	}

	private ResourceSet createDefaultResourceSet() {
		final ResourceSet resourceSet = new ResourceSetImpl();
		// In contrast to EMFDemoclesPatternMetamodelPlugin.createDefaultResourceSet, we
		// do not delegate directly to the global registry!
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		return resourceSet;
	}
}
