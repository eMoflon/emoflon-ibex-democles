package org.emoflon.ibex.gt.democles.runtime;

import org.emoflon.ibex.common.operational.SimpleMatch;
import org.gervarro.democles.common.IDataFrame;
import org.gervarro.democles.specification.emf.Pattern;

/**
 * A graph transformation match from Democles.
 */
public class DemoclesGTMatch extends SimpleMatch {
	/**
	 * Creates a new DemoclesGTMatch with the given frame and pattern.
	 * 
	 * @param frame
	 *            the Democles frame
	 * @param pattern
	 *            the Democles pattern
	 */
	public DemoclesGTMatch(final IDataFrame frame, final Pattern pattern) {
		super(pattern.getName());
		for (int i = 0; i < pattern.getSymbolicParameters().size(); i++) {
			String parameterName = pattern.getSymbolicParameters().get(i).getName();
			Object parameterValue = frame.getValue(i);
			put(parameterName, parameterValue);
		}
	}
}
