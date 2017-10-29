package invizio.cip.math;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Op;
import net.imagej.ops.special.computer.AbstractUnaryComputerOp;
import net.imglib2.type.numeric.RealType;

public class Math1OperationCIP {
	
	/**
	 * 
	 * @author Benoit Lombardot
	 *
	 */
	
	
	@Plugin(type = Op.class , name = "Math1OperationCosCIP")
	public static class Cos< T extends RealType<T> , U extends RealType<U> > extends AbstractUnaryComputerOp<T, U>
	{
		@Override
		public void compute(final T input1, final U output) {
			output.setReal(  Math.cos( input1.getRealDouble() )  );
		}
	}
	
	

	@Plugin(type = Op.class , name = "Math1OperationSinCIP")
	public static class Sin< T extends RealType<T> , U extends RealType<U> > extends AbstractUnaryComputerOp<T, U>
	{
		@Override
		public void compute(final T input1, final U output) {
			output.setReal(  Math.sin( input1.getRealDouble() )  );
		}
	}

	
	@Plugin(type = Op.class , name = "Math1OperationTanCIP")
	public static class Tan< T extends RealType<T>  , U extends RealType<U> > extends AbstractUnaryComputerOp<T, U>
	{
		@Override
		public void compute(final T input1, final U output) {
			output.setReal(  Math.tan( input1.getRealDouble() )  );
		}
	}

	@Plugin(type = Op.class , name = "Math1OperationACosCIP")
	public static class ACos< T extends RealType<T>  , U extends RealType<U> > extends AbstractUnaryComputerOp<T, U>
	{
		@Override
		public void compute(final T input1, final U output) {
			output.setReal(  Math.acos( input1.getRealDouble() )  );
		}
	}

	@Plugin(type = Op.class , name = "Math1OperationASinCIP")
	public static class ASin< T extends RealType<T>  , U extends RealType<U> > extends AbstractUnaryComputerOp<T, U>
	{
		@Override
		public void compute(final T input1, final U output) {
			output.setReal(  Math.asin( input1.getRealDouble() )  );
		}
	}

	@Plugin(type = Op.class , name = "Math1OperationATanCIP")
	public static class ATan< T extends RealType<T>  , U extends RealType<U> > extends AbstractUnaryComputerOp<T, U>
	{
		@Override
		public void compute(final T input1, final U output) {
			output.setReal(  Math.atan( input1.getRealDouble() )  );
		}
	}

	
	
	@Plugin(type = Op.class , name = "Math1OperationLogCIP")
	public static class Log< T extends RealType<T>  , U extends RealType<U> > extends AbstractUnaryComputerOp<T, U>
	{
		@Override
		public void compute(final T input1, final U output) {
			output.setReal(  Math.log( input1.getRealDouble() )  );
		}
	}

	@Plugin(type = Op.class , name = "Math1OperationExpCIP")
	public static class Exp< T extends RealType<T>  , U extends RealType<U> > extends AbstractUnaryComputerOp<T, U>
	{
		@Override
		public void compute(final T input1, final U output) {
			output.setReal(  Math.exp( input1.getRealDouble() )  );
		}
	}

	
	@Plugin(type = Op.class , name = "Math1OperationSqrtCIP")
	public static class Sqrt< T extends RealType<T> , U extends RealType<U> > extends AbstractUnaryComputerOp<T, U>
	{
		@Override
		public void compute(final T input1, final U output) {
			output.setReal(  Math.sqrt( input1.getRealDouble() )  );
		}
	}

	@Plugin(type = Op.class , name = "Math1OperationAbsCIP")
	public static class Abs< T extends RealType<T> , U extends RealType<U> > extends AbstractUnaryComputerOp<T, U>
	{
		@Override
		public void compute(final T input1, final U output) {
			output.setReal(  Math.abs( input1.getRealDouble() )  );
		}
	}

	@Plugin(type = Op.class , name = "Math1OperationRoundCIP")
	public static class Round< T extends RealType<T> , U extends RealType<U> > extends AbstractUnaryComputerOp<T, U>
	{
		@Override
		public void compute(final T input1, final U output) {
			output.setReal(  Math.round( input1.getRealDouble() )  );
		}
	}

	@Plugin(type = Op.class , name = "Math1OperationFloorCIP")
	public static class Floor< T extends RealType<T> , U extends RealType<U> > extends AbstractUnaryComputerOp<T, U>
	{
		@Override
		public void compute(final T input1, final U output) {
			output.setReal(  Math.floor( input1.getRealDouble() )  );
		}
	}

	@Plugin(type = Op.class , name = "Math1OperationCeilCIP")
	public static class Ceil< T extends RealType<T> , U extends RealType<U> > extends AbstractUnaryComputerOp<T, U>
	{
		@Override
		public void compute(final T input1, final U output) {
			output.setReal(  Math.ceil( input1.getRealDouble() )  );
		}
	}

	@Plugin(type = Op.class , name = "Math1OperationSignCIP")
	public static class Sign< T extends RealType<T> , U extends RealType<U> > extends AbstractUnaryComputerOp<T, U>
	{
		@Override
		public void compute(final T input1, final U output) {
			output.setReal(  Math.signum( input1.getRealDouble() )  );
		}
	}

	
}
