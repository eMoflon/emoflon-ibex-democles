package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.operations;

import org.emoflon.ibex.tgg.operational.csp.RuntimeTGGAttributeConstraint;
import org.emoflon.ibex.tgg.operational.csp.constraints.factories.RuntimeTGGAttrConstraintProvider;
import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.runtime.InternalDataFrameProvider;
import org.gervarro.democles.runtime.NativeOperation;
import org.gervarro.democles.runtime.RemappedDataFrame;

import language.csp.definition.TGGAttributeConstraintDefinition;

public class TGGAttributeNativeOperation extends NativeOperation {	
	private final RuntimeTGGAttributeConstraint attrConstr;
	private String id;
	private RuntimeTGGAttrConstraintProvider attrConstrProvider;
	
	public TGGAttributeNativeOperation(
			final RuntimeTGGAttributeConstraint attrConstr,
			final String id,
			final RuntimeTGGAttrConstraintProvider attrConstrProvider) {
		this.attrConstr = attrConstr;
		this.id = id;
		this.attrConstrProvider = attrConstrProvider;
	}
	
	public TGGAttributeConstraintDefinition getAttributeConstraintDefinition() {
		return attrConstrProvider.getTGGAttributeConstraintDefinition(id);
	}
	
	@Override
	public InternalDataFrameProvider getDataFrame(RemappedDataFrame frame, Adornment adornment) {
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
