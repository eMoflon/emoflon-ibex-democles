package org.emoflon.ibex.tgg.runtime.engine;

import java.util.Collection;
import java.util.stream.Collectors;

import org.emoflon.ibex.tgg.operational.matches.IMatch;
import org.emoflon.ibex.tgg.operational.matches.SimpleMatch;
import org.gervarro.democles.common.IDataFrame;
import org.gervarro.democles.specification.emf.Pattern;

public class DemoclesMatch implements IMatch {
	private IDataFrame frame;
	private Pattern pattern;

	public DemoclesMatch(IDataFrame frame, Pattern pattern) {
		this.frame = frame;
		this.pattern = pattern;
	}
	
	public Collection<String> parameterNames() {
		return pattern.getSymbolicParameters()
				.stream()
				.map(p -> p.getName())
				.collect(Collectors.toList());
	}

	public Object get(String name) {
		int index = varNameToIndex(name);
		return index == -1 ? null : frame.getValue(index);
	}

	public String patternName() {
		return pattern.getName();
	}
	
	private int varNameToIndex(String varName) {
		for(int i = 0; i < pattern.getSymbolicParameters().size(); i++){
			if(varName.equals(pattern.getSymbolicParameters().get(i).getName()))
				return i;
		}
		return -1;
	}
	
	public String toString() {
		return patternName();
	}

	@Override
	public IMatch copy() {
		SimpleMatch copy = new SimpleMatch(pattern.getName());
		parameterNames().forEach(n -> copy.put(n, get(n)));
		return copy;
	}
}
