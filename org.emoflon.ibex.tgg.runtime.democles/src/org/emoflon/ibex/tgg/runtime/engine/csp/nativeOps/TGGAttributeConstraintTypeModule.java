package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import org.eclipse.emf.ecore.util.Switch;
import org.gervarro.democles.specification.ConstraintType;
import org.gervarro.democles.specification.VariableType;
import org.gervarro.democles.specification.emf.TypeModule;

import TGGAttributeConstraint.EqStr;
import TGGAttributeConstraint.util.TGGAttributeConstraintSwitch;

public class TGGAttributeConstraintTypeModule extends TypeModule<TGGAttributeConstraintModule> {

	public TGGAttributeConstraintTypeModule(TGGAttributeConstraintModule typeModule) {
		super(typeModule);
	}

	@Override
	protected Switch<VariableType> createVariableTypeSwitch() {
		return null;
	}

	@Override
	protected Switch<ConstraintType> createConstraintTypeSwitch() {
		return new TGGAttributeConstraintTypeSwitch();
	}

	private class TGGAttributeConstraintTypeSwitch extends TGGAttributeConstraintSwitch<ConstraintType> {
		
		@Override
		public ConstraintType caseEqStr(EqStr object) {
			return ((TGGAttributeConstraintModule) typeModule).getConstraintType(TGGAttributeConstraintModule.EQ_STRING_LABEL);
		}
	}
}
