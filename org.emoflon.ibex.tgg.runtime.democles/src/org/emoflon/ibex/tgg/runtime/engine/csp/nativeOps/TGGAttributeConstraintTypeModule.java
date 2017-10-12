package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps;

import org.eclipse.emf.ecore.util.Switch;
import org.gervarro.democles.specification.ConstraintType;
import org.gervarro.democles.specification.VariableType;
import org.gervarro.democles.specification.emf.TypeModule;

import TGGAttributeConstraint.AttributeConstraint;
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
		public ConstraintType caseAttributeConstraint(AttributeConstraint object) {
			String id = object.getName();
			return ((TGGAttributeConstraintModule) typeModule).getConstraintType(id);
		}
	}
}
