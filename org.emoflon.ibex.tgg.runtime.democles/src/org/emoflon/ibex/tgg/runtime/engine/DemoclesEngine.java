package org.emoflon.ibex.tgg.runtime.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emoflon.ibex.tgg.compiler.TGGCompiler;
import org.emoflon.ibex.tgg.compiler.patterns.IbexPatternOptimiser;
import org.emoflon.ibex.tgg.compiler.patterns.PatternSuffixes;
import org.emoflon.ibex.tgg.compiler.patterns.common.IbexPattern;
import org.emoflon.ibex.tgg.compiler.patterns.common.RulePartPattern;
import org.emoflon.ibex.tgg.compiler.patterns.translation_app_conds.CheckTranslationStatePattern;
import org.emoflon.ibex.tgg.operational.OperationalStrategy;
import org.emoflon.ibex.tgg.operational.PatternMatchingEngine;
import org.emoflon.ibex.tgg.operational.util.IMatch;
import org.emoflon.ibex.tgg.operational.util.IbexOptions;
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
import org.gervarro.democles.event.MatchEventListener;
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
import org.gervarro.democles.specification.emf.Constant;
import org.gervarro.democles.specification.emf.Constraint;
import org.gervarro.democles.specification.emf.ConstraintParameter;
import org.gervarro.democles.specification.emf.EMFDemoclesPatternMetamodelPlugin;
import org.gervarro.democles.specification.emf.EMFPatternBuilder;
import org.gervarro.democles.specification.emf.Pattern;
import org.gervarro.democles.specification.emf.PatternBody;
import org.gervarro.democles.specification.emf.PatternInvocationConstraint;
import org.gervarro.democles.specification.emf.SpecificationFactory;
import org.gervarro.democles.specification.emf.SpecificationPackage;
import org.gervarro.democles.specification.emf.Variable;
import org.gervarro.democles.specification.emf.constraint.EMFTypeModule;
import org.gervarro.democles.specification.emf.constraint.PatternInvocationTypeModule;
import org.gervarro.democles.specification.emf.constraint.RelationalTypeModule;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFTypeFactory;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFTypePackage;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFVariable;
import org.gervarro.democles.specification.emf.constraint.emf.emf.Reference;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraint;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraintFactory;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraintPackage;
import org.gervarro.democles.specification.impl.DefaultPattern;
import org.gervarro.democles.specification.impl.DefaultPatternBody;
import org.gervarro.democles.specification.impl.DefaultPatternFactory;
import org.gervarro.democles.specification.impl.PatternInvocationConstraintModule;
import org.gervarro.notification.model.ModelDelta;

import language.TGGRule;
import language.TGGRuleCorr;
import language.TGGRuleElement;
import language.TGGRuleNode;

public class DemoclesEngine implements MatchEventListener, PatternMatchingEngine {

	private static final Logger logger = Logger.getLogger(DemoclesEngine.class);

	private Registry registry;
	private Collection<Pattern> patterns;
	private HashMap<IDataFrame, Collection<IMatch>> matches;
	private RetePatternMatcherModule retePatternMatcherModule;
	private EMFPatternBuilder<DefaultPattern, DefaultPatternBody> patternBuilder;
	private Collection<RetePattern> patternMatchers;
	protected OperationalStrategy app;
	private HashMap<IbexPattern, Pattern> patternMap;
	private DemoclesAttributeHelper dAttrHelper;
	private IbexOptions options;
	private IbexPatternOptimiser optimizer;
	private NotificationProcessor observer;

	// Factories
	private final SpecificationFactory factory = SpecificationFactory.eINSTANCE;
	private final EMFTypeFactory emfTypeFactory = EMFTypeFactory.eINSTANCE;
	private final RelationalConstraintFactory rcFactory = RelationalConstraintFactory.eINSTANCE;

	@Override
	public void initialise(Registry registry, OperationalStrategy app, IbexOptions options) {
		this.registry = registry;
		this.options = options;
		patterns = new ArrayList<>();
		matches = new HashMap<>();
		patternMatchers = new ArrayList<>();
		this.app = app;
		patternMap = new HashMap<>();
		this.dAttrHelper = new DemoclesAttributeHelper();
		optimizer = new IbexPatternOptimiser();

		createAndRegisterPatterns();
	}
	
