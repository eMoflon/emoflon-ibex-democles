package org.emoflon.ibex.tgg.compiler.democles.defaults;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.emoflon.ibex.tgg.compiler.defaults.DefaultFilesGenerator;
import org.emoflon.ibex.tgg.ide.admin.BuilderExtension;
import org.emoflon.ibex.tgg.ide.admin.IbexTGGBuilder;
import org.moflon.core.utilities.LogUtils;
import org.moflon.tgg.mosl.tgg.TripleGraphGrammarFile;

public class IbexDemoclesBuilderExtension implements BuilderExtension {

	private Logger logger = Logger.getLogger(IbexDemoclesBuilderExtension.class);
	
	@Override
	public void run(IbexTGGBuilder builder, TripleGraphGrammarFile editorModel, TripleGraphGrammarFile flattenedEditorModel) {
		try {
			builder.createDefaultDebugRunFile("MODELGEN_Debug_App", (projectName, fileName) 
				-> DefaultFilesGenerator.generateModelGenDebugFile(projectName, fileName));
			builder.createDefaultRunFile("MODELGEN_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateModelGenFile(projectName, fileName));
			builder.createDefaultRunFile("SYNC_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateSyncAppFile(projectName, fileName));
			builder.createDefaultRunFile("INITIAL_FWD_App", (projectName, fileName) 
				-> DefaultFilesGenerator.generateInitialFwdAppFile(projectName, fileName));
			builder.createDefaultRunFile("INITIAL_BWD_App", (projectName, fileName) 
				-> DefaultFilesGenerator.generateInitialBwdAppFile(projectName, fileName));
			builder.createDefaultRunFile("CC_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateCCAppFile(projectName, fileName));
			builder.createDefaultRunFile("CO_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateCOAppFile(projectName, fileName));
			builder.createDefaultRunFile("FWD_OPT_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateFWDOptAppFile(projectName, fileName));
			builder.createDefaultRunFile("BWD_OPT_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateBWDOptAppFile(projectName, fileName));
			builder.enforceDefaultConfigFile("_DefaultRegistrationHelper", (projectName, fileName) 
					-> DefaultFilesGenerator.generateDefaultRegHelperFile(projectName));
			builder.createDefaultConfigFile("DemoclesRegistrationHelper", (projectName, fileName)
					-> DefaultFilesGenerator.generateRegHelperFile(projectName, editorModel));
		} catch (CoreException e) {
			LogUtils.error(logger, e);
		}
	}
}
