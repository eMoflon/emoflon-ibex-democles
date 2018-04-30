package org.emoflon.ibex.tgg.runtime.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EcorePackage;
import org.emoflon.ibex.tgg.compiler.patterns.common.IBlackPattern;
import org.emoflon.ibex.tgg.compiler.patterns.common.IbexBasePattern;
import org.emoflon.ibex.tgg.operational.defaults.IbexOptions;
import org.gervarro.democles.specification.emf.Constant;
import org.gervarro.democles.specification.emf.Constraint;
import org.gervarro.democles.specification.emf.ConstraintParameter;
import org.gervarro.democles.specification.emf.ConstraintVariable;
import org.gervarro.democles.specification.emf.PatternBody;
import org.gervarro.democles.specification.emf.SpecificationFactory;
import org.gervarro.democles.specification.emf.Variable;
import org.gervarro.democles.specification.emf.constraint.emf.emf.Attribute;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFTypeFactory;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFVariable;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraint;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraintFactory;

import TGGAttributeConstraint.AttributeConstraint;
import TGGAttributeConstraint.TGGAttributeConstraintFactory;
import language.TGGRuleNode;
import language.basic.expressions.TGGAttributeExpression;
import language.basic.expressions.TGGEnumExpression;
import language.basic.expressions.TGGExpression;
import language.basic.expressions.TGGLiteralExpression;
import language.basic.expressions.TGGParamValue;
import language.csp.TGGAttributeConstraint;
import language.inplaceAttributes.TGGAttributeConstraintOperators;
import language.inplaceAttributes.TGGInplaceAttributeExpression;

public class DemoclesAttributeHelper {
	// Maps for attribute constraints
	private Map<Object, Constant> constants;
	private Map<String, EMFVariable> body_attr_vars; 
	private Map<String, EMFVariable> signature_attr_vars; 
	private Map<String, Attribute> attrs;
	private Set<Constraint> ops;	
	
	public DemoclesAttributeHelper(IbexOptions options) {
		constants = new HashMap<>();
		body_attr_vars = new HashMap<>();
		signature_attr_vars = new HashMap<>();
		attrs = new HashMap<>();
		ops = new HashSet<>();
	}

	public void createAttributeInplaceAttributeConditions(IBlackPattern ibexPattern, PatternBody body, Map<String, EMFVariable> nodeToVar, EList<Variable> parameters) {
		createInplaceAttributeConditions(ibexPattern, body, nodeToVar, parameters);
		
		// Transfer to body
		body.getConstraints().addAll(attrs.values());
		body.getConstraints().addAll(ops);
		body.getConstants().addAll(constants.values());
		body.getLocalVariables().addAll(body_attr_vars.values());
		body.getLocalVariables().addAll(signature_attr_vars.values());		
	}
	
	public void createAttributeConstraints(IBlackPattern ibexPattern, PatternBody body, Map<String, EMFVariable> nodeToVar, EList<Variable> parameters) {
		createConstraintsForAttributeConstraints(ibexPattern, body, nodeToVar, parameters);
		
		// Transfer to body
		body.getConstraints().addAll(attrs.values());
		body.getConstraints().addAll(ops);
		body.getConstants().addAll(constants.values());
		body.getLocalVariables().addAll(body_attr_vars.values());
		body.getLocalVariables().addAll(signature_attr_vars.values());
	}

	private void createConstraintsForAttributeConstraints(IBlackPattern pattern, PatternBody body, Map<String, EMFVariable> nodeToVar, EList<Variable> parameters) {
		Collection<TGGAttributeConstraint> extractedConstraints = pattern.getAttributeConstraints();
		extractedConstraints.forEach(constraint -> createAttributeConstraint(constraint, nodeToVar));
	}

	private void createAttributeConstraint(TGGAttributeConstraint constraint, Map<String, EMFVariable> nodeToVar) {
		String id = constraint.getDefinition().getName();
		AttributeConstraint c = TGGAttributeConstraintFactory.eINSTANCE.createAttributeConstraint();
		c.setName(id);
		
		for(TGGParamValue param : constraint.getParameters()) {
			ConstraintParameter parameter = SpecificationFactory.eINSTANCE.createConstraintParameter();
			
			if(param instanceof TGGAttributeExpression) {
				TGGAttributeExpression expr = (TGGAttributeExpression) param;
				TGGRuleNode node = expr.getObjectVar();
				EMFVariable node_var = nodeToVar.get(node.getName());
				
				parameter.setReference(createOrRetrieveAttributeVariable(node, node_var, expr.getAttribute()));
			}
			else if(param instanceof TGGLiteralExpression || param instanceof TGGEnumExpression) {
				EDataType attrType = param.getParameterDefinition().getType();
				TGGExpression expr = (TGGExpression) param;
				parameter.setReference(createOrRetrieveConstant(expr, attrType));
			}
			
			c.getParameters().add(parameter);
		}
		
		ops.add(c);
 	}

	private void createInplaceAttributeConditions(IBlackPattern ibexPattern, PatternBody body, Map<String, EMFVariable> nodeToVar, EList<Variable> parameters) {
		// For every node, create variables for attributes and constants used inplace for the node
		for (String nodeName : nodeToVar.keySet())
			createInplaceAttributeConstraints(ibexPattern.getNode(nodeName), nodeToVar.get(nodeName));
	}

