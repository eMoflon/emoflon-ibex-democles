package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import java.util.List;

import org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.operations.TGGAttributeNativeOperation;
import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.common.runtime.OperationBuilder;
import org.gervarro.democles.common.runtime.VariableRuntime;
import org.gervarro.democles.plan.incremental.leaf.Component;
import org.gervarro.democles.plan.incremental.leaf.FilterComponent;
import org.gervarro.democles.specification.ConstraintType;
import org.gervarro.democles.specification.VariableType;

public class TGGConstraintComponentBuilder<VR extends VariableRuntime> implements OperationBuilder<Component,Component,VR> {
	private final TGGNativeOperationBuilder<VariableRuntime> operationBuilder;
	
	public TGGConstraintComponentBuilder(final TGGNativeOperationBuilder<VariableRuntime> operationBuilder) {
		this.operationBuilder = operationBuilder;
	}
	
	@Override
	public final Component getConstraintOperation(final ConstraintType constraint, final List<? extends VR> parameters) {
		if (constraint instanceof TGGConstraintType) {
			final TGGAttributeNativeOperation nativeOperation =
					operationBuilder.getConstraintOperation(constraint, parameters);
			final int size = nativeOperation.getAttributeConstraintDefinition().getParameterDefinitions().size();
			return new FilterComponent(nativeOperation,
					new Adornment(size), constraint, parameters);
		}
		return null;
	}

	@Override
	public final Component getVariableOperation(final VariableType variable, final VR runtimeVariable) {
		return null;
	}
}
