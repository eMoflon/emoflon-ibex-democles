package org.emoflon.ibex.tgg.runtime.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.emoflon.ibex.tgg.compiler.patterns.common.IBlackPattern;
import org.emoflon.ibex.tgg.compiler.patterns.common.IbexBasePattern;
import org.emoflon.ibex.tgg.compiler.patterns.common.PatternInvocation;
import org.emoflon.ibex.tgg.operational.defaults.IbexOptions;
import org.gervarro.democles.specification.emf.Constraint;
import org.gervarro.democles.specification.emf.ConstraintParameter;
import org.gervarro.democles.specification.emf.Pattern;
import org.gervarro.democles.specification.emf.PatternBody;
import org.gervarro.democles.specification.emf.PatternInvocationConstraint;
import org.gervarro.democles.specification.emf.SpecificationFactory;
import org.gervarro.democles.specification.emf.Variable;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFTypeFactory;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFVariable;
import org.gervarro.democles.specification.emf.constraint.emf.emf.Reference;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraint;
import org.gervarro.democles.specification.emf.constraint.relational.RelationalConstraintFactory;

import language.TGGRuleNode;

/**
 * Transformation from IBlackPattern to (Democles) Pattern
 */
public class IBlackToDemoclesPatternTransformation {
	private IbexOptions options;
	private HashMap<IBlackPattern, Pattern> patternMap;
	private Collection<Pattern> patterns;
	
	// Factories
	private final SpecificationFactory factory = SpecificationFactory.eINSTANCE;
	private final EMFTypeFactory emfTypeFactory = EMFTypeFactory.eINSTANCE;
	private final RelationalConstraintFactory rcFactory = RelationalConstraintFactory.eINSTANCE;
	
	public IBlackToDemoclesPatternTransformation(IbexOptions options) {
		this.options = options;
		patternMap = new HashMap<>();
		patterns = new ArrayList<>();
	}
	
	public Collection<Pattern> getPatterns() {
		return this.patterns;
	}
	
	public Pattern ibexToDemocles(IBlackPattern ibexPattern) {
		if (patternMap.containsKey(ibexPattern))
			return patternMap.get(ibexPattern);

		// Root pattern
		Pattern pattern = factory.createPattern();
		pattern.setName(ibexPattern.getName());
		PatternBody body = factory.createPatternBody();
		pattern.getBodies().add(body);

		// Parameters
		Map<String, EMFVariable> nodeToVar = new HashMap<>();
		EList<Variable> parameters = pattern.getSymbolicParameters();

		// Extract constraints and fill nodeToVar and parameters
		EList<Constraint> constraints = ibexToDemocles(ibexPattern, body, nodeToVar, parameters);

		// Pattern invocations
		for (PatternInvocation inv : ibexPattern.getPositiveInvocations()) {
			if (patternIsNotEmpty(inv.getInvokedPattern())) {
				PatternInvocationConstraint invCon = createInvocationConstraint(inv, true, nodeToVar);
				if(!invCon.getParameters().isEmpty())
					constraints.add(invCon);
			}
		}

		for (PatternInvocation inv : ibexPattern.getNegativeInvocations()) {
			if (patternIsNotEmpty(inv.getInvokedPattern())) {
				PatternInvocationConstraint invCon = createInvocationConstraint(inv, false, nodeToVar);
				if(!invCon.getParameters().isEmpty())
					constraints.add(invCon);
			}
		}

		patternMap.put(ibexPattern, pattern);
		patterns.add(pattern);

		return pattern;
	}
	
	protected static boolean patternIsNotEmpty(IBlackPattern pattern) {
		return !pattern.getSignatureNodes().isEmpty();
	}

	private EList<Constraint> ibexToDemocles(IBlackPattern ibexPattern, PatternBody body, Map<String, EMFVariable> nodeToVar, EList<Variable> parameters) {		
		createVariablesForNodes(ibexPattern, body, nodeToVar, parameters);
		
		DemoclesAttributeHelper dAttrHelper = new DemoclesAttributeHelper(options);
		dAttrHelper.createAttributeInplaceAttributeConditions(ibexPattern, body, nodeToVar, parameters);
		if (this.options.blackInterpSupportsAttrConstrs())
			dAttrHelper.createAttributeConstraints(ibexPattern, body, nodeToVar, parameters);
		
		createConstraintsForEdges(ibexPattern, nodeToVar, body.getConstraints());
		createUnequalConstraintsForInjectivity(ibexPattern, body, nodeToVar);

		return body.getConstraints();
	}

