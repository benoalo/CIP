package invizio.cip.parameters;


import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsPeriodicFactory;
import net.imglib2.type.numeric.RealType;


/**
 * 
 * @author Benoit Lombardot
 *
 */


public class Format {

	// format a numeric array describing a parameter per dimension of an image/matrix
	public static Float[] perDim(Float[] pixelSize, int nDim )
	{
		
		if( pixelSize == null ) {
			pixelSize = new Float[nDim];
			for(int d=0 ; d<nDim ; d++) {
				pixelSize[d] = 1f;
			}
		}
		else if( pixelSize.length == 1 )
		{
			Float pixelSize0 = pixelSize[0];
			pixelSize = new Float[nDim];
			for(int d=0 ; d<nDim ; d++) {
				pixelSize[d] = pixelSize0;
			}
		}
		else if( pixelSize.length < nDim )
		{
			pixelSize = null;
			//TODO: Error, the pixelSize is not consistent with the image dimension ( pixelSize.length vs. nDim )
		}
		else if( pixelSize.length > nDim )
		{
			Float[] pixelSize0 = pixelSize;
			pixelSize = new Float[nDim];
			for(int d=0 ; d<nDim ; d++) {
				pixelSize[d] = pixelSize0[d];
			}
			
			//TODO: Warning! to many elements in pixelSize, only the nDim first will be used 
		}
		
		return pixelSize;
	}
	
	
	public static <T extends RealType<T>> OutOfBoundsFactory<T,RandomAccessibleInterval<T>>  outOfBoundFactory(String method, T valueT )
	{
		
		OutOfBoundsFactory<T,RandomAccessibleInterval<T>> outfBoundFactory = null;
		switch( method.toLowerCase() ) {
		
		case "zeros":
			outfBoundFactory = new OutOfBoundsConstantValueFactory<T,RandomAccessibleInterval<T>>( valueT);
			break;
			
		case "value":
			outfBoundFactory = new OutOfBoundsConstantValueFactory<T,RandomAccessibleInterval<T>>( valueT );
			break;
						
		case "same":
			outfBoundFactory = new OutOfBoundsBorderFactory<T,RandomAccessibleInterval<T>>();
			break;
			
		case "periodic":
			outfBoundFactory = new OutOfBoundsPeriodicFactory<T,RandomAccessibleInterval<T>>();
			break;
			
		case "mirror":
			outfBoundFactory = new OutOfBoundsMirrorFactory<T,RandomAccessibleInterval<T>>( OutOfBoundsMirrorFactory.Boundary.SINGLE );
			break;
		
		default: // "mirror":
			outfBoundFactory = new OutOfBoundsMirrorFactory<T,RandomAccessibleInterval<T>>( OutOfBoundsMirrorFactory.Boundary.SINGLE );
			break;
			
		}
		
		return outfBoundFactory;
	}
	
	
	

}
