package org.emoflon.ibex.tgg.compiler.democles.defaults;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.emoflon.ibex.tgg.compiler.defaults.DefaultFilesGenerator;
import org.emoflon.ibex.tgg.ui.ide.admin.BuilderExtension;
import org.emoflon.ibex.tgg.ui.ide.admin.IbexTGGBuilder;
import org.moflon.tgg.mosl.tgg.TripleGraphGrammarFile;
import org.moflon.util.LogUtils;

public class IbexDemoclesBuilderExtension implements BuilderExtension {

	private Logger logger = Logger.getLogger(IbexDemoclesBuilderExtension.class);
	
	private static final String ENGINE = "DemoclesEngine";
	private static final String IMPORT = "import org.emoflon.ibex.tgg.runtime.engine.DemoclesEngine;";
	
	@Override
	public void run(IbexTGGBuilder builder, TripleGraphGrammarFile editorModel, TripleGraphGrammarFile flattenedEditorModel) {
		try {
			builder.createDefaultFile("MODELGEN_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateModelGenFile(projectName, fileName, ENGINE, IMPORT));
			builder.createDefaultFile("SYNC_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateSyncAppFile(projectName, fileName, ENGINE, IMPORT));
			builder.createDefaultFile("CC_App", (projectName, fileName) 
					-> DefaultFilesGenerator.generateCCAppFile(projectName, fileName, ENGINE, IMPORT));
		} catch (CoreException e) {
			LogUtils.error(logger, e);
		}
	}
}
