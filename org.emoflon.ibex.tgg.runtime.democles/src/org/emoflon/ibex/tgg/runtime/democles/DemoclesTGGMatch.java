package org.emoflon.ibex.tgg.runtime.democles;

import org.emoflon.ibex.gt.democles.runtime.DemoclesGTMatch;
import org.emoflon.ibex.tgg.operational.matches.IMatch;
import org.emoflon.ibex.tgg.operational.matches.SimpleMatch;
import org.gervarro.democles.common.IDataFrame;
import org.gervarro.democles.specification.emf.Pattern;

/**
 * A TGG match from Democles.
 */
public class DemoclesTGGMatch extends DemoclesGTMatch implements IMatch {
	/**
	 * Creates a new DemoclesMatch with the given frame and pattern.
	 * 
	 * @param frame
	 *            the Democles frame
	 * @param pattern
	 *            the Democles pattern
	 */
	public DemoclesTGGMatch(final IDataFrame frame, final Pattern pattern) {
		super(frame, pattern);
	}

	@Override
	public IMatch copy() {
		SimpleMatch copy = new SimpleMatch(getPatternName());
		getParameterNames().forEach(n -> copy.put(n, get(n)));
		return copy;
	}
}