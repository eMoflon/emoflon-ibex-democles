package org.emoflon.ibex.tgg.runtime.engine.csp.interpretableAdornedOps;

import java.util.List;

import org.emoflon.ibex.tgg.runtime.engine.csp.interpretableAdornedOps.operations.EqStrOperation;
import org.gervarro.democles.common.runtime.OperationBuilder;
import org.gervarro.democles.common.runtime.VariableRuntime;
import org.gervarro.democles.runtime.InterpretableAdornedOperation;
import org.gervarro.democles.specification.ConstraintType;
import org.gervarro.democles.specification.VariableType;

public class TGGAttributeConstraintOperationBuilder <VR extends VariableRuntime>
implements OperationBuilder<InterpretableAdornedOperation, List<InterpretableAdornedOperation>, VR> {
	
	@Override
	public final List<InterpretableAdornedOperation> getConstraintOperation(final ConstraintType constraint, final List<? extends VR> parameters) {
		if (constraint instanceof TGGConstraintType) {
			if (constraint == TGGAttributeConstraintModule.EQ_STRING) {
				return EqStrOperation.getAllOperations();
			}
		}
		return null;
	}

	@Override
	public final InterpretableAdornedOperation getVariableOperation(final VariableType variable, final VR runtimeVariable) {
		return null;
	}
}
