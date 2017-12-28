package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoflon.ibex.tgg.operational.csp.constraints.factories.RuntimeTGGAttrConstraintProvider;
import org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.operations.TGGAttributeNativeOperation;
import org.gervarro.democles.common.runtime.OperationBuilder;
import org.gervarro.democles.common.runtime.VariableRuntime;
import org.gervarro.democles.runtime.NativeOperation;
import org.gervarro.democles.specification.ConstraintType;
import org.gervarro.democles.specification.VariableType;

public class TGGNativeOperationBuilder<VR extends VariableRuntime>
		implements OperationBuilder<NativeOperation,NativeOperation,VR> {
	private final RuntimeTGGAttrConstraintProvider attrConstrProvider;

	// Caching maps
	private final Map<String,TGGAttributeNativeOperation> constraintTypeMapping =
			new HashMap<String,TGGAttributeNativeOperation>();

	public TGGNativeOperationBuilder(
			final RuntimeTGGAttrConstraintProvider tggAttributeConstraintProvider) {
		this.attrConstrProvider = tggAttributeConstraintProvider;
	}
	
	public TGGAttributeNativeOperation getVariableOperation(final VariableType variableType,
			final VR variableRuntime) {
		return null;
	}
	
	public TGGAttributeNativeOperation getConstraintOperation(final ConstraintType constraintType,
			final List<? extends VR> parameters) {
		if (constraintType instanceof TGGConstraintType) {
			final TGGConstraintType tggConstraintType =	(TGGConstraintType) constraintType;
			final String id = tggConstraintType.getID();
			TGGAttributeNativeOperation nativeOperation = constraintTypeMapping.get(id);
			if (nativeOperation == null) {
				nativeOperation = new TGGAttributeNativeOperation(id, attrConstrProvider);
				constraintTypeMapping.put(id, nativeOperation);
			}
			return nativeOperation;
		}
		return null;
	}
}