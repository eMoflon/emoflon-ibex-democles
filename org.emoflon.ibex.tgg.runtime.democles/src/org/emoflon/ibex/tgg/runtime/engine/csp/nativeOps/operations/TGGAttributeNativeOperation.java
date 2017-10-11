package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.operations;

import java.util.List;

import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.runtime.NativeOperation;
import org.gervarro.democles.specification.ConstraintType;

public abstract class TGGAttributeNativeOperation extends NativeOperation {
	public abstract ConstraintType getConstraintType();
	public abstract List<Adornment> getAllowedAdornments();
}
