package invizio.cip.misc;



import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;
import net.imagej.ImageJ;

import net.imagej.ops.AbstractOp;
import net.imagej.ops.OpService;
import net.imagej.ops.Op;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;


/**
 * 
 * @author Benoit Lombardot
 *
 */


// TODO
//	[-] control the number of threads used
//	[-] check whether the ops implementation used is the fastest available, ideally it should use the imglib2 implementation
	
	@Plugin(type = Op.class, name="CreateCIP", headless = true)
	public class CreateCIP  < T extends RealType<T> > extends AbstractOp 
	{
		
		
		
		@Parameter (label="size", type = ItemIO.INPUT)
		private Long[] dimensions;
		
		@Parameter( label="value", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private Float value;

		@Parameter( label="type", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private String type;
		
		@Parameter (label="origin" , type = ItemIO.INPUT)
		private Long[] origin;
		
		@Parameter (type = ItemIO.OUTPUT)
		private	RandomAccessibleInterval<T> outputImage;
		
		
		@Parameter
		OpService op;
		
		
		
		
		
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			
			if( dimensions == null )
			{	//TODO: error message
				return;
			}
			if( dimensions.length == 1 )
			{	
				dimensions = new Long[] {dimensions[0] , dimensions[0]};
			}
			int nDim = dimensions.length;
			long[] dims = new long[nDim];
			for(int d=0; d<nDim; d++)
				dims[d] = dimensions[d];
			
			if( type==null )
				type="float";
			type = type.toLowerCase().trim();
			
			if( value==null )
				value = 0f;
			
			if( origin == null || origin.length<dimensions.length )
			{
				origin = new Long[nDim];
				for(int d=0; d<nDim ; d++)
					origin[d] = 0l;
			}
			long[] offset = new long[nDim];
			long[] translation = new long[nDim];
			long minOffset = Long.MAX_VALUE;
			
			for(int d=0; d<nDim ; d++)
			{
				offset[d] = -origin[d];
				translation[d] = origin[d];
				if( Math.abs(offset[d])>minOffset)
					minOffset = Math.abs(offset[d]); 
			}
			
			
			T valueT = null;
			Img<T> img = null;
			
			if( type.equals("bittype") || type.equals("bit") || type.equals("boolean") || type.equals("booltype") )
			{
				img = (Img<T>) op.create().img( FinalDimensions.wrap(dims) , new BitType() );
				valueT = (T) new BitType();
			}
			else if( type.equals("byte") || type.equals("bytetype") || type.equals("int8") )
			{
				img = (Img<T>) op.create().img( FinalDimensions.wrap(dims) , new ByteType() );
				valueT = (T) new ByteType();
			}	
			else if( type.equals("unsignedbyte") || type.equals("unsignedbytetype") || type.equals("uint8") || type.equals("ubyte"))
			{
				img = (Img<T>) op.create().img( FinalDimensions.wrap(dims) , new UnsignedByteType() );
				valueT = (T) new UnsignedByteType();
			}	
			else if( type.equals("short") || type.equals("shorttype") || type.equals("int16") )
			{
				img = (Img<T>) op.create().img( FinalDimensions.wrap(dims) , new ShortType() );
				valueT = (T) new ShortType();
			}	
			else if( type.equals("unsignedshort") || type.equals("unsignedshorttype") || type.equals("uint16") || type.equals("ushort") )
			{
				img = (Img<T>) op.create().img( FinalDimensions.wrap(dims) , new UnsignedShortType() );
				valueT = (T) new UnsignedShortType();
			}	
			else if( type.equals("int") || type.equals("inttype") || type.equals("int32") )
			{
				img = (Img<T>) op.create().img( FinalDimensions.wrap(dims) , new IntType() );
				valueT = (T) new IntType();
			}	
			else if( type.equals("unsignedint") || type.equals("unsignedinttype") || type.equals("uint32") || type.equals("uint"))
			{
				img = (Img<T>) op.create().img( FinalDimensions.wrap(dims) , new UnsignedIntType() );
				valueT = (T) new UnsignedIntType();
			}	
			else if( type.equals("float") || type.equals("floattype") || type.equals("float32")  || type.equals("single"))
			{
				img = (Img<T>) op.create().img( FinalDimensions.wrap(dims) , new FloatType() );
				valueT = (T) new FloatType();
			}	
			else if( type.equals("double") || type.equals("doubletype") || type.equals("float64") )
			{
				img = (Img<T>) op.create().img( FinalDimensions.wrap(dims) , new DoubleType() );
				valueT = (T) new DoubleType();
			}	
			else if( type.equals("long") || type.equals("longtype") || type.equals("int64") )
			{
				img = (Img<T>) op.create().img( FinalDimensions.wrap(dims) , new LongType() );
				valueT = (T) new LongType();
			}	
			else if( type.equals("unsignedlong") || type.equals("unsignedlongtype") || type.equals("uint64") || type.equals("ulong") )
			{
				img = (Img<T>) op.create().img( FinalDimensions.wrap(dims) , new UnsignedLongType() );
				valueT = (T) new UnsignedLongType();
			}	
			else {
				//TODO: error message
				return;
			}
			
			
			
			
			if( value != 0f )
			{	
				valueT.setReal( value );	
				outputImage = (RandomAccessibleInterval<T>) op.math().add(  img, valueT  );
			}
			else {
				outputImage = img;
			}
			
			// translate the orgin if needed
			if( minOffset>0 ) {
				//outputImage = Views.offset(outputImage, offset);
				outputImage = Views.translate(outputImage, translation);
			}
			
			
		}



		
		
		public static void main(final String... args)
		{
			
			ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			ImagePlus imp = IJ.openImage(	CIP.class.getResource( "/blobs32.tif" ).getFile()	);
			ij.ui().show(imp);
			
			
			Img<UnsignedByteType> img = ImageJFunctions.wrap(imp);
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			
			@SuppressWarnings("unchecked")
			RandomAccessibleInterval<UnsignedByteType> output = (RandomAccessibleInterval<UnsignedByteType>)
									cip.create( img , 10  );
					
			//cip.create( cip.aslist(100, 50, 2) , 10, "double"  );
			
			String str = output==null ? "null" : output.toString();
			
			System.out.println("hello create image:" + str );
			ij.ui().show(output);
			
			System.out.println("done!");
		}
		
	
}
