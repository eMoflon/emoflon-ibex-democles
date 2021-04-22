package org.emoflon.ibex.tgg.ide.democles.visualisation;

import java.util.Optional;

import org.eclipse.emf.ecore.presentation.EcoreEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorPart;
import org.gervarro.democles.specification.emf.Pattern;
import org.gervarro.democles.specification.emf.PatternBody;
import org.moflon.core.ui.visualisation.EMoflonPlantUMLGenerator;
import org.moflon.core.ui.visualisation.common.EMoflonDiagramTextProvider;

public class IbexDemoclesPatternVisualiser implements EMoflonDiagramTextProvider {

	@Override
	public String getDiagramBody(IEditorPart editor, ISelection selection) {
		return maybeVisualisePattern(editor, selection).orElse(
			   EMoflonPlantUMLGenerator.emptyDiagram());
	}
	
	private Optional<Object> selectionInEcoreEditor(IEditorPart editor){
		return Optional.of(editor)
				.flatMap(maybeCast(EcoreEditor.class))
				.map(EcoreEditor::getSelection)
				.flatMap(maybeCast(TreeSelection.class))
				.map(TreeSelection::getFirstElement);
	}
	
	private Optional<String> maybeVisualisePattern(IEditorPart editor, ISelection selection) {
		return extractPatternBodyFromEditor(editor)					
				.map(pb -> IbexDemoclesPlantUMLGenerator.visualisePatternBody(pb, "0" + IbexDemoclesPlantUMLGenerator.separator()));
	}

	private Optional<PatternBody> extractPatternBodyFromEditor(IEditorPart editor) {
		return Optional.of(editor)
				.flatMap(this::selectionInEcoreEditor)
				.flatMap(maybeCast(Pattern.class))
				.flatMap(this::patternHasExactlyOneBody)
				.map(p -> p.getBodies().get(0));
	}

	private Optional<Pattern> patternHasExactlyOneBody(Pattern p){
		if(p.getBodies().size() != 1) 
			return Optional.empty();
		else
			return Optional.of(p);
	}
	
	@Override
	public boolean supportsEditor(IEditorPart editor) {
		return extractPatternBodyFromEditor(editor).isPresent();
	}

	@Override
	public boolean supportsSelection(ISelection arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
