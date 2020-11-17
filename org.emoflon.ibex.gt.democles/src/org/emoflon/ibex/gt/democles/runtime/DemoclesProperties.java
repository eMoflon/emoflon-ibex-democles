package org.emoflon.ibex.gt.democles.runtime;

import org.emoflon.ibex.common.operational.IPatternInterpreterProperties;

public class DemoclesProperties implements IPatternInterpreterProperties {
	@Override
	public boolean needs_trash_resource() {
		return true;
	}

	@Override
	public boolean supports_dynamic_emf() {
		return true;
	}
	
	@Override
	public boolean needs_paranoid_modificiations() {
		return true;
	}
	
	@Override
	public boolean uses_synchroneous_matching() {
		return true;
	}
}
