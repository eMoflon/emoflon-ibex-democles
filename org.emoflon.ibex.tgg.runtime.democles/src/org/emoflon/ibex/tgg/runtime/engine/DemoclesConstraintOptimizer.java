package org.emoflon.ibex.tgg.runtime.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import language.TGGRuleEdge;
import language.TGGRuleNode;
import language.basic.expressions.TGGLiteralExpression;
import language.inplaceAttributes.TGGInplaceAttributeExpression;

public class DemoclesConstraintOptimizer {

	/**
	 * This method takes a pair of nodes which potentially need an unequal-constraint
	 * and checks if this is really necessary.
	 * @param nodes The pair of nodes between which a unequal-constraint might be necessary.
	 * @return true, if the pair requires an unequal-constraint.
	 */
	public boolean unequalConstraintNecessary(Pair<TGGRuleNode, TGGRuleNode> nodes) {
		return !equalConstantAttributeValues(nodes)
			&& !transitiveContainment(nodes)
			&& !differentContainmentSubTrees(nodes);
	}

	private boolean equalConstantAttributeValues(Pair<TGGRuleNode, TGGRuleNode> nodes) {
		for (TGGInplaceAttributeExpression attrExprLeft : nodes.getLeft().getAttrExpr()) {
			if (attrExprLeft.getValueExpr() instanceof TGGLiteralExpression)
				for (TGGInplaceAttributeExpression attrExprRight : nodes.getRight().getAttrExpr()) {
					if (attrExprRight.getValueExpr() instanceof TGGLiteralExpression && attrExprLeft.getAttribute().equals(attrExprRight.getAttribute()))
						if (((TGGLiteralExpression)attrExprLeft.getValueExpr()).getValue().equals(((TGGLiteralExpression)attrExprRight.getValueExpr()).getValue())) {
							return true;
						}
				}
		}
		
		return false;
	}

	private boolean transitiveContainment(Pair<TGGRuleNode, TGGRuleNode> nodes) {
		List<TGGRuleNode> leftHierarchy = containmentHierarchyFromNodeToRoot(nodes.getLeft());
		List<TGGRuleNode> rightHierarchy = containmentHierarchyFromNodeToRoot(nodes.getRight());
		
		for (TGGRuleNode left : leftHierarchy) {
			if (left.equals(nodes.getRight()))
				return true;
		}
		for (TGGRuleNode right : rightHierarchy) {
			if (right.equals(nodes.getLeft()))
				return true;
		}
		
		return false;
	}

	private boolean differentContainmentSubTrees(Pair<TGGRuleNode, TGGRuleNode> nodes) {
		List<TGGRuleNode> leftHierarchy = containmentHierarchyFromNodeToRoot(nodes.getLeft());
		List<TGGRuleNode> rightHierarchy = containmentHierarchyFromNodeToRoot(nodes.getRight());
		
		// if one of the nodes is the root of its tree, both nodes cannot be in different sub-trees
		if (leftHierarchy.size() <= 1 || rightHierarchy.size() <= 1)
			return false;
		
		// if the roots of both nodes' trees are not equal, they are in unconnected trees,
		// which makes unequal-constraints necessary
		if (!leftHierarchy.get(leftHierarchy.size()-1).equals(rightHierarchy.get(rightHierarchy.size()-1)))
			return false;

		// if the parents of both nodes are equal,
		// they have to take care of checking unequality
		if (leftHierarchy.get(1).equals(rightHierarchy.get(1)))
			return false;
		
		// in the remaining cases, the nodes are in different but connected sub-trees
		// and their parent nodes transitively take care of the unequality
		return true;
	}
	
	private List<TGGRuleNode> containmentHierarchyFromNodeToRoot(TGGRuleNode node) {
		List<TGGRuleNode> hierarchy = new ArrayList<>();
		Optional<TGGRuleNode> possibleCurrentNode = Optional.of(node);
		
		while (possibleCurrentNode.isPresent()) {
			TGGRuleNode currentNode = possibleCurrentNode.get();
			hierarchy.add(currentNode);
			
			Optional<TGGRuleEdge> possibleContainmentEdge = currentNode.getIncomingEdges().stream()
																	.filter(e -> e.getType().isContainment())
																	.findAny();

			possibleCurrentNode = possibleContainmentEdge.map(e -> e.getSrcNode());
		}
		
		return hierarchy;
	}
	
}
