package org.emoflon.ibex.gt.democles.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.gervarro.democles.event.MatchEvent;
import org.gervarro.democles.event.MatchEventListener;
import org.gervarro.democles.specification.emf.ConstraintParameter;
import org.gervarro.democles.specification.emf.Pattern;
import org.gervarro.democles.specification.emf.PatternBody;
import org.gervarro.democles.specification.emf.PatternInvocationConstraint;
import org.gervarro.democles.specification.emf.SpecificationFactory;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFTypeFactory;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFVariable;
import org.gervarro.democles.specification.emf.constraint.emf.emf.Reference;

import IBeXLanguage.IBeXEdge;
import IBeXLanguage.IBeXNode;
import IBeXLanguage.IBeXPattern;
import IBeXLanguage.IBeXPatternInvocation;

/**
 * Engine for Unidirectional Graph Transformations with Democles pattern
 * matcher.
 * 
 * @author Patrick Robrecht
 * @version 0.1
 */
public class GTDemoclesEngine implements MatchEventListener {
	// Factories from Democles.
	private static final SpecificationFactory democlesSpecificationFactory = SpecificationFactory.eINSTANCE;
	private static final EMFTypeFactory democlesEmfTypeFactory = EMFTypeFactory.eINSTANCE;

	/**
	 * The Democles patterns.
	 */
	private List<Pattern> patterns = new ArrayList<>();

	/**
	 * A mapping between IBeXPatterns and Democles Patterns.
	 */
	private HashMap<IBeXPattern, Pattern> patternMap = new HashMap<>();

	public void createDemoclesPatterns() {
		List<IBeXPattern> ibexPatterns = new ArrayList<>(); // TODO add all IBeXPatterns to the list.
		ibexPatterns.forEach(ibexPattern -> this.getPattern(ibexPattern));
	}

	/**
	 * Returns the Democles Pattern for the given {@link IBeXPattern}. If the
	 * Democles pattern does not exist yet, it is created and added to the Democles
	 * patterns.
	 * 
	 * @param ibexPattern
	 *            the IBeXPattern
	 * @return the Democles pattern
	 */
	private Pattern getPattern(final IBeXPattern ibexPattern) {
		if (patternMap.containsKey(ibexPattern)) {
			// Democles pattern already exists, so just return it.
			return patternMap.get(ibexPattern);
		}

		Pattern pattern = this.transformPattern(ibexPattern);
		patternMap.put(ibexPattern, pattern);
		patterns.add(pattern);
		return pattern;
	}

	/**
	 * Transforms an {@link IBeXPattern} to a Democles {@link Pattern}.
	 * 
	 * @param ibexPattern
	 *            the IBeXPattern to transform
	 * @return the Democles pattern
	 */
	private Pattern transformPattern(final IBeXPattern ibexPattern) {
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

		// Local node -> local variables in the body of the Democles pattern.
		ibexPattern.getLocalNodes().forEach(ibexLocalNode -> {
			if (!nodeToVariable.containsKey(ibexLocalNode)) {
				nodeToVariable.put(ibexLocalNode, transformSignatureNodeToVariable(ibexLocalNode));
			}
			body.getLocalVariables().add(nodeToVariable.get(ibexLocalNode));
		});

		// Local edges -> Constraint of type Reference in the body of the Democles
		// pattern.
		ibexPattern.getLocalEdges().forEach(ibexLocalEdge -> {
			body.getConstraints().add(transformLocalEdgeToReference(ibexLocalEdge, nodeToVariable));
		});

		// Invocations -> Constraint of type PatternInvocationConstraint in the body of
		// the Democles pattern.
		ibexPattern.getInvocations().forEach(invocation -> {
			body.getConstraints().add(this.transformInvocation(invocation, nodeToVariable));
		});

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
	 * Transforms an {@link IBeXEdge} to an equivalent {@link Reference}.
	 * 
	 * @param ibexEdge
	 *            the IBeXEdge to transfrom
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
		invocationConstraint.setInvokedPattern(this.getPattern(ibexInvocation.getInvokedBy()));

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

	/**
	 * Handles {@link MatchEvent} from Democles.
	 * 
	 * @param event
	 *            the MatchEvent to handle
	 */
	@Override
	public void handleEvent(final MatchEvent event) {
		// TODO Auto-generated method stub
		System.out.println("GTDemoclesEngine.handleEvent: " + event.getEventType() + " - " + event.getSource());
	}
}
