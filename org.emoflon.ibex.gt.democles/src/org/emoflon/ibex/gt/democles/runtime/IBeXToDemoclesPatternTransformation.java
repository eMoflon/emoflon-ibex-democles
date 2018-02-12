package org.emoflon.ibex.gt.democles.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.emoflon.ibex.gt.transformations.AbstractModelTransformation;
import org.gervarro.democles.specification.emf.ConstraintParameter;
import org.gervarro.democles.specification.emf.Pattern;
import org.gervarro.democles.specification.emf.PatternBody;
import org.gervarro.democles.specification.emf.PatternInvocationConstraint;
import org.gervarro.democles.specification.emf.SpecificationFactory;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFTypeFactory;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFVariable;
import org.gervarro.democles.specification.emf.constraint.emf.emf.Reference;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraint;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraintFactory;

import IBeXLanguage.IBeXEdge;
import IBeXLanguage.IBeXNode;
import IBeXLanguage.IBeXNodePair;
import IBeXLanguage.IBeXPattern;
import IBeXLanguage.IBeXPatternInvocation;
import IBeXLanguage.IBeXPatternSet;

/**
 * Transformation from an IBeXPatternSet to Democles Patterns.
 * 
 * @author Patrick Robrecht
 * @version 0.1
 */
public class IBeXToDemoclesPatternTransformation extends AbstractModelTransformation<IBeXPatternSet, List<Pattern>> {
	// Factories from Democles.
	private static final SpecificationFactory democlesSpecificationFactory = SpecificationFactory.eINSTANCE;
	private static final EMFTypeFactory democlesEmfTypeFactory = EMFTypeFactory.eINSTANCE;
	private static final RelationalConstraintFactory democlesRelationalConstraintFactory = RelationalConstraintFactory.eINSTANCE;

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
		ibexPatternSet.getPatterns().forEach(p -> this.transformPattern(p));
		return this.democlesPatterns;
	}

	/**
	 * Transforms an {@link IBeXPattern} to a Democles {@link Pattern}.
	 * 
	 * @param ibexPattern
	 *            the IBeXPattern to transform
	 * @return the Democles pattern
	 */
	private Pattern transformPattern(final IBeXPattern ibexPattern) {
		if (patternMap.containsKey(ibexPattern.getName())) {
			return patternMap.get(ibexPattern.getName());
		}

		Pattern pattern = democlesSpecificationFactory.createPattern();
		pattern.setName(ibexPattern.getName());

		PatternBody body = democlesSpecificationFactory.createPatternBody();
		pattern.getBodies().add(body);

		// Signature nodes -> parameter in Democles pattern.
		Map<IBeXNode, EMFVariable> nodeToVariable = new HashMap<>();
		ibexPattern.getSignatureNodes().forEach(ibexSignatureNode -> {
			if (!nodeToVariable.containsKey(ibexSignatureNode)) {
				nodeToVariable.put(ibexSignatureNode, transformSignatureNodeToVariable(ibexSignatureNode));
			}
			pattern.getSymbolicParameters().add(nodeToVariable.get(ibexSignatureNode));
		});

		// Local node -> local variables in the Democles PatternBody.
		ibexPattern.getLocalNodes().forEach(ibexLocalNode -> {
			if (!nodeToVariable.containsKey(ibexLocalNode)) {
				nodeToVariable.put(ibexLocalNode, transformSignatureNodeToVariable(ibexLocalNode));
			}
			body.getLocalVariables().add(nodeToVariable.get(ibexLocalNode));
		});

		ibexPattern.getInjectivityConstraints().forEach(injectivityConstraint -> {
			body.getConstraints()
					.add(transformInjectivityConstraintToRelationalConstraint(injectivityConstraint, nodeToVariable));
		});

		// Local edges -> Constraint of type Reference in the Democles PatternBody.
		ibexPattern.getLocalEdges().forEach(ibexLocalEdge -> {
			body.getConstraints().add(transformLocalEdgeToReference(ibexLocalEdge, nodeToVariable));
		});

		// Invocations -> PatternInvocationConstraint in the Democles PatternBody.
		ibexPattern.getInvocations().forEach(invocation -> {
			body.getConstraints().add(this.transformInvocation(invocation, nodeToVariable));
		});

		// Add to patterns.
		this.patternMap.put(ibexPattern.getName(), pattern);
		this.democlesPatterns.add(pattern);
		return pattern;
	}

	/**
	 * Transforms an {@link IBeXNode} to an equivalent {@link EMFVariable}.
	 * 
	 * @param ibexNode
	 *            the IBeXNode to transform
	 * @return the EMFVariable
	 */
	private static EMFVariable transformSignatureNodeToVariable(final IBeXNode ibexNode) {
		EMFVariable variable = democlesEmfTypeFactory.createEMFVariable();
		variable.setName(ibexNode.getName());
		variable.setEClassifier(ibexNode.getType());
		return variable;
	}

	/**
	 * Transforms an {@link IBeXNodePair} for injectivity to an equivalent
	 * RelationalConstraint.
	 * 
	 * @param injectivityConstraint
	 *            the pair of nodes which must be different
	 * @param nodeToVariable
	 *            the mapping between IBeXNodes and variables
	 * @return the constraint
	 */
	private static RelationalConstraint transformInjectivityConstraintToRelationalConstraint(
			final IBeXNodePair injectivityConstraint, final Map<IBeXNode, EMFVariable> nodeToVariable) {
		RelationalConstraint unequalConstraint = democlesRelationalConstraintFactory.createUnequal();
		injectivityConstraint.getValues().forEach(node -> {
			ConstraintParameter p = democlesSpecificationFactory.createConstraintParameter();
			p.setReference(nodeToVariable.get(node));
			unequalConstraint.getParameters().add(p);
		});
		return unequalConstraint;
	}

	/**
	 * Transforms an {@link IBeXEdge} to an equivalent {@link Reference}.
	 * 
	 * @param ibexEdge
	 *            the IBeXEdge to transform
	 * @param nodeToVariable
	 *            the mapping between IBeXNodes and variables
	 * @return the Reference
	 */
	private static Reference transformLocalEdgeToReference(final IBeXEdge ibexEdge,
			final Map<IBeXNode, EMFVariable> nodeToVariable) {
		// Edge type.
		Objects.requireNonNull(ibexEdge.getType(), "The type of IBeXEdge may not be null!");
		Reference reference = democlesEmfTypeFactory.createReference();
		reference.setEModelElement(ibexEdge.getType());

		// Parameter for the source node.
		Objects.requireNonNull(ibexEdge.getSourceNode(), "The source node of an IBeXEdge may not be null!");
		Objects.requireNonNull(nodeToVariable.get(ibexEdge.getSourceNode()),
				"A mapping for the source node must exist!");
		ConstraintParameter parameterForSourceNode = democlesSpecificationFactory.createConstraintParameter();
		parameterForSourceNode.setReference(nodeToVariable.get(ibexEdge.getSourceNode()));
		reference.getParameters().add(parameterForSourceNode);

		// Parameter for the target node.
		Objects.requireNonNull(ibexEdge.getTargetNode(), "The target node of an IBeXEdge may not be null!");
		Objects.requireNonNull(nodeToVariable.get(ibexEdge.getTargetNode()),
				"A mapping for the target node must exist!");
		ConstraintParameter parameterForTargetNode = democlesSpecificationFactory.createConstraintParameter();
		parameterForTargetNode.setReference(nodeToVariable.get(ibexEdge.getTargetNode()));
		reference.getParameters().add(parameterForTargetNode);

		return reference;
	}

	/**
	 * Transforms an {@link IBeXPatternInvocation} to an equivalent
	 * {@link PatternInvocationConstraint}.
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

		ibexInvocation.getInvokedPattern().getSignatureNodes().forEach(signatureNode -> {
			// Find the node mapped to the signature node.
			IBeXNode nodeMappedToSignatureNode = ibexInvocation.getMapping().stream()
					.filter(map -> map.getValue().equals(signatureNode)).map(map -> map.getKey()).findAny()
					.orElse(null);
			Objects.requireNonNull(nodeMappedToSignatureNode, signatureNode.getName() + " not mapped!");

			ConstraintParameter parameter = democlesSpecificationFactory.createConstraintParameter();
			parameter.setReference(nodeToVariable.get(nodeMappedToSignatureNode));
			invocationConstraint.getParameters().add(parameter);
		});

		return invocationConstraint;
	}
}
