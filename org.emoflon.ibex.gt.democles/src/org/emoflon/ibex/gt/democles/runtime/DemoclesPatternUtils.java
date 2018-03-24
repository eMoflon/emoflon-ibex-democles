package org.emoflon.ibex.gt.democles.runtime;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EAttribute;
import org.gervarro.democles.specification.emf.Constant;
import org.gervarro.democles.specification.emf.ConstraintParameter;
import org.gervarro.democles.specification.emf.ConstraintVariable;
import org.gervarro.democles.specification.emf.PatternBody;
import org.gervarro.democles.specification.emf.SpecificationFactory;
import org.gervarro.democles.specification.emf.constraint.emf.emf.Attribute;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFTypeFactory;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFVariable;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraint;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraintFactory;

import IBeXLanguage.IBeXAttributeConstraint;
import IBeXLanguage.IBeXRelation;

/**
 * Utility methods for creating Democles patterns.
 */
public class DemoclesPatternUtils {
	// Factories from Democles.
	private static final SpecificationFactory democlesSpecificationFactory = SpecificationFactory.eINSTANCE;
	private static final EMFTypeFactory democlesEmfTypeFactory = EMFTypeFactory.eINSTANCE;
	private static final RelationalConstraintFactory democlesRelationalConstraintFactory = RelationalConstraintFactory.eINSTANCE;

	/**
	 * Creates an attribute constraint for the given node and attribute variable.
	 * 
	 * @param attribute
	 *            the attribute type
	 * @param nodeVariable
	 *            the variable for the node
	 * @param attributeVariable
	 *            the variable for the attribute
	 * @return the attribute constraint
	 */
	public static Attribute createAttributeConstraint(final EAttribute attribute, final ConstraintVariable nodeVariable,
			final ConstraintVariable attributeVariable) {
		Attribute attributeConstraint = democlesEmfTypeFactory.createAttribute();
		attributeConstraint.setEModelElement(attribute);

		ConstraintParameter parameterForNode = democlesSpecificationFactory.createConstraintParameter();
		attributeConstraint.getParameters().add(parameterForNode);
		parameterForNode.setReference(nodeVariable);

		ConstraintParameter parameterForAttribute = democlesSpecificationFactory.createConstraintParameter();
		attributeConstraint.getParameters().add(parameterForAttribute);
		parameterForAttribute.setReference(attributeVariable);

		return attributeConstraint;
	}

	/**
	 * Adds a variable with the given name and attribute type to the body if such a
	 * variable does not exist yet. Otherwise, the existing variable is returned.
	 * 
	 * @param ac
	 *            the attribute constraint whose attribute to create a variable for
	 * @param body
	 *            the pattern body
	 * @return the variable for the attribute
	 */
	public static EMFVariable addAttributeVariableToBody(final IBeXAttributeConstraint ac, final PatternBody body) {
		Objects.requireNonNull(ac, "The attribute constraint must not be null!");
		Objects.requireNonNull(body, "The pattern body must not be null!");

		String name = ac.getNode().getName() + "__" + ac.getType().getName();
		Optional<EMFVariable> existingAttributeVariable = body.getLocalVariables().stream()
				.filter(v -> v instanceof EMFVariable).map(v -> (EMFVariable) v).filter(v -> name.equals(v.getName()))
				.findAny();
		if (existingAttributeVariable.isPresent()) {
			return existingAttributeVariable.get();
		} else {
			EMFVariable attributeVariable = EMFTypeFactory.eINSTANCE.createEMFVariable();
			attributeVariable.setEClassifier(ac.getType().getEAttributeType());
			attributeVariable.setName(name);
			body.getLocalVariables().add(attributeVariable);
			return attributeVariable;
		}
	}

	/**
	 * If there is no constant for the given value yet, a constant with the given
	 * value is added to the pattern body. Otherwise the existing constant is
	 * returned.
	 * 
	 * @param constantValue
	 *            the value for the constant
	 * @param body
	 *            the pattern body
	 * @return the constant
	 */
	public static Constant addConstantToBody(final Object constantValue, final PatternBody body) {
		Objects.requireNonNull(constantValue, "The constant must not be null!");
		Objects.requireNonNull(body, "The pattern body must not be null!");

		Optional<Constant> existingConstant = body.getConstants().stream()
				.filter(c -> c.getValue().equals(constantValue)).findAny();
		if (existingConstant.isPresent()) {
			return existingConstant.get();
		}

		Constant constant = SpecificationFactory.eINSTANCE.createConstant();
		constant.setValue(constantValue);
		body.getConstants().add(constant);
		return constant;
	}

	/**
	 * Creates a relational constraint.
	 * 
	 * @param relation
	 *            the relation
	 * @param attributeVariable
	 *            the variable for the attribute
	 * @param attributeValueVariable
	 *            the variable for the attribute
	 * @return the constraint for the relation between attribute values
	 */
	public static RelationalConstraint createRelationalConstraintForAttribute(final IBeXRelation relation,
			final ConstraintVariable attributeVariable, final ConstraintVariable attributeValueVariable) {
		RelationalConstraint constraint = getRelationalConstraintForRelation(relation);

		ConstraintParameter parameterForAttribute = SpecificationFactory.eINSTANCE.createConstraintParameter();
		constraint.getParameters().add(parameterForAttribute);
		parameterForAttribute.setReference(attributeVariable);

		ConstraintParameter parameterForConstant = SpecificationFactory.eINSTANCE.createConstraintParameter();
		constraint.getParameters().add(parameterForConstant);
		parameterForConstant.setReference(attributeValueVariable);

		return constraint;
	}

	/**
	 * Returns an relational constraint for the given relation.
	 * 
	 * @param relation
	 *            the relation
	 * @return the empty relational constraint
	 */
	private static RelationalConstraint getRelationalConstraintForRelation(IBeXRelation relation) {
		RelationalConstraint constraint;
		switch (relation) {
		case EQUAL:
			constraint = democlesRelationalConstraintFactory.createEqual();
			break;
		case GREATER_OR_EQUAL:
			constraint = democlesRelationalConstraintFactory.createLargerOrEqual();
			break;
		case GREATER:
			constraint = democlesRelationalConstraintFactory.createLarger();
			break;
		case SMALLER:
			constraint = democlesRelationalConstraintFactory.createSmallerOrEqual();
			break;
		case SMALLER_OR_EQUAL:
			constraint = democlesRelationalConstraintFactory.createSmaller();
			break;
		case UNEQUAL:
			constraint = democlesRelationalConstraintFactory.createUnequal();
			break;
		default:
			throw new IllegalArgumentException("Illegal relation type " + relation);
		}
		return constraint;
	}
}
