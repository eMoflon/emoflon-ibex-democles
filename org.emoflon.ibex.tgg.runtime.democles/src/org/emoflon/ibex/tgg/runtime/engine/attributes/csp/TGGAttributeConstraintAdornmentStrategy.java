package org.emoflon.ibex.tgg.runtime.engine.attributes.csp;

import java.util.Collections;
import java.util.List;

import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.common.runtime.AdornmentAssignmentStrategy;
import org.gervarro.democles.runtime.NativeOperation;

public enum TGGAttributeConstraintAdornmentStrategy implements AdornmentAssignmentStrategy<List<Adornment>, NativeOperation> {
	INSTANCE;
		
	public final Adornment getAdornmentForNativeVariableOperation(final NativeOperation nativeOperation) {
		throw new RuntimeException("Unknown native variable operation");
	}
	
	public final List<Adornment> getAdornmentForNativeConstraintOperation(final NativeOperation nativeOperation) {
		final TGGAttributeNativeOperation operation = (TGGAttributeNativeOperation) nativeOperation;
		
		// Only support BBBB* as a possible operation
		int[] adornments = operation.getAttributeConstraintDefinition().getParameterDefinitions().stream()
				.mapToInt(x -> Adornment.BOUND)
				.toArray();
		
		return Collections.singletonList(Adornment.create(adornments));
	}	
}
