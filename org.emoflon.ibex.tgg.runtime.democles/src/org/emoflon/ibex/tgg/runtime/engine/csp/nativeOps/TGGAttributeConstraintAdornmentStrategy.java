package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import java.util.Arrays;
import java.util.List;

import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.common.runtime.AdornmentAssignmentStrategy;
import org.gervarro.democles.runtime.NativeOperation;

public enum TGGAttributeConstraintAdornmentStrategy implements AdornmentAssignmentStrategy<List<Adornment>,NativeOperation> {
	INSTANCE;
	
	public final Adornment getAdornmentForNativeVariableOperation(final NativeOperation nativeOperation) {
		throw new RuntimeException("Unknown native variable operation");
	}
	
	public final List<Adornment> getAdornmentForNativeConstraintOperation(final NativeOperation nativeOperation) {
		return Arrays.asList(
				Adornment.create(Adornment.BOUND, Adornment.BOUND),
				Adornment.create(Adornment.BOUND, Adornment.FREE),
				Adornment.create(Adornment.FREE, Adornment.BOUND),
				Adornment.create(Adornment.FREE, Adornment.FREE));
	}
	
	public AdornmentAssignmentStrategy<Adornment, NativeOperation> getStrategy(Adornment adornment){
		return new AdornmentAssignmentStrategy<Adornment, NativeOperation>() {
			
			@Override
			public Adornment getAdornmentForNativeVariableOperation(NativeOperation nativeOperation) {
				return adornment;
			}
			
			@Override
			public Adornment getAdornmentForNativeConstraintOperation(NativeOperation nativeOperation) {
				return adornment;
			}
		};
	}
}
