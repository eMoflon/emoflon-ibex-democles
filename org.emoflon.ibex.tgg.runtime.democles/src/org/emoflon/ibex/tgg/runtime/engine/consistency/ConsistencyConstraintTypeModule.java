package org.emoflon.ibex.tgg.runtime.engine.consistency;

import org.eclipse.emf.ecore.util.Switch;
import org.gervarro.democles.specification.ConstraintType;
import org.gervarro.democles.specification.VariableType;
import org.gervarro.democles.specification.emf.TypeModule;

import ConsistencyConstraint.Consistency;
import ConsistencyConstraint.util.ConsistencyConstraintSwitch;

public class ConsistencyConstraintTypeModule extends TypeModule<ConsistencyConstraintModule> {

	public ConsistencyConstraintTypeModule(ConsistencyConstraintModule typeModule) {
		super(typeModule);
	}

	@Override
	protected Switch<VariableType> createVariableTypeSwitch() {
		return null;
	}

	@Override
	protected Switch<ConstraintType> createConstraintTypeSwitch() {
		return new ConsistencyConstraintTypeSwitch();
	}

	private class ConsistencyConstraintTypeSwitch extends ConsistencyConstraintSwitch<ConstraintType> {
		
		@Override
		public ConstraintType caseConsistency(Consistency object) {
			String id = object.getName();
			return ((ConsistencyConstraintModule) typeModule).getConstraintType(id);
		}
	}
}
