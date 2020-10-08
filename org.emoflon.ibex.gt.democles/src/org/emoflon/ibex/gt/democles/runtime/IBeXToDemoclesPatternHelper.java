package org.emoflon.ibex.gt.democles.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXAttributeConstraint;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXAttributeExpression;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXAttributeValue;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXConstant;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXContextPattern;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXEdge;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXEnumLiteral;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXInjectivityConstraint;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXNode;
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
	public IBeXToDemoclesPatternHelper(final IBeXContextPattern ibexPattern) {
		this.ibexPattern = ibexPattern;

		democlesPattern = democlesSpecificationFactory.createPattern();
		democlesPattern.setName(ibexPattern.getName());

		democlesPatternBody = democlesSpecificationFactory.createPatternBody();
		democlesPattern.getBodies().add(democlesPatternBody);
	}

	/**
	 * Transforms the nodes to variables and the injectivity constraints and edges
	 * to constraints.
	 */
	public Pattern transform() {
		transformSignatureNodesToSymbolicParameters();
		transformLocalNodesToLocalVariables();
		transformInjectivityConstraints();
		transformLocalEdgesToConstraints();
		transformAttributeConstraints();
		return democlesPattern;
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
		for (final IBeXInjectivityConstraint injectivityNodePair : ibexPattern.getInjectivityConstraints()) {
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
	private RelationalConstraint transformInjectivityToRelationalConstraint(final IBeXInjectivityConstraint injectivityConstraint) {
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
		Objects.requireNonNull(ibexEdge.getType(), "The type of IBeXEdge must not be null!");

		Reference reference = democlesEmfTypeFactory.createReference();
		reference.setEModelElement(ibexEdge.getType());

		reference.getParameters().add(createParameter(ibexEdge.getSourceNode()));
		reference.getParameters().add(createParameter(ibexEdge.getTargetNode()));

		return reference;
	}

	/**
	 * Creates a parameter for the given node.
	 * 
	 * @param ibexNode
	 *            the node
	 */
	private ConstraintParameter createParameter(final IBeXNode ibexNode) {
		Objects.requireNonNull(ibexNode, "The node referenced by an IBeXEdge must not be null!");
		Objects.requireNonNull(nodeToVariable.get(ibexNode), "A mapping for the node must exist!");
		ConstraintParameter parameter = democlesSpecificationFactory.createConstraintParameter();
		parameter.setReference(nodeToVariable.get(ibexNode));
		return parameter;
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
		IBeXAttributeValue lhs = ac.getLhs();
		IBeXAttributeValue rhs = ac.getRhs();
		if (!(lhs instanceof IBeXAttributeExpression || lhs instanceof IBeXConstant || lhs instanceof IBeXEnumLiteral)) {
			// Cannot handle parameters or arithmetic expressions as their values are only known at runtime.
			return;
		}
		
		if (!(rhs instanceof IBeXAttributeExpression || rhs instanceof IBeXConstant || rhs instanceof IBeXEnumLiteral)) {
			// Cannot handle parameters or arithmetic expressions as their values are only known at runtime.
			return;
		}

		ConstraintVariable lhsVariable;
		if (lhs instanceof IBeXAttributeExpression) {
			lhsVariable = DemoclesPatternUtils
					.addConstraintForAttributeExpressionToBody((IBeXAttributeExpression) lhs, democlesPatternBody);
		} else {
			Object constantValue = null;
			if (lhs instanceof IBeXConstant) {
				constantValue = ((IBeXConstant) lhs).getValue();
			} else if (lhs instanceof IBeXEnumLiteral) {
				constantValue = ((IBeXEnumLiteral) lhs).getLiteral().getInstance();
			}
			lhsVariable = DemoclesPatternUtils.addConstantToBody(constantValue, democlesPatternBody);
		}
		
		ConstraintVariable rhsVariable;
		if (rhs instanceof IBeXAttributeExpression) {
			rhsVariable = DemoclesPatternUtils
					.addConstraintForAttributeExpressionToBody((IBeXAttributeExpression) rhs, democlesPatternBody);
		} else {
			Object constantValue = null;
			if (rhs instanceof IBeXConstant) {
				constantValue = ((IBeXConstant) rhs).getValue();
			} else if (rhs instanceof IBeXEnumLiteral) {
				constantValue = ((IBeXEnumLiteral) rhs).getLiteral().getInstance();
			}
			rhsVariable = DemoclesPatternUtils.addConstantToBody(constantValue, democlesPatternBody);
		}

		RelationalConstraint relationalConstraint = DemoclesPatternUtils
				.createRelationalConstraintForAttribute(ac.getRelation(), lhsVariable, rhsVariable);
		democlesPatternBody.getConstraints().add(relationalConstraint);
	}
}
