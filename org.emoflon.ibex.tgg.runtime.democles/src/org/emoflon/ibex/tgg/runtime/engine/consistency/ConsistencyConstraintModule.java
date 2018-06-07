package org.emoflon.ibex.tgg.runtime.engine.consistency;

import java.util.HashMap;
import java.util.Map;

import org.gervarro.democles.specification.ConstraintType;

final public class ConsistencyConstraintModule {
	public static final ConsistencyConstraintModule INSTANCE = new ConsistencyConstraintModule();
	private static final String NAME = "consistency_constraints";
	private Map<String, ConsistencyConstraintType> modules;

	private ConsistencyConstraintModule() {
		modules = new HashMap<>();
	}

	public final String getName() {
		return NAME;
	}

	public final ConstraintType getConstraintType(final String identifier) {
		return modules.get(identifier);
	}

	public final String getConstraintTypeIdentifier(final ConstraintType constraintType) {
		if (constraintType instanceof ConsistencyConstraintType) {
			final StringBuilder builder = new StringBuilder("/");
			builder.append(getName());
			builder.append("/");
			builder.append(((ConsistencyConstraintType) constraintType).getID());
			return builder.toString();
		}
		return null;
	}
}
