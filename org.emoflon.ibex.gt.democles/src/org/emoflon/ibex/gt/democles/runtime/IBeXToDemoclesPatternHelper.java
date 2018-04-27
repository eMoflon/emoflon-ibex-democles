package org.emoflon.ibex.gt.democles.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.gervarro.democles.specification.emf.ConstraintParameter;
import org.gervarro.democles.specification.emf.ConstraintVariable;
import org.gervarro.democles.specification.emf.Pattern;
import org.gervarro.democles.specification.emf.PatternBody;
import org.gervarro.democles.specification.emf.SpecificationFactory;
import org.gervarro.democles.specification.emf.constraint.emf.emf.Attribute;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFTypeFactory;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFVariable;
import org.gervarro.democles.specification.emf.constraint.emf.emf.Reference;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraint;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraintFactory;

import IBeXLanguage.IBeXAttributeConstraint;
import IBeXLanguage.IBeXAttributeExpression;
import IBeXLanguage.IBeXAttributeParameter;
import IBeXLanguage.IBeXAttributeValue;
import IBeXLanguage.IBeXConstant;
import IBeXLanguage.IBeXContextPattern;
import IBeXLanguage.IBeXEdge;
import IBeXLanguage.IBeXEnumLiteral;
import IBeXLanguage.IBeXNode;
import IBeXLanguage.IBeXNodePair;

/**
 * Helper which can transform of nodes, edges and attribute constraints of an
 * IBeXPattern to their representation in a Democles pattern.
 */
public class IBeXToDemoclesPatternHelper {
	// Factories from Democles.
	private static final SpecificationFactory democlesSpecificationFactory = SpecificationFactory.eINSTANCE;
	private static final EMFTypeFactory democlesEmfTypeFactory = EMFTypeFactory.eINSTANCE;
	private static final RelationalConstraintFactory democlesRelationalConstraintFactory = RelationalConstraintFactory.eINSTANCE;

	/**
	 * The context pattern form the IBeX model which is transformed.
	 */
	private final IBeXContextPattern ibexPattern;

	/**
	 * The Democles pattern.
	 */
	private final Pattern democlesPattern;

	/**
	 * The body of the Democles pattern.
	 */
	private final PatternBody democlesPatternBody;

	/**
	 * The mapping between nodes from the IBeX model and Democles variables.
	 */
	private final Map<IBeXNode, EMFVariable> nodeToVariable = new HashMap<>();

	/**
	 * Creates a IBeXToDemoclesPatternHelper.
	 * 
	 * @param ibexPattern
	 *            the context pattern
	 * @param democlesPattern
	 *            the Democles pattern
	 */
	public IBeXToDemoclesPatternHelper(final IBeXContextPattern ibexPattern, final Pattern democlesPattern) {
		this.ibexPattern = ibexPattern;
		this.democlesPattern = democlesPattern;
		this.democlesPatternBody = democlesPattern.getBodies().get(0);
	}

	/**
	 * Transforms the nodes to variables and the injectivity constraints and edges
	 * to constraints.
	 */
	public void transform() {
		transformSignatureNodesToSymbolicParameters();
		transformLocalNodesToLocalVariables();
		transformInjectivityConstraints();
		transformLocalEdgesToConstraints();
		transformAttributeConstraints();
	}

	/**
	 * Returns the mapping between IBeXNodes and variables.
	 * 
	 * @return the mapping
	 */
	public Map<IBeXNode, EMFVariable> getNodeToVariableMapping() {
		return nodeToVariable;
	}

	/**
	 * Adds a symbolic parameter for each signature node.
	 */
	private void transformSignatureNodesToSymbolicParameters() {
		for (final IBeXNode ibexSignatureNode : ibexPattern.getSignatureNodes()) {
			nodeToVariable.put(ibexSignatureNode, transformNodeToVariable(ibexSignatureNode));
			democlesPattern.getSymbolicParameters().add(nodeToVariable.get(ibexSignatureNode));
		}
	}

	/**
	 * Adds a local variable for each local node.
	 */
	private void transformLocalNodesToLocalVariables() {
		for (final IBeXNode ibexLocalNode : ibexPattern.getLocalNodes()) {
			nodeToVariable.put(ibexLocalNode, transformNodeToVariable(ibexLocalNode));
			democlesPattern.getBodies().get(0).getLocalVariables().add(nodeToVariable.get(ibexLocalNode));
		}
	}

	/**
	 * Transforms an {@link IBeXNode} to an equivalent {@link EMFVariable}.
	 * 
	 * @param ibexNode
	 *            the IBeXNode to transform
	 * @return the EMFVariable
	 */
	private static EMFVariable transformNodeToVariable(final IBeXNode ibexNode) {
		EMFVariable variable = democlesEmfTypeFactory.createEMFVariable();
		variable.setName(ibexNode.getName());
		variable.setEClassifier(ibexNode.getType());
		return variable;
	}

