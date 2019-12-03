package org.emoflon.ibex.tgg.runtime.democles;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EDataType;
import org.emoflon.ibex.tgg.operational.defaults.IbexOptions;
import org.emoflon.ibex.tgg.util.String2EPrimitive;
import org.gervarro.democles.specification.emf.Constant;
import org.gervarro.democles.specification.emf.Constraint;
import org.gervarro.democles.specification.emf.ConstraintParameter;
import org.gervarro.democles.specification.emf.PatternBody;
import org.gervarro.democles.specification.emf.SpecificationFactory;
import org.gervarro.democles.specification.emf.Variable;
import org.gervarro.democles.specification.emf.constraint.emf.emf.Attribute;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFTypeFactory;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFVariable;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraint;

import TGGAttributeConstraint.AttributeConstraint;
import TGGAttributeConstraint.TGGAttributeConstraintFactory;
import language.TGGAttributeConstraint;
import language.TGGAttributeExpression;
import language.TGGEnumExpression;
import language.TGGExpression;
import language.TGGLiteralExpression;
import language.TGGParamValue;
import language.TGGRuleNode;

public class DemoclesAttributeHelper {
	// Maps for attribute constraints
	private Map<Object, Constant> constants;
	private Map<String, EMFVariable> body_attr_vars;
	private Map<String, EMFVariable> signature_attr_vars;
	private Map<String, Attribute> attrs;
	private Set<Constraint> ops;

	public DemoclesAttributeHelper(IbexOptions options, PatternBody body) {
		constants = new HashMap<>();
		body.getConstants()//
				.stream()//
				.forEach(c -> constants.put(c.getValue(), c));

		ops = body.getConstraints()//
				.stream()//
				.filter(RelationalConstraint.class::isInstance)//
				.collect(Collectors.toSet());

		attrs = new HashMap<>();
		body_attr_vars = new HashMap<>();
		body.getConstraints()//
				.stream()//
				.filter(Attribute.class::isInstance)//
				.map(Attribute.class::cast)//
				.forEach(attr -> {
					EMFVariable to = (EMFVariable) attr.getParameters().get(1).getReference();
					attrs.put(to.getName(), attr);
					body_attr_vars.put(to.getName(), to);
				});

		// Certainly empty as there are no local attribute variables
		signature_attr_vars = new HashMap<>();
	}

	public void createAttributeConstraints(Collection<TGGAttributeConstraint> extractedConstraints, PatternBody body,
			Map<String, EMFVariable> nodeToVar, EList<Variable> parameters) {
		createConstraintsForAttributeConstraints(extractedConstraints, body, nodeToVar, parameters);

		// Transfer to body
		body.getConstraints().addAll(attrs.values());
		body.getConstraints().addAll(ops);
		body.getConstants().addAll(constants.values());
		body.getLocalVariables().addAll(body_attr_vars.values());
		body.getLocalVariables().addAll(signature_attr_vars.values());
	}

	private void createConstraintsForAttributeConstraints(Collection<TGGAttributeConstraint> extractedConstraints,
			PatternBody body, Map<String, EMFVariable> nodeToVar, EList<Variable> parameters) {
		extractedConstraints.forEach(constraint -> createAttributeConstraint(constraint, nodeToVar));
	}

	private void createAttributeConstraint(TGGAttributeConstraint constraint, Map<String, EMFVariable> nodeToVar) {
		String id = constraint.getDefinition().getName();
		AttributeConstraint c = TGGAttributeConstraintFactory.eINSTANCE.createAttributeConstraint();
		c.setName(id);

		for (TGGParamValue param : constraint.getParameters()) {
			ConstraintParameter parameter = SpecificationFactory.eINSTANCE.createConstraintParameter();

			if (param instanceof TGGAttributeExpression) {
				TGGAttributeExpression expr = (TGGAttributeExpression) param;
				TGGRuleNode node = expr.getObjectVar();
				EMFVariable node_var = nodeToVar.get(node.getName());

				parameter.setReference(createOrRetrieveAttributeVariable(node, node_var, expr.getAttribute()));
			} else if (param instanceof TGGLiteralExpression || param instanceof TGGEnumExpression) {
				EDataType attrType = param.getParameterDefinition().getType();
				TGGExpression expr = (TGGExpression) param;
				parameter.setReference(createOrRetrieveConstant(expr, attrType));
			}

			c.getParameters().add(parameter);
		}

		ops.add(c);
	}

	private final static String getVarName(String nodeName, EAttribute attr) {
		return "__" + nodeName + "__" + attr.getName() + "__";
	}

	private EMFVariable createOrRetrieveAttributeVariable(TGGRuleNode node, EMFVariable nodeVar, EAttribute eAttr) {
		boolean isAttributeFree = nodeVar == null;

		String key = getVarName(node.getName(), eAttr);

		if (signature_attr_vars.containsKey(key))
			return signature_attr_vars.get(key);
		if (body_attr_vars.containsKey(key))
			return body_attr_vars.get(key);

		EMFVariable var = EMFTypeFactory.eINSTANCE.createEMFVariable();
		var.setEClassifier(eAttr.getEAttributeType());
		var.setName(key);

		// Check if this variable is a local variable in the constraint or corresponds
		// to the attribute value of the TGG node
		if (isAttributeFree) {
			signature_attr_vars.put(key, var);
		} else {
			Attribute attr = extractAttribute(nodeVar, var, eAttr);
			attrs.put(key, attr);
			body_attr_vars.put(key, var);
		}

		return var;
	}

	private Constant createOrRetrieveConstant(TGGExpression expr, EDataType attrType) {
		Optional<Object> value = Optional.empty();
		if (expr instanceof TGGLiteralExpression) {
			TGGLiteralExpression tle = (TGGLiteralExpression) expr;
			value = Optional.of(String2EPrimitive.convertLiteral(tle.getValue(), attrType));
		} else if (expr instanceof TGGEnumExpression) {
			value = Optional.of(((TGGEnumExpression) expr).getLiteral().getInstance());
		} else
			throw new IllegalStateException(
					"The RHS of an attribute expression can only be an enum or constant: " + expr);

		Optional<Constant> constant = value.map(v -> {
			if (constants.containsKey(v))
				return constants.get(v);
			else {
				Constant c = SpecificationFactory.eINSTANCE.createConstant();
				c.setValue(v);
				constants.put(v, c);
				return c;
			}
		});

		return constant.orElseThrow(() -> new IllegalStateException("Unable to extract constant from: " + expr));
	}

	private Attribute extractAttribute(EMFVariable from, EMFVariable to, EAttribute attribute) {
		Attribute attr = EMFTypeFactory.eINSTANCE.createAttribute();
		ConstraintParameter parameter = SpecificationFactory.eINSTANCE.createConstraintParameter();
		attr.getParameters().add(parameter);
		parameter.setReference(from);
		ConstraintParameter parameter2 = SpecificationFactory.eINSTANCE.createConstraintParameter();
		attr.getParameters().add(parameter2);
		parameter2.setReference(to);
		attr.setEModelElement(attribute);
		return attr;
	}
}