	private void createVariablesForNodes(IBlackPattern ibexPattern, PatternBody body, Map<String, EMFVariable> nodeToVar, EList<Variable> parameters) {
		// Signature elements
		for (TGGRuleNode element : ibexPattern.getSignatureNodes()) {
			if (!nodeToVar.containsKey(element.getName())) {
				if (element instanceof TGGRuleNode) {
					TGGRuleNode node = (TGGRuleNode) element;
					EMFVariable var = emfTypeFactory.createEMFVariable();
					var.setName(node.getName());
					var.setEClassifier(node.getType());
					nodeToVar.put(node.getName(), var);
				}
			}
			parameters.add(nodeToVar.get(element.getName()));
		}
	
		// All other nodes
		EList<Variable> locals = body.getLocalVariables();
		Collection<TGGRuleNode> allOtherNodes = new ArrayList<>(ibexPattern.getLocalNodes());
		for (TGGRuleNode node : allOtherNodes) {
			if (!nodeToVar.containsKey(node.getName())) {
				EMFVariable var = emfTypeFactory.createEMFVariable();
				var.setName(node.getName());
				var.setEClassifier(node.getType());
				nodeToVar.put(node.getName(), var);
			}
			
			locals.add(nodeToVar.get(node.getName()));
		}
	}

	private void createUnequalConstraintsForInjectivity(IBlackPattern ibexPattern, PatternBody body, Map<String, EMFVariable> nodeToVar) {
		// Force injective matches through unequals-constraints
		forceInjectiveMatchesForPattern((IbexBasePattern) ibexPattern, body, nodeToVar);
	}

	private void createConstraintsForEdges(IBlackPattern ibexPattern, Map<String, EMFVariable> nodeToVar, EList<Constraint> constraints) {
		ibexPattern.getLocalEdges()
			.stream()
			.forEach(edge -> {
				assert(edge.getSrcNode() != null);
				assert(edge.getTrgNode() != null);
				assert(edge.getType() != null);
				assert(nodeToVar.containsKey(edge.getSrcNode().getName()));
				assert(nodeToVar.containsKey(edge.getTrgNode().getName()));
				
				Reference ref = emfTypeFactory.createReference();
				ref.setEModelElement(edge.getType());

				ConstraintParameter from = factory.createConstraintParameter();
				from.setReference(nodeToVar.get(edge.getSrcNode().getName()));
				ref.getParameters().add(from);

				ConstraintParameter to = factory.createConstraintParameter();
				to.setReference(nodeToVar.get(edge.getTrgNode().getName()));
				ref.getParameters().add(to);

				constraints.add(ref);
			});
	}

	private void forceInjectiveMatchesForPattern(IBlackPattern pattern, PatternBody body, Map<String, EMFVariable> nodeToVar) {
		pattern.getInjectivityChecks().stream()
									  .forEach(pair -> {
			RelationalConstraint unequal = rcFactory.createUnequal();

			ConstraintParameter p1 = factory.createConstraintParameter();
			ConstraintParameter p2 = factory.createConstraintParameter();
			unequal.getParameters().add(p1);
			unequal.getParameters().add(p2);
			p1.setReference(nodeToVar.get(pair.getLeft().getName()));
			p2.setReference(nodeToVar.get(pair.getRight().getName()));

			body.getConstraints().add(unequal);
		});
	}

	private PatternInvocationConstraint createInvocationConstraint(PatternInvocation inv, boolean isTrue, Map<String, EMFVariable> nodeToVar) {
			PatternInvocationConstraint invCon = factory.createPatternInvocationConstraint();
			invCon.setPositive(isTrue);
			invCon.setInvokedPattern(ibexToDemocles(inv.getInvokedPattern()));

		for (TGGRuleNode element : inv.getInvokedPattern().getSignatureNodes()) {
			TGGRuleNode invElem = inv.getPreImage(element);
			ConstraintParameter parameter = factory.createConstraintParameter();
			invCon.getParameters().add(parameter);
			parameter.setReference(nodeToVar.get(invElem.getName()));
			assert(parameter.getReference() != null);
		}

		return invCon;
	}
}
