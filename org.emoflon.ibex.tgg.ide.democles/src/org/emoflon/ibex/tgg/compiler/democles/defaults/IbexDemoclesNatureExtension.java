package org.emoflon.ibex.tgg.compiler.democles.defaults;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.emoflon.ibex.tgg.ide.admin.NatureExtension;
import org.moflon.core.plugins.manifest.ManifestFileUpdater;
import org.moflon.core.utilities.LogUtils;

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
								// Ibex Democles deps
								"org.emoflon.ibex.tgg.runtime.democles"
						));
				return changed;
			});
		} catch (CoreException e) {
			LogUtils.error(logger, e);
		}
	}
}
