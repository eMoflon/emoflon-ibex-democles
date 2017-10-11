package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import org.gervarro.democles.specification.ConstraintType;

final public class TGGAttributeConstraintModule {
	public static final TGGAttributeConstraintModule INSTANCE = new TGGAttributeConstraintModule();
	private TGGAttributeConstraintModule() {}
	
	public static final String EQ_STRING_LABEL = "eq_string";

	public static final TGGConstraintType EQ_STRING =
			new TGGConstraintType(EQ_STRING_LABEL);
	
	private static final String NAME = "tgg_attr_constraints";

	public final String getName() {
		return NAME;
	}

	public final ConstraintType getConstraintType(final String identifier) {
		if (EQ_STRING_LABEL.equals(identifier)) {
			return EQ_STRING;
		} 
		return null;
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
