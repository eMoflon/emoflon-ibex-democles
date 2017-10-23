package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.operations;

import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.ibex.tgg.operational.csp.RuntimeTGGAttributeConstraint;
import org.emoflon.ibex.tgg.operational.csp.constraints.factories.RuntimeTGGAttrConstraintProvider;
import org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.TGGAttributeConstraintModule;
import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.runtime.InternalDataFrameProvider;
import org.gervarro.democles.runtime.NativeOperation;
import org.gervarro.democles.runtime.RemappedDataFrame;
import org.gervarro.democles.specification.ConstraintType;

import language.csp.definition.TGGAttributeConstraintAdornment;

public class TGGAttributeNativeOperation extends NativeOperation {	
	private String id;
	private RuntimeTGGAttrConstraintProvider attrConstrProvider;
	
	public TGGAttributeNativeOperation(String id, RuntimeTGGAttrConstraintProvider attrConstrProvider) {
		this.id = id;
		this.attrConstrProvider = attrConstrProvider;
	}
	
	public ConstraintType getConstraintType() {
		return TGGAttributeConstraintModule.INSTANCE.getConstraintType(id);
	}
	
	private RuntimeTGGAttributeConstraint getNewConstraint() {
		return attrConstrProvider.createRuntimeTGGAttributeConstraint(id);
	}
	
	public List<Adornment> getAllowedAdornments(boolean isModelGen){
		if(isModelGen) {
			return attrConstrProvider.getTGGAttributeConstraintDefinition(id).getGenAdornments().stream()
					.map(this::createAdornment)
					.collect(Collectors.toList());
		}
		else {
			return attrConstrProvider.getTGGAttributeConstraintDefinition(id).getSyncAdornments().stream()
					.map(this::createAdornment)
					.collect(Collectors.toList());
		}
	}
	
	private Adornment createAdornment(TGGAttributeConstraintAdornment adornment) {
		return Adornment.create(adornment.getValue().stream()
				.mapToInt(this::stringToAdornment)
				.toArray()
		);
	}
	
	private int stringToAdornment(String adornEntry) {
		switch(adornEntry) {
		case "B": return Adornment.BOUND;
		case "F": return Adornment.FREE;
		default: throw new IllegalArgumentException("Invalid adornment entry found: " + adornEntry);
		}
	}

	@Override
	public InternalDataFrameProvider getDataFrame(RemappedDataFrame frame, Adornment adornment) {
		RuntimeTGGAttributeConstraint attrConstr = getNewConstraint();
		for(int i = 0; i < adornment.size(); i++) {
			if(adornment.get(i) == Adornment.BOUND) {
				attrConstr.getVariables().get(i).bindToValue(frame.getValue(i));
			}
		}
		
		attrConstr.solve();
		
		if(!attrConstr.isSatisfied())
			return null;
		
		if(adornment.cardinality() != 0)
			frame = createDataFrame(frame);
		
		for(int i = 0; i < adornment.size(); i++) {
			if(adornment.get(i) == Adornment.FREE) {
				frame.setValue(i, attrConstr.getVariables().get(i).getValue());
			}
		}
		
		return frame;
	}
}
