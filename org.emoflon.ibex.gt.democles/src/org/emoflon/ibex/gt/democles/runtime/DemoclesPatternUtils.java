package org.emoflon.ibex.gt.democles.runtime;

import org.eclipse.emf.ecore.EAttribute;

import org.gervarro.democles.specification.emf.ConstraintParameter;
import org.gervarro.democles.specification.emf.ConstraintVariable;
import org.gervarro.democles.specification.emf.SpecificationFactory;
import org.gervarro.democles.specification.emf.constraint.emf.emf.Attribute;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFTypeFactory;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraint;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraintFactory;

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
