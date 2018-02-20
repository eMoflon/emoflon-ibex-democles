package org.emoflon.ibex.gt.democles.runtime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emoflon.ibex.common.operational.IMatch;
import org.emoflon.ibex.common.operational.IMatchObserver;
import org.emoflon.ibex.common.operational.IPatternInterpreter;
import org.emoflon.ibex.common.utils.ModelPersistenceUtils;
import org.gervarro.democles.common.DataFrame;
import org.gervarro.democles.common.IDataFrame;
import org.gervarro.democles.common.PatternMatcherPlugin;
import org.gervarro.democles.common.runtime.CategoryBasedQueueFactory;
import org.gervarro.democles.common.runtime.ListOperationBuilder;
import org.gervarro.democles.common.runtime.OperationBuilder;
import org.gervarro.democles.common.runtime.Task;
import org.gervarro.democles.common.runtime.VariableRuntime;
import org.gervarro.democles.constraint.CoreConstraintModule;
import org.gervarro.democles.constraint.emf.EMFConstraintModule;
import org.gervarro.democles.event.MatchEvent;
import org.gervarro.democles.event.MatchEventListener;
import org.gervarro.democles.incremental.emf.ModelDeltaCategorizer;
import org.gervarro.democles.incremental.emf.NotificationProcessor;
import org.gervarro.democles.interpreter.incremental.rete.RetePattern;
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
import org.gervarro.democles.plan.incremental.leaf.Component;
import org.gervarro.democles.plan.incremental.leaf.ReteSearchPlanAlgorithm;
import org.gervarro.democles.runtime.AdornedNativeOperationBuilder;
import org.gervarro.democles.runtime.InterpretableAdornedOperation;
import org.gervarro.democles.runtime.JavaIdentifierProvider;
import org.gervarro.democles.specification.emf.EMFDemoclesPatternMetamodelPlugin;
import org.gervarro.democles.specification.emf.EMFPatternBuilder;
import org.gervarro.democles.specification.emf.Pattern;
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
 * Engine for (unidirectional) graph transformations with Democles.
 */
public class DemoclesGTEngine implements IPatternInterpreter, MatchEventListener {
	/**
	 * The registry.
	 */
	protected Registry registry;

	/**
	 * The match observer.
	 */
	protected IMatchObserver app;

	/**
	 * The matches.
	 */
	protected HashMap<IDataFrame, Collection<IMatch>> matches;

	/**
	 * The Democles patterns.
	 */
	protected Collection<Pattern> patterns = new ArrayList<Pattern>();

	/**
	 * Democles pattern builder.
	 */
	protected EMFPatternBuilder<DefaultPattern, DefaultPatternBody> patternBuilder;

	/**
	 * The pattern matchers.
	 */
	protected Collection<RetePattern> patternMatchers;

	/**
	 * The pattern matcher module.
	 */
	protected RetePatternMatcherModule retePatternMatcherModule;

	/**
	 * The observer (??).
	 */
	protected NotificationProcessor observer;

	/**
	 * The path for debugging output.
	 */
	protected Optional<String> debugPath = Optional.empty();

	/**
	 * Creates a new DemoclesGTEngine.
	 */
	public DemoclesGTEngine() {
		this.patterns = new ArrayList<>();
		this.patternMatchers = new ArrayList<>();
		this.matches = new HashMap<IDataFrame, Collection<IMatch>>();
	}

	@Override
	public void initPatterns(final IBeXPatternSet ibexPatternSet) {
		IBeXToDemoclesPatternTransformation transformation = new IBeXToDemoclesPatternTransformation();
		patterns = transformation.transform(ibexPatternSet);
		this.savePatternsForDebugging();
		this.createAndRegisterPatterns();
	}

	/**
	 * Saves the Democles patterns for debugging.
	 */
	private void savePatternsForDebugging() {
		debugPath.ifPresent(path -> {
			List<Pattern> sortedPatterns = patterns.stream() //
					.sorted((p1, p2) -> p1.getName().compareTo(p2.getName())) // alphabetically by name
					.collect(Collectors.toList());
			ModelPersistenceUtils.saveModel(sortedPatterns, path + "/democles-patterns");
		});
	}

