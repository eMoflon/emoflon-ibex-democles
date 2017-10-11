package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import java.util.List;

import org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.operations.EqStrNativeOperation;
import org.gervarro.democles.common.runtime.OperationBuilder;
import org.gervarro.democles.common.runtime.VariableRuntime;
import org.gervarro.democles.runtime.NativeOperation;
import org.gervarro.democles.specification.ConstraintType;
import org.gervarro.democles.specification.VariableType;

public class TGGAttributeConstraintOperationBuilder <VR extends VariableRuntime>
implements OperationBuilder<NativeOperation, NativeOperation, VR> {
	
	private static EqStrNativeOperation eq_Str_Native = new EqStrNativeOperation();
	
	@Override
	public final NativeOperation getConstraintOperation(final ConstraintType constraint, final List<? extends VR> parameters) {
		if (constraint instanceof TGGConstraintType) {
			if (constraint == TGGAttributeConstraintModule.EQ_STRING) {
				return eq_Str_Native;
			}
		}
		return null;
	}

	@Override
	public final NativeOperation getVariableOperation(final VariableType variable, final VR runtimeVariable) {
		return null;
	}
}
