package org.emoflon.ibex.tgg.runtime.engine;

import org.emoflon.ibex.gt.democles.runtime.DemoclesGTMatch;
import org.emoflon.ibex.tgg.operational.matches.IMatch;
import org.emoflon.ibex.tgg.operational.matches.SimpleMatch;
import org.gervarro.democles.common.IDataFrame;
import org.gervarro.democles.specification.emf.Pattern;

/**
 * A TGG match from Democles.
 */
public class DemoclesMatch extends DemoclesGTMatch implements IMatch {
	/**
	 * Creates a new DemoclesMatch with the given frame and pattern.
	 * 
	 * @param frame
	 *            the Democles frame
	 * @param pattern
	 *            the Democles pattern
	 */
	public DemoclesMatch(final IDataFrame frame, final Pattern pattern) {
		super(frame, pattern);
	}

	@Override
	public IMatch copy() {
		SimpleMatch copy = new SimpleMatch(pattern.getName());
		getParameterNames().forEach(n -> copy.put(n, get(n)));
		return copy;
	}
}