	@Override
	public void monitor(ResourceSet rs) {
		if (options.debug()){
			saveDemoclesPatterns(rs);		
			printReteNetwork(rs);
		}
		
		observer.install(rs);
	}

	private void createAndRegisterPatterns() {
		// Create EMF-based pattern specification
		createDemoclesPatterns();

		// Democles configuration
		final EMFInterpretableIncrementalOperationBuilder<VariableRuntime> emfNativeOperationModule = configureDemocles();

		// Build the pattern matchers in 2 phases
		// 1) EMF-based to EMF-independent transformation
		final Collection<DefaultPattern> internalPatterns = patternBuilder.build(patterns);

		// 2) EMF-independent to pattern matcher runtime (i.e., Rete network) transformation
		retePatternMatcherModule.build(internalPatterns.toArray(new DefaultPattern[internalPatterns.size()]));
		retePatternMatcherModule.getSession().setAutoCommitMode(false);
		
		// Attach match listener to pattern matchers
		retrievePatternMatchers();
		patternMatchers.forEach(pm -> pm.addEventListener(this));

		// Install model event listeners on the resource set
		EdgeDeltaFeeder edgeDeltaFeeder = new EdgeDeltaFeeder(emfNativeOperationModule);
		UndirectedEdgeToDirectedEdgeConverter undirectedEdgeToDirectedEdgeConverter = new UndirectedEdgeToDirectedEdgeConverter(edgeDeltaFeeder);
		ReferenceToEdgeConverter referenceToEdgeConverter = new ReferenceToEdgeConverter(undirectedEdgeToDirectedEdgeConverter);
		BidirectionalReferenceFilter bidirectionalReferenceFilter = new BidirectionalReferenceFilter(referenceToEdgeConverter); 
		observer = new NotificationProcessor(bidirectionalReferenceFilter, new CategoryBasedQueueFactory<ModelDelta>(ModelDeltaCategorizer.INSTANCE));
	}

	private void printReteNetwork(ResourceSet rs) {
		for (final RetePattern retePattern : retePatternMatcherModule.getPatterns()) {
			final List<RetePatternBody> bodies = retePattern.getBodies();
			for (int i = 0; i < bodies.size(); i++) {
				final RetePatternBody body = bodies.get(i);
				System.out.println(body.getHeader().toString() + " @ " + i + ": " + body.getRuntime().toString());
			}
		}
	}

