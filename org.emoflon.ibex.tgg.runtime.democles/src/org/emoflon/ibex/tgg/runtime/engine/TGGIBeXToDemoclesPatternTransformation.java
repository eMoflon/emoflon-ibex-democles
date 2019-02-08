package org.emoflon.ibex.tgg.runtime.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.emoflon.ibex.common.patterns.IBeXPatternUtils;
import org.emoflon.ibex.gt.democles.runtime.IBeXToDemoclesPatternHelper;
import org.emoflon.ibex.gt.democles.runtime.IBeXToDemoclesPatternTransformation;
import org.emoflon.ibex.tgg.operational.csp.sorting.SearchPlanAction;
import org.emoflon.ibex.tgg.operational.defaults.IbexOptions;
import org.gervarro.democles.specification.emf.Pattern;
import org.gervarro.democles.specification.emf.PatternBody;
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFVariable;

import IBeXLanguage.IBeXContextPattern;
import language.NAC;
import language.TGGAttributeConstraint;
import language.TGGAttributeConstraintLibrary;
import language.TGGNamedElement;
import language.TGGRule;

import org.emoflon.ibex.tgg.operational.monitoring.*;

public class TGGIBeXToDemoclesPatternTransformation extends IBeXToDemoclesPatternTransformation {
	
	ObservableOperation observableOperation = new ObservableOperation();
	GeneratedPatternsSizeObserver generatedPatternsSizeObserver = new GeneratedPatternsSizeObserver(observableOperation);
	
	private IbexOptions options;
	private Map<IBeXContextPattern, TGGNamedElement> patternToRuleMap;

	public TGGIBeXToDemoclesPatternTransformation(IbexOptions options,
			Map<IBeXContextPattern, TGGNamedElement> patternToRuleMap) {
		this.options = options;
		this.patternToRuleMap = patternToRuleMap;

	}

	@Override
	protected Pattern transformPattern(final IBeXContextPattern ibexPattern) {
		generatedPatternsSizeObserver.setEdges(ibexPattern);
		if (patternMap.containsKey(ibexPattern.getName())) {
			return patternMap.get(ibexPattern.getName());
		}

		// Transform nodes, edges, injectivity and attribute constraints.
		IBeXToDemoclesPatternHelper patternHelper = new IBeXToDemoclesPatternHelper(ibexPattern);
		Pattern democlesPattern = patternHelper.transform();

		// Handle TGG attribute constraints
		if (options.blackInterpSupportsAttrConstrs()) {
			PatternBody body = democlesPattern.getBodies().get(0);
			DemoclesAttributeHelper helper = new DemoclesAttributeHelper(options, body);
			TGGNamedElement tggElement = patternToRuleMap.get(ibexPattern);
			if (tggElement != null) {
				Map<String, EMFVariable> nameToVar = new HashMap<>();
				patternHelper.getNodeToVariableMapping().keySet()
						.forEach(k -> nameToVar.put(k.getName(), patternHelper.getNodeToVariableMapping().get(k)));
				helper.createAttributeConstraints(getAttributeConstraintsForPattern(tggElement, ibexPattern), body,
						nameToVar, democlesPattern.getSymbolicParameters());
			}
		}

		// Transform each invocations to a PatternInvocationConstraint.
		transformPatternInvocations(ibexPattern, democlesPattern, patternHelper.getNodeToVariableMapping());

		// Add to patterns.
		patternMap.put(ibexPattern.getName(), democlesPattern);
		democlesPatterns.add(democlesPattern);
		return democlesPattern;
	}

	private Collection<TGGAttributeConstraint> getAttributeConstraintsForPattern(TGGNamedElement tggElement,
			IBeXContextPattern pattern) {
		TGGAttributeConstraintLibrary library = null;

		if (tggElement instanceof TGGRule) {
			TGGRule rule = (TGGRule) tggElement;
			assert (rule != null && rule.getAttributeConditionLibrary() != null);
			library = rule.getAttributeConditionLibrary();
		} else if (tggElement instanceof NAC) {
			NAC nac = (NAC) tggElement;
			library = nac.getAttributeConditionLibrary();
		}

		Collection<TGGAttributeConstraint> attributeConstraints = library.getTggAttributeConstraints();

		return attributeConstraints//
				.stream()//
				.filter(c -> isBlackAttributeConstraintInPattern(c, pattern))//
				.collect(Collectors.toList());
	}

	private boolean isBlackAttributeConstraintInPattern(TGGAttributeConstraint constraint, IBeXContextPattern pattern) {
		return constraint.getParameters()//
				.stream()//
				.allMatch(p -> SearchPlanAction.isConnectedToPattern(p, n -> IBeXPatternUtils.getAllNodes(pattern)//
						.stream()//
						.anyMatch(node -> node.getName().contentEquals(n))));
	}
}