	void createInplaceAttributeConstraints(TGGRuleNode node, EMFVariable nodeVar) {
		// Every attribute expression is of the form:  LHS OP RHS ==> <node>.variable OP Constant (e.g., p.id > 5)
		for (TGGInplaceAttributeExpression attrExpr : node.getAttrExpr())
			createVariableConstantAndConstraint(node, nodeVar, attrExpr);
	}

	private void createVariableConstantAndConstraint(TGGRuleNode node, EMFVariable nodeVar, TGGInplaceAttributeExpression attrExpr) {
		EMFVariable attr_var = createOrRetrieveAttributeVariable(node, nodeVar, attrExpr.getAttribute());
		Constant rhs = createOrRetrieveConstant(attrExpr);
		RelationalConstraint op = extractRelationalConstraint(attr_var, rhs, attrExpr.getOperator());
		ops.add(op);
	}

	private EMFVariable createOrRetrieveAttributeVariable(TGGRuleNode node, EMFVariable nodeVar, EAttribute eAttr) {
		boolean isAttributeFree = nodeVar == null;
		
		String key = IbexBasePattern.getVarName(node, eAttr);
		
		if(signature_attr_vars.containsKey(key))
			return signature_attr_vars.get(key);
		if(body_attr_vars.containsKey(key))
			return body_attr_vars.get(key);
		
		EMFVariable var = EMFTypeFactory.eINSTANCE.createEMFVariable();
		var.setEClassifier(eAttr.getEAttributeType());
		var.setName(key);

		// Check if this variable is a local variable in the constraint or corresponds to the attribute value of the TGG node
		if(isAttributeFree) {
			signature_attr_vars.put(key, var);
		}
		else {
			Attribute attr = extractAttribute(nodeVar, var, eAttr);
			attrs.put(key, attr);
			body_attr_vars.put(key, var);
		}

		return var;
	}
	
	private Constant createOrRetrieveConstant(TGGInplaceAttributeExpression attrExpr) {
		TGGExpression expr = attrExpr.getValueExpr();
		EDataType attrType = attrExpr.getAttribute().getEAttributeType();
		return createOrRetrieveConstant(expr, attrType);
	}

	private Constant createOrRetrieveConstant(TGGExpression expr, EDataType attrType) {
		Optional<Object> value = Optional.empty();
		if (expr instanceof TGGLiteralExpression) {
			TGGLiteralExpression tle = (TGGLiteralExpression) expr;
			value = Optional.of(convertLiteral(tle.getValue(), attrType));
		} else if (expr instanceof TGGEnumExpression) {
			value = Optional.of(((TGGEnumExpression) expr).getLiteral());
		} else
			throw new IllegalStateException("The RHS of an attribute expression can only be an enum or constant: " + expr);
		
		Optional<Constant> constant =  value.map(v -> {
			if(constants.containsKey(v))
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

	private RelationalConstraint extractRelationalConstraint(ConstraintVariable from, ConstraintVariable to, TGGAttributeConstraintOperators operator) {
		RelationalConstraint constraint;
		switch (operator) {
		case EQUAL:
			constraint = RelationalConstraintFactory.eINSTANCE.createEqual();
			break;
		case GR_EQUAL:
			constraint = RelationalConstraintFactory.eINSTANCE.createLargerOrEqual();
			break;
		case GREATER:
			constraint = RelationalConstraintFactory.eINSTANCE.createLarger();
			break;
		case LE_EQUAL:
			constraint = RelationalConstraintFactory.eINSTANCE.createSmallerOrEqual();
			break;
		case LESSER:
			constraint = RelationalConstraintFactory.eINSTANCE.createSmaller();
			break;
		case UNEQUAL:
			constraint = RelationalConstraintFactory.eINSTANCE.createUnequal();
			break;
		default:
			return null;
		}
		
		ConstraintParameter parameter = SpecificationFactory.eINSTANCE.createConstraintParameter();
		constraint.getParameters().add(parameter);
		parameter.setReference(from);
		ConstraintParameter parameter2 = SpecificationFactory.eINSTANCE.createConstraintParameter();
		constraint.getParameters().add(parameter2);
		parameter2.setReference(to);
		return constraint;
	}
	
	private Object convertLiteral(String literal, EDataType type) {
		if(type.equals(EcorePackage.Literals.ESTRING) ) {
			if(!(literal.startsWith("\"") && literal.endsWith("\""))) 
				throw new RuntimeException("Trimming of the string did not work. Your string should start and end with \"");
			return literal.substring(1, literal.length() - 1);
		}
		if(type.equals(EcorePackage.Literals.EINT) ) {
			return Integer.parseInt(literal);
		}
		if(type.equals(EcorePackage.Literals.EFLOAT) ) {
			return Float.parseFloat(literal);
		}
		if(type.equals(EcorePackage.Literals.EDOUBLE) ) {
			return Double.parseDouble(literal);
		}
		if(type.equals(EcorePackage.Literals.EBOOLEAN) ) {
			return Boolean.parseBoolean(literal);
		}
		if(type.equals(EcorePackage.Literals.ECHAR) ) {
			if(!(literal.startsWith("\'") && literal.endsWith("\'"))) 
				throw new RuntimeException("Trimming of the char did not work. Your string should start and end with \'");

			return literal.length() < 2 ? '\0' : literal.charAt(1);
		}
		if(type.equals(EcorePackage.Literals.ELONG) ) {
			return Long.parseLong(literal);
		}
		
		throw new RuntimeException(type + " is not yet supported as a Datatype");
	}
}