	private void saveDemoclesPatterns(ResourceSet rs) {
		Resource r = rs.createResource(URI.createPlatformResourceURI(options.projectPath() + "/debug/patterns.xmi", true));
		r.getContents().addAll(patterns);
		try {
			r.save(null);
			rs.getResources().remove(r);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createDemoclesPatterns() {
		TGGCompiler compiler = new TGGCompiler(options);
		compiler.preparePatterns();

		for (TGGRule r : compiler.getRuleToPatternMap().keySet()) {
			for (IbexPattern pattern : compiler.getRuleToPatternMap().get(r)) {
				if (patternIsNotEmpty(pattern) && app.isPatternRelevant(pattern.getName()))
					ibexToDemocles(pattern);
			}
		}
		
		if(options.debug()){
			logger.debug(patterns.stream()
					 .map(p -> p.getBodies().get(0).getConstraints().size())
					 .sorted()
					 .map(i -> " " + i)
					 .collect(Collectors.joining()));
		}
	}

	private boolean patternIsNotEmpty(IbexPattern pattern) {
		return !pattern.getSignatureElements().isEmpty();
	}

	private Pattern ibexToDemocles(IbexPattern ibexPattern) {
		if (patternMap.containsKey(ibexPattern))
			return patternMap.get(ibexPattern);

		// Root pattern
		Pattern pattern = factory.createPattern();
		pattern.setName(ibexPattern.getName());
		PatternBody body = factory.createPatternBody();
		pattern.getBodies().add(body);

		// Parameters
		Map<TGGRuleNode, EMFVariable> nodeToVar = new HashMap<>();
		EList<Variable> parameters = pattern.getSymbolicParameters();

		// Extract constraints and fill nodeToVar and parameters
		EList<Constraint> constraints = ibexToDemocles(ibexPattern, body, nodeToVar, parameters);

		// Pattern invocations
		for (IbexPattern inv : ibexPattern.getPositiveInvocations()) {
			if (patternIsNotEmpty(inv)) {
				Collection<PatternInvocationConstraint> invCons = createInvocationConstraint(ibexPattern, inv, true, nodeToVar);
				invCons.stream().filter(invCon -> !invCon.getParameters().isEmpty()).forEach(invCon -> constraints.add(invCon));
			}
		}

		for (IbexPattern inv : ibexPattern.getNegativeInvocations()) {
			if (patternIsNotEmpty(inv)) {
				Collection<PatternInvocationConstraint> invCons = createInvocationConstraint(ibexPattern, inv, false, nodeToVar);
				invCons.stream().filter(invCon -> !invCon.getParameters().isEmpty()).forEach(invCon -> constraints.add(invCon));
			}
		}

		patternMap.put(ibexPattern, pattern);
		patterns.add(pattern);

		return pattern;
	}

	private EList<Constraint> ibexToDemocles(IbexPattern ibexPattern, PatternBody body, Map<TGGRuleNode, EMFVariable> nodeToVar, EList<Variable> parameters) {
		// Constraints
		EList<Constraint> constraints = body.getConstraints();

		// Constants
		EList<Constant> constants = body.getConstants();

		// Signature elements
		for (TGGRuleElement element : ibexPattern.getSignatureElements()) {
			if (!nodeToVar.containsKey(element)) {
				if (element instanceof TGGRuleNode) {
					TGGRuleNode node = (TGGRuleNode) element;
					EMFVariable var = emfTypeFactory.createEMFVariable();
					var.setName(node.getName());
					var.setEClassifier(node.getType());
					nodeToVar.put(node, var);

					dAttrHelper.extractConstants(node, var);
					dAttrHelper.extractAttributeVariables(node, var);
				}
			}
			parameters.add(nodeToVar.get(element));
		}

		// All other nodes
		EList<Variable> locals = body.getLocalVariables();
		for (TGGRuleNode node : ibexPattern.getBodyNodes()) {
			if (!nodeToVar.containsKey(node)) {
				EMFVariable var = emfTypeFactory.createEMFVariable();
				var.setName(node.getName());
				var.setEClassifier(node.getType());
				nodeToVar.put(node, var);
				locals.add(nodeToVar.get(node));

				dAttrHelper.extractConstants(node, var);
				dAttrHelper.extractAttributeVariables(node, var);
			}
		}

		dAttrHelper.resolveAttributeVariables(nodeToVar.values());

		// Attributes as constraints
		constraints.addAll(dAttrHelper.getAttributes());

		// Inplace Attribute constraints as constraints
		constraints.addAll(dAttrHelper.getRelationalConstraints());

		constants.addAll(dAttrHelper.getConstants());

		// add new variables as nodes
		locals.addAll(dAttrHelper.getEMFVariables());

		// reset attribute helper. Do it here before the recursive call of this method
		dAttrHelper.clearAll();

		// Edges as constraints
		if (!(ibexPattern instanceof CheckTranslationStatePattern && ((CheckTranslationStatePattern) ibexPattern).isLocal()))
			ibexPattern.getBodyEdges()
				.stream()
				.filter(e -> optimizer.retainAsOpposite(e, ibexPattern))
				.forEach(edge -> {
					Reference ref = emfTypeFactory.createReference();
					ref.setEModelElement(edge.getType());

					ConstraintParameter from = factory.createConstraintParameter();
					from.setReference(nodeToVar.get(edge.getSrcNode()));
					ref.getParameters().add(from);

					ConstraintParameter to = factory.createConstraintParameter();
					to.setReference(nodeToVar.get(edge.getTrgNode()));
					ref.getParameters().add(to);

					constraints.add(ref);
			});

		// Handle Corrs
		for (TGGRuleCorr corr : ibexPattern.getBodyCorrNodes()) {
			Reference srcRef = emfTypeFactory.createReference();
			srcRef.setEModelElement((EReference) corr.getType().getEStructuralFeature("source"));

			ConstraintParameter from = factory.createConstraintParameter();
			from.setReference(nodeToVar.get(corr));
			srcRef.getParameters().add(from);

			ConstraintParameter to = factory.createConstraintParameter();
			to.setReference(nodeToVar.get(corr.getSource()));
			srcRef.getParameters().add(to);

			constraints.add(srcRef);

			Reference trgRef = emfTypeFactory.createReference();
			trgRef.setEModelElement((EReference) corr.getType().getEStructuralFeature("target"));

			to = factory.createConstraintParameter();
			to.setReference(nodeToVar.get(corr));
			trgRef.getParameters().add(to);

			from = factory.createConstraintParameter();
			from.setReference(nodeToVar.get(corr.getTarget()));
			trgRef.getParameters().add(from);

			constraints.add(trgRef);
		}

		// Force injective matches through unequals-constraints
		if (ibexPattern instanceof RulePartPattern)
			forceInjectiveMatchesForPattern((RulePartPattern) ibexPattern, body, nodeToVar);

		return constraints;
	}

	private void forceInjectiveMatchesForPattern(RulePartPattern pattern, PatternBody body, Map<TGGRuleNode, EMFVariable> nodeToVar) {
		pattern.getInjectivityChecks().stream()
									  .filter(pair -> optimizer.unequalConstraintNecessary(pair))
									  .forEach(pair -> {
			RelationalConstraint unequal = rcFactory.createUnequal();

			ConstraintParameter p1 = factory.createConstraintParameter();
			ConstraintParameter p2 = factory.createConstraintParameter();
			unequal.getParameters().add(p1);
			unequal.getParameters().add(p2);
			p1.setReference(nodeToVar.get(pair.getLeft()));
			p2.setReference(nodeToVar.get(pair.getRight()));

			body.getConstraints().add(unequal);
		});
	}

	private Collection<PatternInvocationConstraint> createInvocationConstraint(IbexPattern root, IbexPattern inv, boolean isTrue, Map<TGGRuleNode, EMFVariable> nodeToVar) {
		List<PatternInvocationConstraint> invCons = new LinkedList<>();
		for (int i = 0; i < root.getMappedRuleElement(inv, inv.getSignatureElements().stream().findFirst().get()).size(); i++) {
			PatternInvocationConstraint invCon = factory.createPatternInvocationConstraint();
			invCon.setPositive(isTrue);
			invCon.setInvokedPattern(ibexToDemocles(inv));
			invCons.add(invCon);
		}

		for (TGGRuleElement element : inv.getSignatureElements()) {
			for (int i = 0; i < root.getMappedRuleElement(inv, inv.getSignatureElements().stream().findFirst().get()).size(); i++) {
				TGGRuleElement invElem = root.getMappedRuleElement(inv, element).get(i);
				ConstraintParameter parameter = factory.createConstraintParameter();
				invCons.get(i).getParameters().add(parameter);
				parameter.setReference(nodeToVar.get(invElem));

			}
		}
		return invCons;
	}

	private void retrievePatternMatchers() {
		for (Pattern pattern : patterns) {
			if (app.isPatternRelevant(pattern.getName())) {
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
		final RelationalTypeModule internalRelationalTypeModule = new RelationalTypeModule(CoreConstraintModule.INSTANCE);

		patternBuilder = new EMFPatternBuilder<DefaultPattern, DefaultPatternBody>(new DefaultPatternFactory());
		final PatternInvocationConstraintModule<DefaultPattern, DefaultPatternBody> patternInvocationTypeModule = new PatternInvocationConstraintModule<DefaultPattern, DefaultPatternBody>(patternBuilder);
		final PatternInvocationTypeModule<DefaultPattern, DefaultPatternBody> internalPatternInvocationTypeModule = new PatternInvocationTypeModule<DefaultPattern, DefaultPatternBody>(patternInvocationTypeModule);
		patternBuilder.addConstraintTypeSwitch(internalPatternInvocationTypeModule.getConstraintTypeSwitch());
		patternBuilder.addConstraintTypeSwitch(internalRelationalTypeModule.getConstraintTypeSwitch());
		patternBuilder.addConstraintTypeSwitch(internalEMFTypeModule.getConstraintTypeSwitch());
		patternBuilder.addVariableTypeSwitch(internalEMFTypeModule.getVariableTypeSwitch());

		retePatternMatcherModule = new RetePatternMatcherModule();
		
		retePatternMatcherModule.setTaskQueueFactory(new CategoryBasedQueueFactory<Task>(org.gervarro.democles.runtime.IncrementalTaskCategorizer.INSTANCE));
		
		// EMF native
		final EMFInterpretableIncrementalOperationBuilder<VariableRuntime> emfNativeOperationModule = new EMFInterpretableIncrementalOperationBuilder<VariableRuntime>(retePatternMatcherModule, emfTypeModule);
		// EMF batch
		final EMFBatchOperationBuilder<VariableRuntime> emfBatchOperationModule = new EMFBatchOperationBuilder<VariableRuntime>(emfNativeOperationModule, DefaultEMFBatchAdornmentStrategy.INSTANCE);
		final EMFIdentifierProviderBuilder<VariableRuntime> emfIdentifierProviderModule = new EMFIdentifierProviderBuilder<VariableRuntime>(JavaIdentifierProvider.INSTANCE);
		// Relational
		final ListOperationBuilder<InterpretableAdornedOperation, VariableRuntime> relationalOperationModule = new ListOperationBuilder<InterpretableAdornedOperation, VariableRuntime>(
				new RelationalOperationBuilder<VariableRuntime>());

		final ReteSearchPlanAlgorithm algorithm = new ReteSearchPlanAlgorithm();
		// EMF incremental
		final AdornedNativeOperationBuilder<VariableRuntime> emfIncrementalOperationModule = new AdornedNativeOperationBuilder<VariableRuntime>(emfNativeOperationModule, DefaultEMFIncrementalAdornmentStrategy.INSTANCE);
		// EMF component
		algorithm.addComponentBuilder(new AdornedNativeOperationDrivenComponentBuilder<VariableRuntime>(emfIncrementalOperationModule));
		// Relational component
		algorithm.addComponentBuilder(new FilterComponentBuilder<VariableRuntime>(relationalOperationModule));

		retePatternMatcherModule.setSearchPlanAlgorithm(algorithm);
		retePatternMatcherModule.addOperationBuilder(emfBatchOperationModule);
		retePatternMatcherModule.addOperationBuilder(relationalOperationModule);
		retePatternMatcherModule.addIdentifierProviderBuilder(emfIdentifierProviderModule);
		return emfNativeOperationModule;
	}

	public void updateMatches() {
		// Trigger the Rete network
		retePatternMatcherModule.performIncrementalUpdates();
	}

	public void terminate() {
		patternMatchers.forEach(pm -> pm.removeEventListener(this));
	}

	public void handleEvent(final MatchEvent event) {
		// React to events
		final String type = event.getEventType();
		final DataFrame frame = event.getMatching();

		if (options.debug())
			logger.debug("Received match:  " + event);

		Optional<Pattern> p = patterns.stream().filter(pattern -> getPatternID(pattern).equals(event.getSource().toString())).findAny();

		p.ifPresent(pattern -> {
			// React to create
			if (type.contentEquals(MatchEvent.INSERT) && (!matches.keySet().contains(frame) || matches.get(frame).stream().allMatch(m -> !m.patternName().equals(pattern.getName())))) {
				IMatch match = new DemoclesMatch(frame, pattern);
				if (matches.keySet().contains(frame)) {
					matches.get(frame).add(match);
				} else {
					matches.put(frame, new ArrayList<IMatch>(Arrays.asList(match)));
				}
				// TODO [Anjorin] Better way of accessing rule name.
				app.addOperationalRuleMatch(PatternSuffixes.removeSuffix(pattern.getName()), match);
			}

			// React to delete
			if (type.equals(MatchEvent.DELETE)) {
				Collection<IMatch> matchList = matches.get(frame);
				Optional<IMatch> match = matchList == null ? Optional.empty() : matchList.stream().filter(m -> m.patternName().equals(pattern.getName())).findAny();

				match.ifPresent(m -> {
					if (m.patternName().endsWith(PatternSuffixes.CONSISTENCY)) {
						app.addBrokenMatch(m);
					}

					app.removeOperationalRuleMatch(m);
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
	public void registerInternalMetamodels() {
		SpecificationPackage.init();
		RelationalConstraintPackage.init();
		EMFTypePackage.init();
	}

	@Override
	public ResourceSet createAndPrepareResourceSet(String workspacePath) {
		ResourceSet rs = EMFDemoclesPatternMetamodelPlugin.createDefaultResourceSet();
		try {
			EMFDemoclesPatternMetamodelPlugin.setWorkspaceRootDirectory(rs, new File(workspacePath).getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rs;
	}
}