	/**
	 * Adds a relational constraint (unequal) for each injectivity constraints.
	 */
	private void transformInjectivityConstraints() {
		for (final IBeXNodePair injectivityNodePair : ibexPattern.getInjectivityConstraints()) {
			RelationalConstraint constraint = transformInjectivityToRelationalConstraint(injectivityNodePair);
			democlesPatternBody.getConstraints().add(constraint);
		}
	}

	/**
	 * Transforms an {@link IBeXNodePair} for injectivity to an equivalent
	 * RelationalConstraint.
	 * 
	 * @param injectivityConstraint
	 *            the pair of nodes which must be different
	 * @return the constraint
	 */
	private RelationalConstraint transformInjectivityToRelationalConstraint(final IBeXNodePair injectivityConstraint) {
		RelationalConstraint unequalConstraint = democlesRelationalConstraintFactory.createUnequal();
		injectivityConstraint.getValues().forEach(node -> {
			ConstraintParameter p = democlesSpecificationFactory.createConstraintParameter();
			p.setReference(nodeToVariable.get(node));
			unequalConstraint.getParameters().add(p);
		});
		return unequalConstraint;
	}

	/**
	 * Adds a constraint for each local edge.
	 */
	private void transformLocalEdgesToConstraints() {
		for (final IBeXEdge ibexLocalEdge : ibexPattern.getLocalEdges()) {
			Reference constraint = transformLocalEdgeToReference(ibexLocalEdge);
			democlesPatternBody.getConstraints().add(constraint);
		}
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
	private Reference transformLocalEdgeToReference(final IBeXEdge ibexEdge) {
		// Edge type.
		Objects.requireNonNull(ibexEdge.getType(), "The type of IBeXEdge must not be null!");
		Reference reference = democlesEmfTypeFactory.createReference();
		reference.setEModelElement(ibexEdge.getType());

		// Parameter for the source node.
		Objects.requireNonNull(ibexEdge.getSourceNode(), "The source node of an IBeXEdge must not be null!");
		Objects.requireNonNull(nodeToVariable.get(ibexEdge.getSourceNode()),
				"A mapping for the source node must exist!");
		ConstraintParameter parameterForSourceNode = democlesSpecificationFactory.createConstraintParameter();
		parameterForSourceNode.setReference(nodeToVariable.get(ibexEdge.getSourceNode()));
		reference.getParameters().add(parameterForSourceNode);

		// Parameter for the target node.
		Objects.requireNonNull(ibexEdge.getTargetNode(), "The target node of an IBeXEdge must not be null!");
		Objects.requireNonNull(nodeToVariable.get(ibexEdge.getTargetNode()),
				"A mapping for the target node must exist!");
		ConstraintParameter parameterForTargetNode = democlesSpecificationFactory.createConstraintParameter();
		parameterForTargetNode.setReference(nodeToVariable.get(ibexEdge.getTargetNode()));
		reference.getParameters().add(parameterForTargetNode);

		return reference;
	}

	/**
	 * Transforms the attribute constraints to Democles constraints.
	 */
	private void transformAttributeConstraints() {
		for (final IBeXAttributeConstraint ac : ibexPattern.getAttributeConstraint()) {
			transformAttributeConstraint(ac);
		}
	}

	/**
	 * Transforms the attribute constraint to an attribute and a relational
	 * constraint and adds the created constraints to the pattern body.
	 * 
	 * @param ac
	 *            the attribute constraint
	 * @param body
	 *            the body
	 */
	private void transformAttributeConstraint(final IBeXAttributeConstraint ac) {
		IBeXAttributeValue value = ac.getValue();
		if (value instanceof IBeXAttributeParameter) {
			// Cannot handle parameters as their values are only known at runtime.
			return;
		}

		EMFVariable attributeVariable = DemoclesPatternUtils.addAttributeVariableToBody(ac, democlesPatternBody);
		Attribute attribute = DemoclesPatternUtils.createAttributeConstraint(ac.getType(),
				nodeToVariable.get(ac.getNode()), attributeVariable);
		democlesPatternBody.getConstraints().add(attribute);

		ConstraintVariable valueVariable;
		if (value instanceof IBeXAttributeExpression) {
			valueVariable = DemoclesPatternUtils
					.addConstraintForAttributeExpressionToBody((IBeXAttributeExpression) value, democlesPatternBody);
		} else {
			Object constantValue = null;
			if (value instanceof IBeXConstant) {
				constantValue = ((IBeXConstant) value).getValue();
			} else if (value instanceof IBeXEnumLiteral) {
				constantValue = ((IBeXEnumLiteral) value).getLiteral().getInstance();
			}
			valueVariable = DemoclesPatternUtils.addConstantToBody(constantValue, democlesPatternBody);
		}

		RelationalConstraint relationalConstraint = DemoclesPatternUtils
				.createRelationalConstraintForAttribute(ac.getRelation(), attributeVariable, valueVariable);
		democlesPatternBody.getConstraints().add(relationalConstraint);
	}
}
