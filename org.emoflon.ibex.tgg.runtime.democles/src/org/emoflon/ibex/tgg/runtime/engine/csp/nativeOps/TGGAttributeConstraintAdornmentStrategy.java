package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import java.util.List;

import org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.operations.TGGAttributeNativeOperation;
import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.common.runtime.AdornmentAssignmentStrategy;
import org.gervarro.democles.runtime.NativeOperation;

public enum TGGAttributeConstraintAdornmentStrategy implements AdornmentAssignmentStrategy<List<Adornment>, NativeOperation> {
	INSTANCE;
	
	private boolean isModelGen;
	
	public final Adornment getAdornmentForNativeVariableOperation(final NativeOperation nativeOperation) {
		throw new RuntimeException("Unknown native variable operation");
	}
	
	public final List<Adornment> getAdornmentForNativeConstraintOperation(final NativeOperation nativeOperation) {
		return ((TGGAttributeNativeOperation) nativeOperation).getAllowedAdornments(isModelGen);
	}
	
	public void setIsModelGen(boolean isModelGen) {
		this.isModelGen = isModelGen;
	}
}
