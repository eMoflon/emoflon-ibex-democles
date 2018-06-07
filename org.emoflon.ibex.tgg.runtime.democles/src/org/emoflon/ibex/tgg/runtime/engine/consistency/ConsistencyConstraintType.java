package org.emoflon.ibex.tgg.runtime.engine.consistency;

import org.gervarro.democles.specification.ConstraintType;

public class ConsistencyConstraintType implements ConstraintType {
	private final String id;
	
	ConsistencyConstraintType(String id) {
		this.id = id;
	}
	
	public final String getID() {
		return id;
	}

	public final ConsistencyConstraintModule getModule() {
		return ConsistencyConstraintModule.INSTANCE;
	}
	
	public final String toString() {
		return id;
	}
}