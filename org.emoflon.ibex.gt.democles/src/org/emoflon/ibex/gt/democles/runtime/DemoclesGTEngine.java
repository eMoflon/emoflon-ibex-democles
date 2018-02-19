package org.emoflon.ibex.gt.democles.runtime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emoflon.ibex.common.operational.IMatchObserver;
import org.emoflon.ibex.common.operational.IPatternInterpreter;
import org.emoflon.ibex.common.utils.ModelPersistenceUtils;
import org.gervarro.democles.event.MatchEvent;
import org.gervarro.democles.event.MatchEventListener;
import org.gervarro.democles.incremental.emf.NotificationProcessor;
import org.gervarro.democles.interpreter.incremental.rete.RetePattern;
import org.gervarro.democles.interpreter.incremental.rete.RetePatternMatcherModule;
import org.gervarro.democles.specification.emf.EMFDemoclesPatternMetamodelPlugin;
import org.gervarro.democles.specification.emf.Pattern;

import IBeXLanguage.IBeXPatternSet;

/**
 * Engine for (unidirectional) graph transformations with Democles.
 */
public class DemoclesGTEngine implements IPatternInterpreter, MatchEventListener {
	/**
	 * The registry.
	 */
	protected Registry registry;

	/**
	 * The match observer.
	 */
	protected IMatchObserver app;

	/**
	 * The Democles patterns.
	 */
	protected Collection<Pattern> patterns = new ArrayList<Pattern>();

	/**
	 * The pattern matchers.
	 */
	protected Collection<RetePattern> patternMatchers;

	/**
	 * The pattern matcher module.
	 */
	protected RetePatternMatcherModule retePatternMatcherModule;

	/**
	 * The observer (??).
	 */
	protected NotificationProcessor observer;

	/**
	 * The path for debugging output.
	 */
	protected Optional<String> debugPath = Optional.empty();

	/**
	 * Creates a new DemoclesGTEngine.
	 */
	public DemoclesGTEngine() {
		this.patterns = new ArrayList<>();
		this.patternMatchers = new ArrayList<>();
	}

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
		this.registry = registry;
		this.app = matchObserver;
	}

	@Override
	public ResourceSet createAndPrepareResourceSet(final String workspacePath) {
		ResourceSet resourceSet = new ResourceSetImpl();
		// In contrast to EMFDemoclesPatternMetamodelPlugin.createDefaultResourceSet, we
		// do not delegate directly to the global registry!
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

		try {
			EMFDemoclesPatternMetamodelPlugin.setWorkspaceRootDirectory(resourceSet,
					new File(workspacePath).getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resourceSet;
	}

	@Override
	public void monitor(final ResourceSet resourceSet) {
		this.observer.install(resourceSet);
	}

	@Override
	public void updateMatches() {
		// Trigger the Rete network.
		this.retePatternMatcherModule.performIncrementalUpdates();
	}

	@Override
	public void terminate() {
		this.patternMatchers.forEach(pm -> pm.removeEventListener(this));
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
