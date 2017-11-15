package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.operations.TGGAttributeNativeOperation;
import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.common.runtime.AdornmentAssignmentStrategy;
import org.gervarro.democles.runtime.NativeOperation;

import language.csp.definition.TGGAttributeConstraintAdornment;

public enum TGGAttributeConstraintAdornmentStrategy implements AdornmentAssignmentStrategy<List<Adornment>, NativeOperation> {
	INSTANCE;
	
	private boolean isModelGen;
	
	public final Adornment getAdornmentForNativeVariableOperation(final NativeOperation nativeOperation) {
		throw new RuntimeException("Unknown native variable operation");
	}
	
	public final List<Adornment> getAdornmentForNativeConstraintOperation(final NativeOperation nativeOperation) {
		final TGGAttributeNativeOperation operation = (TGGAttributeNativeOperation) nativeOperation;
		if (isModelGen) {
			return operation.getAttributeConstraintDefinition().getGenAdornments().stream()
					.map(this::createAdornment)
					.collect(Collectors.toList());
		} else {
			return operation.getAttributeConstraintDefinition().getSyncAdornments().stream()
					.map(this::createAdornment)
					.collect(Collectors.toList());
		}
	}
	
	public void setIsModelGen(boolean isModelGen) {
		this.isModelGen = isModelGen;
	}
	
	private Adornment createAdornment(TGGAttributeConstraintAdornment adornment) {
		return Adornment.create(adornment.getValue().stream()
				.mapToInt(this::stringToAdornment)
				.toArray()
		);
	}
	
	private int stringToAdornment(String adornEntry) {
		switch(adornEntry) {
		case "B": return Adornment.BOUND;
		case "F": return Adornment.FREE;
		default: throw new IllegalArgumentException("Invalid adornment entry found: " + adornEntry);
		}
	}
}
