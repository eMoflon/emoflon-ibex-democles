package org.emoflon.ibex.gt.democles.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoflon.ibex.common.operational.SimpleMatch;
import org.emoflon.ibex.tgg.compiler.patterns.PatternSuffixes;
import org.emoflon.ibex.tgg.operational.matches.TGGMatchParameterOrderProvider;
import org.gervarro.democles.common.IDataFrame;
import org.gervarro.democles.specification.emf.Pattern;

/**
 * A graph transformation match from Democles.
 */
public class DemoclesGTMatch extends SimpleMatch {
	/**
	 * Creates a new DemoclesGTMatch with the given frame and pattern.
	 * 
	 * @param frame   the Democles frame
	 * @param pattern the Democles pattern
	 */
	public DemoclesGTMatch(final IDataFrame frame, final Pattern pattern) {
		super(pattern.getName());
		List<String> params = null;
		if (pattern.getName() != null)
			params = TGGMatchParameterOrderProvider.getParams(PatternSuffixes.removeSuffix(pattern.getName()));
		if (params != null) {
			// Insert parameters in a predefined order for determined match hashing
			Map<String, Object> param2value = new HashMap<>();
			for (int i = 0; i < pattern.getSymbolicParameters().size(); i++) {
				String parameterName = pattern.getSymbolicParameters().get(i).getName();
				Object parameterValue = frame.getValue(i);
				param2value.put(parameterName, parameterValue);
			}
			for (String p : params) {
				if (param2value.containsKey(p))
					put(p, param2value.get(p));
			}
		} else {
			for (int i = 0; i < pattern.getSymbolicParameters().size(); i++) {
				String parameterName = pattern.getSymbolicParameters().get(i).getName();
				Object parameterValue = frame.getValue(i);
				put(parameterName, parameterValue);
			}
		}
	}
}
