package invizio.cip.math;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.AbstractBinaryComputerOp;
import net.imglib2.type.numeric.RealType;

public class MoreMathOperationCIP {
	

	/**
	 * 
	 * @author Benoit Lombardot
	 *
	 */

	
	
	@Plugin(type = Op.class , name = "MoreMathOperationMinCIP")
	public static class Min< T extends RealType<T> > extends AbstractBinaryComputerOp<T, T, T>
	{
		@Override
		public void compute(final T input1, final T input2, final T output) {
			output.setReal(  Math.min( input1.getRealDouble() , input2.getRealDouble() )  );
		}
	}

	
	@Plugin(type = Op.class, name = "MoreMathOperationMaxCIP")
	public static class Max< T extends RealType<T> > extends AbstractBinaryComputerOp<T, T, T> 
	{
		@Override
		public void compute(final T input1, final T input2, final T output) {
			output.setReal(  Math.max( input1.getRealDouble() , input2.getRealDouble() )  );
		}
	}
	
	
	@Plugin(type = Op.class, name = "MoreMathOperationPowCIP")
	public static class Pow< T extends RealType<T> > extends AbstractBinaryComputerOp<T, T, T> 
	{
		@Override
		public void compute(final T input1, final T input2, final T output) {
			output.setReal(  Math.pow( input1.getRealDouble() , input2.getRealDouble() )  );
		}
	}
	
}