	protected void createAndRegisterPatterns() {
		// Democles configuration
		EMFInterpretableIncrementalOperationBuilder<VariableRuntime> emfNativeOperationModule = configureDemocles();

		// Build the pattern matchers in 2 phases
		// 1) EMF-based to EMF-independent transformation
		Collection<DefaultPattern> internalPatterns = patternBuilder.build(patterns);

		// 2) EMF-independent to pattern matcher runtime (i.e., Rete network)
		// transformation
		retePatternMatcherModule.build(internalPatterns.toArray(new DefaultPattern[internalPatterns.size()]));
		retePatternMatcherModule.getSession().setAutoCommitMode(false);

		// Attach match listener to pattern matchers
		patterns.forEach(pattern -> {
			if (app.isPatternRelevantForCompiler(pattern.getName())) {
				this.patternMatchers.add(retePatternMatcherModule.getPatternMatcher(getPatternID(pattern)));
			}
		});
		patternMatchers.forEach(pm -> pm.addEventListener(this));

		// Install model event listeners on the resource set
		EdgeDeltaFeeder edgeDeltaFeeder = new EdgeDeltaFeeder(emfNativeOperationModule);
		UndirectedEdgeToDirectedEdgeConverter undirectedEdgeToDirectedEdgeConverter = new UndirectedEdgeToDirectedEdgeConverter(
				edgeDeltaFeeder);
		ReferenceToEdgeConverter referenceToEdgeConverter = new ReferenceToEdgeConverter(
				undirectedEdgeToDirectedEdgeConverter);
		BidirectionalReferenceFilter bidirectionalReferenceFilter = new BidirectionalReferenceFilter(
				referenceToEdgeConverter);
		this.observer = new NotificationProcessor(bidirectionalReferenceFilter,
				new CategoryBasedQueueFactory<ModelDelta>(ModelDeltaCategorizer.INSTANCE));
	}

	private EMFInterpretableIncrementalOperationBuilder<VariableRuntime> configureDemocles() {
		EMFConstraintModule emfTypeModule = new EMFConstraintModule(registry);
		EMFTypeModule internalEMFTypeModule = new EMFTypeModule(emfTypeModule);
		RelationalTypeModule internalRelationalTypeModule = new RelationalTypeModule(CoreConstraintModule.INSTANCE);

		patternBuilder = new EMFPatternBuilder<DefaultPattern, DefaultPatternBody>(new DefaultPatternFactory());
		PatternInvocationConstraintModule<DefaultPattern, DefaultPatternBody> patternInvocationTypeModule //
				= new PatternInvocationConstraintModule<DefaultPattern, DefaultPatternBody>(patternBuilder);
		PatternInvocationTypeModule<DefaultPattern, DefaultPatternBody> internalPatternInvocationTypeModule //
				= new PatternInvocationTypeModule<DefaultPattern, DefaultPatternBody>(patternInvocationTypeModule);
		patternBuilder.addConstraintTypeSwitch(internalPatternInvocationTypeModule.getConstraintTypeSwitch());
		patternBuilder.addConstraintTypeSwitch(internalRelationalTypeModule.getConstraintTypeSwitch());
		patternBuilder.addConstraintTypeSwitch(internalEMFTypeModule.getConstraintTypeSwitch());
		patternBuilder.addVariableTypeSwitch(internalEMFTypeModule.getVariableTypeSwitch());

		retePatternMatcherModule = new RetePatternMatcherModule();
		retePatternMatcherModule.setTaskQueueFactory(
				new CategoryBasedQueueFactory<Task>(org.gervarro.democles.runtime.IncrementalTaskCategorizer.INSTANCE));

		// EMF NativeOperation
		EMFInterpretableIncrementalOperationBuilder<VariableRuntime> emfNativeOperationModule //
				= new EMFInterpretableIncrementalOperationBuilder<VariableRuntime>(retePatternMatcherModule,
						emfTypeModule);
		// EMF batch
		EMFBatchOperationBuilder<VariableRuntime> emfBatchOperationModule //
				= new EMFBatchOperationBuilder<VariableRuntime>(emfNativeOperationModule,
						DefaultEMFBatchAdornmentStrategy.INSTANCE);
		EMFIdentifierProviderBuilder<VariableRuntime> emfIdentifierProviderModule //
				= new EMFIdentifierProviderBuilder<VariableRuntime>(JavaIdentifierProvider.INSTANCE);
		// Relational
		ListOperationBuilder<InterpretableAdornedOperation, VariableRuntime> relationalOperationModule //
				= new ListOperationBuilder<InterpretableAdornedOperation, VariableRuntime>(
						new RelationalOperationBuilder<VariableRuntime>());
		// EMF incremental
		AdornedNativeOperationBuilder<VariableRuntime> emfIncrementalOperationModule //
				= new AdornedNativeOperationBuilder<VariableRuntime>(emfNativeOperationModule,
						DefaultEMFIncrementalAdornmentStrategy.INSTANCE);

		ReteSearchPlanAlgorithm algorithm = this.initReteSearchPlanAlgorithm(Arrays.asList(
				// EMF component
				new AdornedNativeOperationDrivenComponentBuilder<VariableRuntime>(emfIncrementalOperationModule),
				// Relational component
				new FilterComponentBuilder<VariableRuntime>(relationalOperationModule)));
		retePatternMatcherModule.setSearchPlanAlgorithm(algorithm);
		retePatternMatcherModule.addOperationBuilder(emfBatchOperationModule);
		retePatternMatcherModule.addOperationBuilder(relationalOperationModule);
		retePatternMatcherModule.addIdentifierProviderBuilder(emfIdentifierProviderModule);

		return emfNativeOperationModule;
	}

