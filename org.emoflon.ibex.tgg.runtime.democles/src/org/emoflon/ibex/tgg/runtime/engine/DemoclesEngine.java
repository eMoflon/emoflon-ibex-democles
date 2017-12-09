package org.emoflon.ibex.tgg.runtime.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emoflon.ibex.tgg.compiler.TGGCompiler;
import org.emoflon.ibex.tgg.compiler.patterns.PatternSuffixes;
import org.emoflon.ibex.tgg.compiler.patterns.common.IPattern;
import org.emoflon.ibex.tgg.compiler.patterns.common.PatternInvocation;
import org.emoflon.ibex.tgg.operational.OperationalStrategy;
import org.emoflon.ibex.tgg.operational.PatternMatchingEngine;
import org.emoflon.ibex.tgg.operational.util.IMatch;
import org.emoflon.ibex.tgg.operational.util.IbexOptions;
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
import org.gervarro.democles.specification.emf.Constraint;
import org.gervarro.democles.specification.emf.ConstraintParameter;
import org.gervarro.democles.specification.emf.EMFDemoclesPatternMetamodelPlugin;
import org.gervarro.democles.specification.emf.EMFPatternBuilder;
import org.gervarro.democles.specification.emf.Pattern;
import org.gervarro.democles.specification.emf.PatternBody;
import org.gervarro.democles.specification.emf.PatternInvocationConstraint;
import org.gervarro.democles.specification.emf.SpecificationFactory;
import org.gervarro.democles.specification.emf.SpecificationPackage;
import org.gervarro.democles.specification.emf.TypeModule;
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
	private IbexOptions options;
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
		r.getContents().addAll(
				patterns.stream()
				  .sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
				  .collect(Collectors.toList())
				);
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

		for (String r : compiler.getRuleToPatternMap().keySet()) {
			for (IPattern pattern : compiler.getRuleToPatternMap().get(r)) {
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

	private boolean patternIsNotEmpty(IPattern pattern) {
		return !pattern.getSignatureNodes().isEmpty();
	}

	private Pattern ibexToDemocles(IPattern ibexPattern) {
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
		for (PatternInvocation inv : ibexPattern.getPositiveInvocations()) {
			if (patternIsNotEmpty(inv.getInvokedPattern())) {
				PatternInvocationConstraint invCon = createInvocationConstraint(inv, true, nodeToVar);
				if(!invCon.getParameters().isEmpty())
					constraints.add(invCon);
			}
		}

		for (PatternInvocation inv : ibexPattern.getNegativeInvocations()) {
			if (patternIsNotEmpty(inv.getInvokedPattern())) {
				PatternInvocationConstraint invCon = createInvocationConstraint(inv, false, nodeToVar);
				if(!invCon.getParameters().isEmpty())
					constraints.add(invCon);
			}
		}

		patternMap.put(ibexPattern, pattern);
		patterns.add(pattern);

		return pattern;
	}

	private EList<Constraint> ibexToDemocles(IPattern ibexPattern, PatternBody body, Map<TGGRuleNode, EMFVariable> nodeToVar, EList<Variable> parameters) {		
		createVariablesForNodes(ibexPattern, body, nodeToVar, parameters);
		
		if(TGGCompiler.isRootPattern(ibexPattern)) {
			DemoclesAttributeHelper dAttrHelper = new DemoclesAttributeHelper();
			dAttrHelper.createAttributeConstraints(ibexPattern, body, nodeToVar, parameters);
		}
		
		createConstraintsForEdges(ibexPattern, nodeToVar, body.getConstraints());
		createUnequalConstraintsForInjectivity(ibexPattern, body, nodeToVar);

		return body.getConstraints();
	}

	private void createVariablesForNodes(IPattern ibexPattern, PatternBody body, Map<TGGRuleNode, EMFVariable> nodeToVar, EList<Variable> parameters) {
		// Signature elements
		for (TGGRuleNode element : ibexPattern.getSignatureNodes()) {
			if (!nodeToVar.containsKey(element)) {
				if (element instanceof TGGRuleNode) {
					TGGRuleNode node = (TGGRuleNode) element;
					EMFVariable var = emfTypeFactory.createEMFVariable();
					var.setName(node.getName());
					var.setEClassifier(node.getType());
					nodeToVar.put(node, var);
				}
			}
			parameters.add(nodeToVar.get(element));
		}
	
		// All other nodes
		EList<Variable> locals = body.getLocalVariables();
		Collection<TGGRuleNode> allOtherNodes = new ArrayList<>(ibexPattern.getLocalNodes());
		for (TGGRuleNode node : allOtherNodes) {
			if (!nodeToVar.containsKey(node)) {
				EMFVariable var = emfTypeFactory.createEMFVariable();
				var.setName(node.getName());
				var.setEClassifier(node.getType());
				nodeToVar.put(node, var);
			}
			
			locals.add(nodeToVar.get(node));
		}
	}

	private void createUnequalConstraintsForInjectivity(IPattern ibexPattern, PatternBody body, Map<TGGRuleNode, EMFVariable> nodeToVar) {
		// Force injective matches through unequals-constraints
		forceInjectiveMatchesForPattern((RulePartPattern) ibexPattern, body, nodeToVar);
	}

	private void createConstraintsForEdges(IPattern ibexPattern, Map<TGGRuleNode, EMFVariable> nodeToVar, EList<Constraint> constraints) {
		ibexPattern.getLocalEdges()
			.stream()
			.forEach(edge -> {
				assert(edge.getSrcNode() != null);
				assert(edge.getTrgNode() != null);
				assert(edge.getType() != null);
				assert(nodeToVar.containsKey(edge.getSrcNode()));
				assert(nodeToVar.containsKey(edge.getTrgNode()));
				
				Reference ref = emfTypeFactory.createReference();
				ref.setEModelElement(edge.getType());

				ConstraintParameter from = factory.createConstraintParameter();
				from.setReference(nodeToVar.get(edge.getSrcNode()));
				ref.getParameters().add(from);

				ConstraintParameter to = factory.createConstraintParameter();
				to.setReference(nodeToVar.get(edge.getTrgNode()));
				ref.getParameters().add(to);

				constraints.add(ref);
	}

	private void forceInjectiveMatchesForPattern(IPattern pattern, PatternBody body, Map<TGGRuleNode, EMFVariable> nodeToVar) {
		pattern.getInjectivityChecks().stream()
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

	private PatternInvocationConstraint createInvocationConstraint(PatternInvocation inv, boolean isTrue, Map<TGGRuleNode, EMFVariable> nodeToVar) {
			PatternInvocationConstraint invCon = factory.createPatternInvocationConstraint();
			invCon.setPositive(isTrue);
			invCon.setInvokedPattern(ibexToDemocles(inv.getInvokedPattern()));

		for (TGGRuleNode element : inv.getInvokedPattern().getSignatureNodes()) {
			TGGRuleNode invElem = inv.getPreImage(element);
			ConstraintParameter parameter = factory.createConstraintParameter();
			invCon.getParameters().add(parameter);
			parameter.setReference(nodeToVar.get(invElem));
			assert(parameter.getReference() != null);
		}

		return invCon;
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
		// NativeOperation
		final EMFInterpretableIncrementalOperationBuilder<VariableRuntime> emfNativeOperationModule =
				new EMFInterpretableIncrementalOperationBuilder<VariableRuntime>(retePatternMatcherModule, emfTypeModule);
		// EMF batch
		final EMFBatchOperationBuilder<VariableRuntime> emfBatchOperationModule =
				new EMFBatchOperationBuilder<VariableRuntime>(emfNativeOperationModule, DefaultEMFBatchAdornmentStrategy.INSTANCE);
		final EMFIdentifierProviderBuilder<VariableRuntime> emfIdentifierProviderModule =
				new EMFIdentifierProviderBuilder<VariableRuntime>(JavaIdentifierProvider.INSTANCE);
		// Relational
		final ListOperationBuilder<InterpretableAdornedOperation, VariableRuntime> relationalOperationModule =
				new ListOperationBuilder<InterpretableAdornedOperation, VariableRuntime>(
						new RelationalOperationBuilder<VariableRuntime>());
		
		final ReteSearchPlanAlgorithm algorithm = new ReteSearchPlanAlgorithm();
		// EMF incremental
		final AdornedNativeOperationBuilder<VariableRuntime> emfIncrementalOperationModule =
				new AdornedNativeOperationBuilder<VariableRuntime>(emfNativeOperationModule, DefaultEMFIncrementalAdornmentStrategy.INSTANCE);
		// EMF component
		algorithm.addComponentBuilder(new AdornedNativeOperationDrivenComponentBuilder<VariableRuntime>(emfIncrementalOperationModule));
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
		TGGAttributeConstraintAdornmentStrategy.INSTANCE.setIsModelGen(
				options.isModelGen());
		
		// Handle constraints for the EMF to Java transformation
		TGGAttributeConstraintModule.INSTANCE.registerConstraintTypes(options.constraintProvider());
		TypeModule<TGGAttributeConstraintModule> tggAttributeConstraintTypeModule =
				new TGGAttributeConstraintTypeModule(TGGAttributeConstraintModule.INSTANCE);
		patternBuilder.addConstraintTypeSwitch(tggAttributeConstraintTypeModule.getConstraintTypeSwitch());		
		
		// Native operation
		final TGGNativeOperationBuilder<VariableRuntime> tggNativeOperationModule =
				new TGGNativeOperationBuilder<VariableRuntime>(options.constraintProvider());
		// Batch operations
		final EMFBatchOperationBuilder<VariableRuntime> tggBatchOperationModule =
				new EMFBatchOperationBuilder<VariableRuntime>(tggNativeOperationModule,
						TGGAttributeConstraintAdornmentStrategy.INSTANCE);
		retePatternMatcherModule.addOperationBuilder(tggBatchOperationModule);
		// Incremental operation
		algorithm.addComponentBuilder(
				new TGGConstraintComponentBuilder<VariableRuntime>(tggNativeOperationModule));
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