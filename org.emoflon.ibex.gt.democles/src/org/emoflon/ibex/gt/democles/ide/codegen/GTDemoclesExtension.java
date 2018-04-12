package org.emoflon.ibex.gt.democles.ide.codegen;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.emoflon.ibex.gt.codegen.GTEngineExtension;

/**
 * Registers the Democles engine for code generation.
 */
public class GTDemoclesExtension implements GTEngineExtension {

	@Override
	public Set<String> getDependencies() {
		return new HashSet<String>(Arrays.asList("org.emoflon.ibex.gt.democles"));
	}

	@Override
	public Set<String> getImports() {
		return new HashSet<String>(Arrays.asList("org.emoflon.ibex.gt.democles.runtime.DemoclesGTEngine"));
	}

	@Override
	public String getEngineName() {
		return "Democles";
	}

	@Override
	public String getEngineClassName() {
		return "DemoclesGTEngine";
	}
}
