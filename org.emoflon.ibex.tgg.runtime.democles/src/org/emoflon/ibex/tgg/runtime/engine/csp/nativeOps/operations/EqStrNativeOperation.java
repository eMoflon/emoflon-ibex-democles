package org.emoflon.ibex.tgg.runtime.engine.csp.nativeOps.operations;

import org.emoflon.ibex.tgg.operational.csp.RuntimeTGGAttributeConstraint;
import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.runtime.InternalDataFrameProvider;
import org.gervarro.democles.runtime.NativeOperation;
import org.gervarro.democles.runtime.RemappedDataFrame;

public class EqStrNativeOperation extends NativeOperation{

	@Override
	public InternalDataFrameProvider getDataFrame(RemappedDataFrame frame, Adornment adornment) {
		if(adornment.get(0) == Adornment.BOUND && adornment.get(1) == Adornment.BOUND) {
			final Object src = frame.getValue(0);
			final Object trg = frame.getValue(1);
			if (trg.equals(src)) {
				return frame;
			}
		}
		
		if(adornment.get(0) == Adornment.BOUND && adornment.get(1) == Adornment.FREE) {
			final Object src = frame.getValue(0);
			final Object trg = src;
			frame = createDataFrame(frame);
			frame.setValue(1, trg);
			return frame;
		}
		
		if(adornment.get(0) == Adornment.FREE && adornment.get(1) == Adornment.BOUND) {
			final Object trg = frame.getValue(1);
			final Object src = trg;
			frame = createDataFrame(frame);
			frame.setValue(0, src);
			return frame;
		}
		
		if(adornment.get(0) == Adornment.FREE && adornment.get(1) == Adornment.FREE) {
			Object value = RuntimeTGGAttributeConstraint.generateValue(String.class.getName());
			final Object src = value;
			final Object trg = value;
			frame = createDataFrame(frame);
			frame.setValue(0, src);
			frame.setValue(1, trg);
			return frame;
		}
		return null;
	}
	
	
	@Override
	public String toString() {
		return "eq_string";
	}
}
