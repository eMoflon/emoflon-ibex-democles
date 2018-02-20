package org.emoflon.ibex.tgg.compiler.democles.defaults;

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.emoflon.ibex.tgg.ide.admin.NatureExtension;
import org.emoflon.ibex.tgg.ui.ide.admin.plugins.ManifestFileUpdater;
import org.moflon.util.LogUtils;

public class IbexDemoclesNatureExtension implements NatureExtension {

	private Logger logger = Logger.getLogger(IbexDemoclesNatureExtension.class);
	
	@Override
	public void setUpProject(IProject project) {
		try {
			new ManifestFileUpdater().processManifest(project, manifest -> {
				boolean changed = false;
				changed |= ManifestFileUpdater.updateDependencies(
						manifest,
						Arrays.asList(
								// Democles deps
								"org.gervarro.democles.common",
								"org.gervarro.democles.specification.emf",
								"org.gervarro.democles.interpreter",
								"org.gervarro.democles.emf",
								"org.gervarro.democles.interpreter.emf",
								"org.gervarro.democles.interpreter.incremental",
								"org.gervarro.democles.interpreter.incremental.emf",
								"org.gervarro.democles.interpreter.lightning",
								"org.gervarro.democles.notification.emf",
								"org.gervarro.democles.plan",
								"org.gervarro.democles.plan.emf",
								"org.gervarro.democles.plan.incremental.leaf",
								"org.gervarro.util",
								"org.gervarro.notification",
								"org.gervarro.plan.dynprog"
						),
						Arrays.asList(
								// Ibex Democles deps
								"org.emoflon.ibex.tgg.runtime.democles"
						));
				return changed;
			});
		} catch (CoreException | IOException e) {
			LogUtils.error(logger, e);
		}
	}

}
