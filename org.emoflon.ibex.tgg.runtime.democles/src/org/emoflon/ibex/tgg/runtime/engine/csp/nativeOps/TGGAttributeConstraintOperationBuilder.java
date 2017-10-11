package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import java.util.List;

import org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.operations.TGGAttributeNativeOperation;
import org.gervarro.democles.common.runtime.OperationBuilder;
import org.gervarro.democles.common.runtime.VariableRuntime;
import org.gervarro.democles.runtime.NativeOperation;
import org.gervarro.democles.specification.ConstraintType;
import org.gervarro.democles.specification.VariableType;

public class TGGAttributeConstraintOperationBuilder <VR extends VariableRuntime> implements OperationBuilder<NativeOperation, NativeOperation, VR> {
	private TGGAttributeNativeOperation op;
	
	public TGGAttributeConstraintOperationBuilder(TGGAttributeNativeOperation op) {
		this.op = op;
	}
	
	@Override
	public final TGGAttributeNativeOperation getConstraintOperation(final ConstraintType constraint, final List<? extends VR> parameters) {
		if (constraint instanceof TGGConstraintType) {
			if (constraint == op.getConstraintType()) {
				return op;
			}
		}
		return null;
	}

	@Override
	public final TGGAttributeNativeOperation getVariableOperation(final VariableType variable, final VR runtimeVariable) {
		return null;
	}
}
