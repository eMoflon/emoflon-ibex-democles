package org.emoflon.ibex.gt.democles.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.emoflon.ibex.common.utils.IBeXPatternUtils;
import org.emoflon.ibex.gt.transformations.AbstractModelTransformation;
import org.gervarro.democles.specification.emf.ConstraintParameter;
import org.gervarro.democles.specification.emf.Pattern;
import org.gervarro.democles.specification.emf.PatternInvocationConstraint;
import org.gervarro.democles.specification.emf.SpecificationFactory;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFVariable;

import IBeXLanguage.IBeXContext;
import IBeXLanguage.IBeXContextAlternatives;
import IBeXLanguage.IBeXContextPattern;
import IBeXLanguage.IBeXNode;
import IBeXLanguage.IBeXPatternInvocation;
import IBeXLanguage.IBeXPatternSet;

/**
 * Transformation from the IBeX model to Democles Patterns.
 */
public class IBeXToDemoclesPatternTransformation extends AbstractModelTransformation<IBeXPatternSet, List<Pattern>> {
	// Factories from Democles.
	private static final SpecificationFactory democlesSpecificationFactory = SpecificationFactory.eINSTANCE;

	/**
	 * Democles patterns.
	 */
	private List<Pattern> democlesPatterns = new ArrayList<>();

	/**
	 * Mapping between pattern names and Democles patterns.
	 */
	private HashMap<String, Pattern> patternMap = new HashMap<>();

	@Override
	public List<Pattern> transform(final IBeXPatternSet ibexPatternSet) {
		for (IBeXContext ibexPattern : ibexPatternSet.getContextPatterns()) {
			if (ibexPattern instanceof IBeXContextPattern) {
				transformPatternIfNotEmpty((IBeXContextPattern) ibexPattern);
			} else if (ibexPattern instanceof IBeXContextAlternatives) {
				transformAlternatives((IBeXContextAlternatives) ibexPattern);
			}
		}
		return democlesPatterns;
	}

	/**
	 * Transforms the context pattern to a Democles {@link Pattern} if the pattern
	 * is not empty.
	 * 
	 * @param ibexPattern
	 *            the IBeXContextPattern to transform
	 * @return the Democles pattern
	 */
	private void transformPatternIfNotEmpty(final IBeXContextPattern ibexPattern) {
		if (!IBeXPatternUtils.isEmptyPattern(ibexPattern)) {
			transformPattern(ibexPattern);
		}
	}

	/**
	 * Transforms the patterns of the given alternative to to a Democles
	 * {@link Pattern}.
	 * 
	 * @param ibexAlternatives
	 *            the set of alternative patterns
	 */
	private void transformAlternatives(final IBeXContextAlternatives ibexAlternatives) {
		for (IBeXContextPattern ibexPattern : ibexAlternatives.getAlternativePatterns()) {
			transformPatternIfNotEmpty(ibexPattern);
		}
	}

	/**
	 * Transforms the context pattern to a Democles {@link Pattern} if it has not
	 * been transformed before.
	 * 
	 * @param ibexPattern
	 *            the IBeXContextPattern to transform
	 * @return the Democles pattern
	 */
	private Pattern transformPattern(final IBeXContextPattern ibexPattern) {
		if (patternMap.containsKey(ibexPattern.getName())) {
			return patternMap.get(ibexPattern.getName());
		}

		// Transform nodes, edges, injectivity and attribute constraints.
		IBeXToDemoclesPatternHelper patternHelper = new IBeXToDemoclesPatternHelper(ibexPattern);
		Pattern democlesPattern = patternHelper.transform();

		// Transform each invocations to a PatternInvocationConstraint.
		transformPatternInvocations(ibexPattern, democlesPattern, patternHelper.getNodeToVariableMapping());

		// Add to patterns.
		patternMap.put(ibexPattern.getName(), democlesPattern);
		democlesPatterns.add(democlesPattern);
		return democlesPattern;
	}

	/**
	 * Transforms the pattern invocation from the IBeX to the Democles
	 * representation.
	 * 
	 * @param ibexPattern
	 *            the {@link IBeXContextPattern} whose invocations to transform
	 * @param democlesPattern
	 *            the DemoclesPattern
	 * @param nodeToVariable
	 *            the mapping from IBeXNodes to Democles variables
	 */
	private void transformPatternInvocations(final IBeXContextPattern ibexPattern, final Pattern democlesPattern,
			final Map<IBeXNode, EMFVariable> nodeToVariable) {
		for (final IBeXPatternInvocation invocation : ibexPattern.getInvocations()) {
			PatternInvocationConstraint constraint = transformInvocation(invocation, nodeToVariable);
			democlesPattern.getBodies().get(0).getConstraints().add(constraint);
		}
	}

	/**
	 * Transforms a pattern invocation.
	 * 
	 * @param ibexInvocation
	 *            the IBeXPatternInvocation to transform
	 * @param nodeToVariable
	 *            the mapping between IBeXNodes and variables
	 * @return the PatternInvocationConstraint
	 */
	private PatternInvocationConstraint transformInvocation(final IBeXPatternInvocation ibexInvocation,
			final Map<IBeXNode, EMFVariable> nodeToVariable) {
		PatternInvocationConstraint invocationConstraint = democlesSpecificationFactory
				.createPatternInvocationConstraint();
		invocationConstraint.setPositive(ibexInvocation.isPositive());
		invocationConstraint.setInvokedPattern(this.transformPattern(ibexInvocation.getInvokedPattern()));

		for (final IBeXNode signatureNode : ibexInvocation.getInvokedPattern().getSignatureNodes()) {
			Optional<IBeXNode> nodeMappedToSignatureNode = ibexInvocation.getMapping().stream()
					.filter(map -> map.getValue().equals(signatureNode)).map(map -> map.getKey()).findAny();
			if (!nodeMappedToSignatureNode.isPresent()) {
				throw new IllegalArgumentException(signatureNode.getName() + " not mapped!");
			}

			ConstraintParameter parameter = democlesSpecificationFactory.createConstraintParameter();
			parameter.setReference(nodeToVariable.get(nodeMappedToSignatureNode.get()));
			invocationConstraint.getParameters().add(parameter);
		}

		return invocationConstraint;
	}
}
