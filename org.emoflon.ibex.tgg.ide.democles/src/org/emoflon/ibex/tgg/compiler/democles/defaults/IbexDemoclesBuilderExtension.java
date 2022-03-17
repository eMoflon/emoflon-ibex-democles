package org.emoflon.ibex.tgg.compiler.democles.defaults;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.emoflon.ibex.common.project.ManifestHelper;
import org.emoflon.ibex.tgg.builder.TGGBuildUtil;
import org.emoflon.ibex.tgg.codegen.DefaultFilesGenerator;
import org.emoflon.ibex.tgg.codegen.TGGEngineBuilderExtension;
import org.moflon.core.plugins.manifest.ManifestFileUpdater;
import org.moflon.core.utilities.LogUtils;
import org.moflon.tgg.mosl.tgg.TripleGraphGrammarFile;

public class IbexDemoclesBuilderExtension implements TGGEngineBuilderExtension {

	private Logger logger = Logger.getLogger(IbexDemoclesBuilderExtension.class);
	
	private IProject project;
	
	@Override
	public void run(IProject project, TripleGraphGrammarFile editorModel, TripleGraphGrammarFile flattenedEditorModel) {
		this.project = project;
		
		updateManifest();
		
		try {
			TGGBuildUtil.createDefaultDebugRunFile( project,"MODELGEN_Debug_App", (projectName, fileName) 
				-> DefaultFilesGenerator.generateModelGenDebugFile(projectName, fileName));
			TGGBuildUtil.createDefaultRunFile(project, "MODELGEN_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateModelGenFile(projectName, fileName));
			TGGBuildUtil.createDefaultRunFile(project, "SYNC_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateSyncAppFile(projectName, fileName));
			TGGBuildUtil.createDefaultRunFile(project, "INITIAL_FWD_App", (projectName, fileName) 
				-> DefaultFilesGenerator.generateInitialFwdAppFile(projectName, fileName));
			TGGBuildUtil.createDefaultRunFile(project, "INITIAL_BWD_App", (projectName, fileName) 
				-> DefaultFilesGenerator.generateInitialBwdAppFile(projectName, fileName));
			TGGBuildUtil.createDefaultRunFile(project, "CC_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateCCAppFile(projectName, fileName));
			TGGBuildUtil.createDefaultRunFile(project, "CO_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateCOAppFile(projectName, fileName));
			TGGBuildUtil.createDefaultRunFile(project, "FWD_OPT_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateFWDOptAppFile(projectName, fileName));
			TGGBuildUtil.createDefaultRunFile(project, "BWD_OPT_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateBWDOptAppFile(projectName, fileName));
			TGGBuildUtil.createDefaultRunFile(project, "INTEGRATE_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateIntegrateAppFile(projectName, fileName));
			TGGBuildUtil.enforceDefaultConfigFile(project, "_DefaultRegistrationHelper", (projectName, fileName) 
					-> DefaultFilesGenerator.generateDefaultRegHelperFile(projectName));
			TGGBuildUtil.createDefaultConfigFile(project, "DemoclesRegistrationHelper", (projectName, fileName)
					-> DefaultFilesGenerator.generateRegHelperFile(projectName, editorModel));
		} catch (CoreException e) {
			LogUtils.error(logger, e);
		}
	}
	
	private void updateManifest() {
		try {
			IFile manifest = ManifestFileUpdater.getManifestFile(project);
			ManifestHelper helper = new ManifestHelper();
			helper.loadManifest(manifest);
			if(!helper.sectionContainsContent("Require-Bundle", "org.emoflon.ibex.tgg.runtime.democles")) {
				helper.addContentToSection("Require-Bundle", "org.emoflon.ibex.tgg.runtime.democles");
			}
			
			File rawManifest = new File(project.getLocation().toPortableString()+"/"+manifest.getFullPath().removeFirstSegments(1).toPortableString());
			
			helper.updateManifest(rawManifest);
			
		} catch (CoreException | IOException e) {
			LogUtils.error(logger, "Failed to update MANIFEST.MF \n"+e.getMessage());
		}
	}
}
