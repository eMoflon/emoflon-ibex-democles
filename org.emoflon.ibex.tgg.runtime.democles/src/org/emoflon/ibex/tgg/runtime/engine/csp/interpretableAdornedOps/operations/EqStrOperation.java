package org.emoflon.ibex.tgg.runtime.engine.csp.interpretableAdornedOps.operations;

import java.util.Arrays;
import java.util.List;

import org.emoflon.ibex.tgg.operational.csp.RuntimeTGGAttributeConstraint;
import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.runtime.InternalDataFrameProvider;
import org.gervarro.democles.runtime.InterpretableAdornedOperation;
import org.gervarro.democles.runtime.RemappedDataFrame;

public class EqStrOperation {

	private static final InterpretableAdornedOperation EqStr_BB = new EqStrOperationBB();
	private static final InterpretableAdornedOperation EqStr_BF = new EqStrOperationBF();
	private static final InterpretableAdornedOperation EqStr_FB = new EqStrOperationFB();
	private static final InterpretableAdornedOperation EqStr_FF = new EqStrOperationFF();
	
	public static List<InterpretableAdornedOperation> getAllOperations() {
		return Arrays.asList(EqStr_BB, EqStr_BF, EqStr_FB, EqStr_FF);
	}
}

class EqStrOperationBB extends InterpretableAdornedOperation {
	protected EqStrOperationBB() {
		super(new Adornment(new int[] { Adornment.BOUND, Adornment.BOUND }));
	}
	
	@Override
	public InternalDataFrameProvider getDataFrame(RemappedDataFrame frame) {
		final Object src = frame.getValue(0);
		final Object trg = frame.getValue(1);
		if (trg.equals(src)) {
			return frame;
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "eq_string_BB";
	}
}

class EqStrOperationBF extends InterpretableAdornedOperation {
	protected EqStrOperationBF() {
		super(new Adornment(new int[] { Adornment.BOUND, Adornment.FREE}));
	}
	
	@Override
	public InternalDataFrameProvider getDataFrame(RemappedDataFrame frame) {
		final Object src = frame.getValue(0);
		final Object trg = src;
		// FIXME [Anjorin]:  Is this required?
		// frame = frame.createDataFrame();
		frame.setValue(1, trg);
		return frame;
	}
	
	@Override
	public String toString() {
		return "eq_string_BF";
	}
}

class EqStrOperationFB extends InterpretableAdornedOperation {
	protected EqStrOperationFB() {
		super(new Adornment(new int[] { Adornment.FREE, Adornment.BOUND}));
	}
	
	@Override
	public InternalDataFrameProvider getDataFrame(RemappedDataFrame frame) {
		final Object trg = frame.getValue(1);
		final Object src = trg;
		// FIXME [Anjorin]:  Is this required?
		// frame = frame.createDataFrame();
		frame.setValue(0, src);
		return frame;
	}
	
	@Override
	public String toString() {
		return "eq_string_FB";
	}
}

class EqStrOperationFF extends InterpretableAdornedOperation {
	protected EqStrOperationFF() {
		super(new Adornment(new int[] { Adornment.FREE, Adornment.FREE}));
	}
	
	@Override
	public InternalDataFrameProvider getDataFrame(RemappedDataFrame frame) {
		Object value = RuntimeTGGAttributeConstraint.generateValue(String.class.getName());
		final Object src = value;
		final Object trg = value;
		// FIXME [Anjorin]:  Is this required?
		// frame = frame.createDataFrame();
		frame.setValue(0, src);
		frame.setValue(1, trg);
		return frame;
	}
	
	@Override
	public String toString() {
		return "eq_string_FF";
	}
}
