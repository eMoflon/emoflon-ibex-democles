package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import org.gervarro.democles.specification.ConstraintType;

public class TGGConstraintType implements ConstraintType {
	private final String id;
	
	TGGConstraintType(String id) {
		this.id = id;
	}
	
	public final String getID() {
		return id;
	}

	public final TGGAttributeConstraintModule getModule() {
		return TGGAttributeConstraintModule.INSTANCE;
	}
	
	public final String toString() {
		return id;
	}
}