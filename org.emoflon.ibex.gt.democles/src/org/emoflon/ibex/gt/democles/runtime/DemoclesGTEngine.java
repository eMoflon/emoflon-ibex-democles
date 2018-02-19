package org.emoflon.ibex.gt.democles.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emoflon.ibex.common.operational.IMatchObserver;
import org.emoflon.ibex.common.operational.IPatternInterpreter;
import org.emoflon.ibex.common.utils.ModelPersistenceUtils;
import org.gervarro.democles.event.MatchEvent;
import org.gervarro.democles.event.MatchEventListener;
import org.gervarro.democles.specification.emf.Pattern;

import IBeXLanguage.IBeXPatternSet;

/**
 * Engine for (unidirectional) graph transformations with Democles.
 */
public class DemoclesGTEngine implements IPatternInterpreter, MatchEventListener {
	/**
	 * The path for debugging output.
	 */
	protected Optional<String> debugPath = Optional.empty();

	/**
	 * The Democles patterns.
	 */
	private List<Pattern> patterns = new ArrayList<Pattern>();

	@Override
	public void initPatterns(final IBeXPatternSet ibexPatternSet) {
		IBeXToDemoclesPatternTransformation transformation = new IBeXToDemoclesPatternTransformation();
		this.patterns = transformation.transform(ibexPatternSet);
		this.savePatternsForDebugging();
	}

	/**
	 * Saves the Democles patterns for debugging.
	 */
	private void savePatternsForDebugging() {
		this.debugPath.ifPresent(path -> {
			List<Pattern> sortedPatterns = this.patterns.stream()
					.sorted((p1, p2) -> p1.getName().compareTo(p2.getName())) // alphabetically by name
					.collect(Collectors.toList());
			ModelPersistenceUtils.saveModel(sortedPatterns, path + "/democles-patterns");
		});
	}

	@Override
	public void initialise(Registry registry, IMatchObserver matchObserver) {
		// TODO Auto-generated method stub

	}

	@Override
	public ResourceSet createAndPrepareResourceSet(String workspacePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void monitor(ResourceSet resourceSet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateMatches() {
		// TODO Auto-generated method stub

	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDebugPath(final String debugPath) {
		this.debugPath = Optional.of(debugPath);
	}

	/**
	 * Handles the {@link MatchEvent} from Democles.
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
