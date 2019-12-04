package org.emoflon.ibex.tgg.runtime.democles;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emoflon.ibex.common.operational.IMatch;
import org.emoflon.ibex.common.operational.IMatchObserver;
import org.emoflon.ibex.gt.democles.runtime.DemoclesGTEngine;
import org.emoflon.ibex.gt.democles.runtime.IBeXToDemoclesPatternTransformation;
import org.emoflon.ibex.tgg.compiler.transformations.patterns.ContextPatternTransformation;
import org.emoflon.ibex.tgg.operational.IBlackInterpreter;
import org.emoflon.ibex.tgg.operational.defaults.IbexOptions;
import org.emoflon.ibex.tgg.operational.strategies.OperationalStrategy;
import org.emoflon.ibex.tgg.runtime.democles.csp.nativeOps.TGGAttributeConstraintAdornmentStrategy;
import org.emoflon.ibex.tgg.runtime.democles.csp.nativeOps.TGGAttributeConstraintModule;
import org.emoflon.ibex.tgg.runtime.democles.csp.nativeOps.TGGAttributeConstraintTypeModule;
import org.emoflon.ibex.tgg.runtime.democles.csp.nativeOps.TGGConstraintComponentBuilder;
import org.emoflon.ibex.tgg.runtime.democles.csp.nativeOps.TGGNativeOperationBuilder;
import org.gervarro.democles.common.DataFrame;
import org.gervarro.democles.common.runtime.OperationBuilder;
import org.gervarro.democles.common.runtime.VariableRuntime;
import org.gervarro.democles.interpreter.incremental.rete.RetePattern;
import org.gervarro.democles.interpreter.incremental.rete.RetePatternBody;
import org.gervarro.democles.plan.incremental.leaf.Component;
import org.gervarro.democles.plan.incremental.leaf.ReteSearchPlanAlgorithm;
import org.gervarro.democles.runtime.GenericOperationBuilder;
import org.gervarro.democles.specification.emf.Pattern;
import org.gervarro.democles.specification.emf.TypeModule;

import IBeXLanguage.IBeXContextPattern;
import IBeXLanguage.IBeXPatternSet;
import language.TGGNamedElement;

/**
 * Engine for (bidirectional) graph transformations with Democles.
 */
public class DemoclesTGGEngine extends DemoclesGTEngine implements IBlackInterpreter {
	private IbexOptions options;
	private IBeXPatternSet ibexPatterns;
	private Map<IBeXContextPattern, TGGNamedElement> patternToRuleMap;
	private OperationalStrategy strategy;

	/**
	 * Creates a new DemoclesTGGEngine.
	 */
	public DemoclesTGGEngine() {
		super();
	}

	@Override
	public void initialise(final IbexOptions options, Registry registry, IMatchObserver matchObserver) {
		super.initialise(registry, matchObserver);
		
		this.options = options;
		this.strategy = (OperationalStrategy) matchObserver;
		
		ContextPatternTransformation compiler = new ContextPatternTransformation(options, strategy);
		ibexPatterns = compiler.transform();
		patternToRuleMap = compiler.getPatternToRuleMap();
		initPatterns(ibexPatterns);
	}

	@Override
	public void initPatterns(final IBeXPatternSet ibexPatternSet) {
		IBeXToDemoclesPatternTransformation transformation = new TGGIBeXToDemoclesPatternTransformation(options,
				patternToRuleMap);
		this.setPatterns(transformation.transform(ibexPatternSet));
		this.savePatternsForDebugging();
		this.createAndRegisterPatterns();
	}

	@Override
	protected ReteSearchPlanAlgorithm initReteSearchPlanAlgorithm(
			Collection<OperationBuilder<Component, Component, VariableRuntime>> builders) {
		ReteSearchPlanAlgorithm algorithm = super.initReteSearchPlanAlgorithm(builders);
		this.handleTGGAttributeConstraints().ifPresent(b -> algorithm.addComponentBuilder(b));
		return algorithm;
	}

	private Optional<TGGConstraintComponentBuilder<VariableRuntime>> handleTGGAttributeConstraints() {
		if (!this.options.blackInterpSupportsAttrConstrs()) {
			return Optional.empty();
		}

		// Handle constraints for the EMF to Java transformation
		TGGAttributeConstraintModule.INSTANCE.registerConstraintTypes(options.constraintProvider());
		TypeModule<TGGAttributeConstraintModule> tggAttributeConstraintTypeModule = new TGGAttributeConstraintTypeModule(
				TGGAttributeConstraintModule.INSTANCE);
		patternBuilder.addConstraintTypeSwitch(tggAttributeConstraintTypeModule.getConstraintTypeSwitch());

		// Native operation
		final TGGNativeOperationBuilder<VariableRuntime> tggNativeOperationModule = new TGGNativeOperationBuilder<VariableRuntime>(
				options.constraintProvider());
		// Batch operations
		final GenericOperationBuilder<VariableRuntime> tggBatchOperationModule = new GenericOperationBuilder<VariableRuntime>(
				tggNativeOperationModule, TGGAttributeConstraintAdornmentStrategy.INSTANCE);
		retePatternMatcherModule.addOperationBuilder(tggBatchOperationModule);

		// Incremental operation
		return Optional.of(new TGGConstraintComponentBuilder<VariableRuntime>(tggNativeOperationModule));
	}

	@Override
	public void monitor(final ResourceSet resourceSet) {
		if (options.debug()) {
			savePatterns(resourceSet, options.projectPath() + "/debug/democles-patterns.xmi", patterns.values()//
					.stream()//
					.sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))//
					.collect(Collectors.toList()));

			savePatterns(resourceSet, options.projectPath() + "/debug/ibex-patterns.xmi", Arrays.asList(ibexPatterns));
		}

		super.monitor(resourceSet);
	}

	/**
	 * Use this method to get extra debug information concerning the rete network.
	 * Currently not used to reduce debug output.
	 */
	@SuppressWarnings("unused")
	private void printReteNetwork() {
		for (final RetePattern retePattern : retePatternMatcherModule.getPatterns()) {
			final List<RetePatternBody> bodies = retePattern.getBodies();
			for (int i = 0; i < bodies.size(); i++) {
				final RetePatternBody body = bodies.get(i);
				System.out.println(body.getHeader().toString() + " @ " + i + ": " + body.getRuntime().toString());
			}
		}
	}

	private void savePatterns(ResourceSet rs, String path, Collection<EObject> patterns) {
		Resource democlesPatterns = rs.createResource(URI.createPlatformResourceURI(path, true));
		democlesPatterns.getContents().addAll(patterns);
		try {
			democlesPatterns.save(null);
			rs.getResources().remove(democlesPatterns);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected IMatch createMatch(final DataFrame frame, final Pattern pattern) {
		return new DemoclesTGGMatch(frame, pattern);
	}
}