	protected ReteSearchPlanAlgorithm initReteSearchPlanAlgorithm(
			Collection<OperationBuilder<Component, Component, VariableRuntime>> builders) {
		ReteSearchPlanAlgorithm algorithm = new ReteSearchPlanAlgorithm();
		builders.forEach(builder -> algorithm.addComponentBuilder(builder));
		return algorithm;
	}

	@Override
	public void initialise(Registry registry, IMatchObserver matchObserver) {
		this.registry = registry;
		this.app = matchObserver;
	}

	@Override
	public ResourceSet createAndPrepareResourceSet(final String workspacePath) {
		ResourceSet resourceSet = new ResourceSetImpl();
		// In contrast to EMFDemoclesPatternMetamodelPlugin.createDefaultResourceSet, we
		// do not delegate directly to the global registry!
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

		try {
			EMFDemoclesPatternMetamodelPlugin.setWorkspaceRootDirectory(resourceSet,
					new File(workspacePath).getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resourceSet;
	}

	@Override
	public void monitor(final ResourceSet resourceSet) {
		observer.install(resourceSet);
	}

	@Override
	public void updateMatches() {
		// Trigger the Rete network.
		retePatternMatcherModule.performIncrementalUpdates();
	}

	@Override
	public void terminate() {
		patternMatchers.forEach(pm -> pm.removeEventListener(this));
	}

	@Override
	public void setDebugPath(final String debugPath) {
		this.debugPath = Optional.of(debugPath);
	}

	/**
	 * Handles the {@link MatchEvent} from Democles.
	 * 
	 * @param event
	 *            the MatchEvent to handle
	 */
	@Override
	public void handleEvent(final MatchEvent event) {
		String type = event.getEventType();
		DataFrame frame = event.getMatching();
		Optional<Pattern> p = patterns.stream()
				.filter(pattern -> getPatternID(pattern).equals(event.getSource().toString())).findAny();
		p.ifPresent(pattern -> {
			if (type.contentEquals(MatchEvent.INSERT) && (!matches.keySet().contains(frame)
					|| matches.get(frame).stream().allMatch(m -> !m.getPatternName().equals(pattern.getName())))) {
				IMatch match = this.createMatch(frame, pattern);
				if (matches.keySet().contains(frame)) {
					matches.get(frame).add(match);
				} else {
					matches.put(frame, new ArrayList<IMatch>(Arrays.asList(match)));
				}
				app.addMatch(match);
			}
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

	protected IMatch createMatch(final DataFrame frame, final Pattern pattern) {
		return new DemoclesGTMatch(frame, pattern);
	}

	/**
	 * Returns the pattern identifier for the given pattern.
	 * 
	 * @param pattern
	 *            the Democles pattern
	 * @return the identifier of the Democles pattern
	 */
	protected static String getPatternID(final Pattern pattern) {
		return PatternMatcherPlugin.getIdentifier(pattern.getName(), pattern.getSymbolicParameters().size());
	}
}
