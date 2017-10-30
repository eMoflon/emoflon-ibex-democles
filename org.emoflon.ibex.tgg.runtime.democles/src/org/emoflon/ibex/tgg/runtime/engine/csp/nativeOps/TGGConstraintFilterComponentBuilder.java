package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import java.util.List;
import java.util.Optional;

import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.common.runtime.OperationBuilder;
import org.gervarro.democles.common.runtime.VariableRuntime;
import org.gervarro.democles.plan.incremental.leaf.Component;
import org.gervarro.democles.plan.incremental.leaf.FilterComponent;
import org.gervarro.democles.runtime.InterpretableAdornedOperation;
import org.gervarro.democles.specification.ConstraintType;
import org.gervarro.democles.specification.VariableType;

public class TGGConstraintFilterComponentBuilder<VR extends VariableRuntime> implements OperationBuilder<Component,Component,VR> {
	private final TGGNativeOperationBuilder<VariableRuntime> operationBuilder;
	private final Adornment adornment;
	
	public TGGConstraintFilterComponentBuilder(final TGGNativeOperationBuilder<VariableRuntime> operationBuilder, Adornment adornment) {
		this.operationBuilder = operationBuilder;
		this.adornment = adornment;
	}
	
	@Override
	public final Component getConstraintOperation(final ConstraintType constraint, final List<? extends VR> parameters) {
		List<InterpretableAdornedOperation> ops = operationBuilder.getConstraintOperation(constraint, parameters);
		if(ops != null) {
			Optional<InterpretableAdornedOperation> adornedOperation = ops.stream().filter(op -> op.getPrecondition().equals(adornment)).findAny();
			return adornedOperation.map(op -> new FilterComponent(op, adornment, constraint, parameters))
								   .orElseThrow(() -> new IllegalStateException(ops + " does not contain an operation with adornment " + adornment));
		}
		
		return null;
	}

	@Override
	public final Component getVariableOperation(final VariableType variable, final VR runtimeVariable) {
		return null;
	}
}
