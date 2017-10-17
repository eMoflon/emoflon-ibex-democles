package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import java.util.HashMap;
import java.util.Map;

import org.emoflon.ibex.tgg.operational.csp.constraints.factories.RuntimeTGGAttrConstraintProvider;
import org.gervarro.democles.specification.ConstraintType;

final public class TGGAttributeConstraintModule {
	public static final TGGAttributeConstraintModule INSTANCE = new TGGAttributeConstraintModule();
	private static final String NAME = "tgg_attr_constraints";
	private Map<String, TGGConstraintType> modules;

	private TGGAttributeConstraintModule() {
		modules = new HashMap<>();
	}

	public final String getName() {
		return NAME;
	}

	public final ConstraintType getConstraintType(final String identifier) {
		return modules.get(identifier);
	}
	
	public void registerConstraintTypes(RuntimeTGGAttrConstraintProvider constraintProvider) {
		constraintProvider.getAllUsedConstraintNames()
			.stream().forEach(constr -> {
				modules.put(constr, new TGGConstraintType(constr));
			});
	}

	public final String getConstraintTypeIdentifier(
			final ConstraintType constraintType) {
		if (constraintType instanceof TGGConstraintType) {
			final StringBuilder builder = new StringBuilder("/");
			builder.append(getName());
			builder.append("/");
			builder.append(((TGGConstraintType) constraintType).getID());
			return builder.toString();
		}
		return null;
	}
}
