package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.common.runtime.AdornedElementBuilder;
import org.gervarro.democles.common.runtime.AdornmentAssignmentStrategy;
import org.gervarro.democles.common.runtime.OperationBuilder;
import org.gervarro.democles.common.runtime.VariableRuntime;
import org.gervarro.democles.runtime.DelegatingAdornedOperation;
import org.gervarro.democles.runtime.InterpretableAdornedOperation;
import org.gervarro.democles.runtime.NativeOperation;

public class TGGAdornedNativeOperationBuilder<VR extends VariableRuntime> extends
		AdornedElementBuilder<VR, List<Adornment>, NativeOperation, InterpretableAdornedOperation, List<InterpretableAdornedOperation>>
		implements OperationBuilder<InterpretableAdornedOperation, List<InterpretableAdornedOperation>, VR> {

	// Caching maps
	private final Map<NativeOperation, List<InterpretableAdornedOperation>> nativeConstraintOperationToAdornedOperation = new HashMap<NativeOperation, List<InterpretableAdornedOperation>>();

	public TGGAdornedNativeOperationBuilder(
			final OperationBuilder<NativeOperation, NativeOperation, VR> nativeOperationBuilder,
			final AdornmentAssignmentStrategy<List<Adornment>, NativeOperation> adornmentAssignmentStrategy) {
		super(nativeOperationBuilder, adornmentAssignmentStrategy);
	}

	@Override
	protected InterpretableAdornedOperation createOperationForVariable(final NativeOperation nativeOperation) {
		throw new IllegalArgumentException("Unknown variable for native operation");
	}

	@Override
	protected List<InterpretableAdornedOperation> createOperationForConstraint(final NativeOperation nativeOperation) {
		List<InterpretableAdornedOperation> adornedOperations = nativeConstraintOperationToAdornedOperation.get(nativeOperation);
		if (adornedOperations == null) {
			adornedOperations = new LinkedList<InterpretableAdornedOperation>();
			for (final Adornment adornment : getAdornmentAssignmentStrategy().getAdornmentForNativeConstraintOperation(nativeOperation)) {
				final DelegatingAdornedOperation delegatingOperation = new DelegatingAdornedOperation(nativeOperation, adornment);
				adornedOperations.add(delegatingOperation);
				nativeOperation.addEventListener(delegatingOperation);
			}
			
			nativeConstraintOperationToAdornedOperation.put(nativeOperation, adornedOperations);
		}
		return adornedOperations;
	}
}