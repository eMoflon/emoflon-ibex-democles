package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import java.util.List;

import org.gervarro.democles.common.runtime.OperationBuilder;
import org.gervarro.democles.common.runtime.VariableRuntime;
import org.gervarro.democles.plan.incremental.leaf.Component;
import org.gervarro.democles.plan.incremental.leaf.FilterComponent;
import org.gervarro.democles.runtime.InterpretableAdornedOperation;
import org.gervarro.democles.specification.ConstraintType;
import org.gervarro.democles.specification.VariableType;

public class TGGConstraintFilterComponentBuilder<VR extends VariableRuntime> implements OperationBuilder<Component,Component,VR> {
	private final TGGNativeOperationBuilder<VariableRuntime> operationBuilder;
	
	public TGGConstraintFilterComponentBuilder(final TGGNativeOperationBuilder<VariableRuntime> operationBuilder) {
		this.operationBuilder = operationBuilder;
	}
	
	@Override
	public final Component getConstraintOperation(final ConstraintType constraint, final List<? extends VR> parameters) {
		final List<InterpretableAdornedOperation> adornedOperations =
				operationBuilder.getConstraintOperation(constraint, parameters);
		if (adornedOperations != null) {
			for (final InterpretableAdornedOperation adornedOperation : adornedOperations) {
				return new FilterComponent(adornedOperation, adornedOperation.getPrecondition(),
						constraint, parameters);
			}
		}
		return null;
	}

	@Override
	public final Component getVariableOperation(final VariableType variable, final VR runtimeVariable) {
		return null;
	}
}
