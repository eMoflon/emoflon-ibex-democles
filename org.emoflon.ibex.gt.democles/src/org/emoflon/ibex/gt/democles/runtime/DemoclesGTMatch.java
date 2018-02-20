package org.emoflon.ibex.gt.democles.runtime;

import java.util.Collection;
import java.util.stream.Collectors;

import org.emoflon.ibex.common.operational.IMatch;
import org.gervarro.democles.common.IDataFrame;
import org.gervarro.democles.specification.emf.Pattern;

/**
 * A graph transformation match from Democles.
 */
public class DemoclesGTMatch implements IMatch {
	/**
	 * The Democles frame.
	 */
	private IDataFrame frame;

	/**
	 * The Democles pattern.
	 */
	protected Pattern pattern;

	/**
	 * Creates a new DemoclesGTMatch with the given frame and pattern.
	 * 
	 * @param frame
	 *            the Democles frame
	 * @param pattern
	 *            the Democles pattern
	 */
	public DemoclesGTMatch(final IDataFrame frame, final Pattern pattern) {
		this.frame = frame;
		this.pattern = pattern;
	}

	@Override
	public Object get(final String name) {
		int index = parameterNameToIndex(name);
		return index == -1 ? null : frame.getValue(index);
	}

	/**
	 * Maps parameter variable names to Democles variable identifiers.
	 * 
	 * @param varName
	 *            the variable
	 * @return the id if it exists, -1 otherwise
	 */
	private int parameterNameToIndex(final String varName) {
		for (int i = 0; i < pattern.getSymbolicParameters().size(); i++) {
			if (varName.equals(pattern.getSymbolicParameters().get(i).getName())) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public Collection<String> getParameterNames() {
		return pattern.getSymbolicParameters().stream().map(p -> p.getName()).collect(Collectors.toList());
	}

	@Override
	public String getPatternName() {
		return pattern.getName();
	}
}
