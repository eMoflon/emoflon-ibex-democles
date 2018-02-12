package org.emoflon.ibex.gt.democles.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.ibex.common.utils.ModelPersistenceUtils;
import org.emoflon.ibex.gt.engine.GTEngine;
import org.gervarro.democles.event.MatchEvent;
import org.gervarro.democles.event.MatchEventListener;
import org.gervarro.democles.specification.emf.Pattern;

import IBeXLanguage.IBeXPatternSet;

/**
 * Engine for unidirectional graph transformations with Democles.
 * 
 * @author Patrick Robrecht
 * @version 0.1
 */
public class DemoclesGTEngine extends GTEngine implements MatchEventListener {
	/**
	 * The Democles patterns.
	 */
	private List<Pattern> patterns = new ArrayList<Pattern>();

	@Override
	protected void transformPatterns(final IBeXPatternSet ibexPatternSet) {
		IBeXToDemoclesPatternTransformation transformation = new IBeXToDemoclesPatternTransformation();
		this.patterns = transformation.transform(ibexPatternSet);
		this.savePatternsForDebugging();
	}

	private void savePatternsForDebugging() {
		this.debugPath.ifPresent(path -> {
			List<Pattern> sortedPatterns = this.patterns.stream()
					.sorted((p1, p2) -> p1.getName().compareTo(p2.getName())) // alphabetically by name
					.collect(Collectors.toList());
			ModelPersistenceUtils.saveModel(sortedPatterns, path + "/democles-patterns");
		});
	}

	/**
	 * Handles {@link MatchEvent} from Democles.
	 * 
	 * @param event
	 *            the MatchEvent to handle
	 */
	@Override
	public void handleEvent(final MatchEvent event) {
		// TODO Auto-generated method stub
		System.out.println("GTDemoclesEngine.handleEvent: " + event.getEventType() + " - " + event.getSource());
	}
}
