package invizio.cip.region;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import invizio.cip.CIPService;
import invizio.cip.RaiCIP2;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.filter.ThresholdToSelection;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.roi.util.IterableRandomAccessibleRegion;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;



/**
 * 
 * @author Benoit Lombardot
 *
 */


public class Regions {

	
	
	public static <B extends BooleanType<B>, T extends RealType<T>> Object toIterableRegion( Object image, CIPService cipService )
	{
    	RaiCIP2<T> raiCIP = cipService.toRaiCIP( image );
    	T valT = raiCIP.randomAccess().get();
    	if( valT instanceof BooleanType )
    	{
    		RandomAccessibleInterval<B> mask = (RandomAccessibleInterval<B>)raiCIP;
    		return Regions.maskToIterableRegion( mask );
    	}
    	else {
    		return Regions.labelMapToIterableRegions( raiCIP );    		
    	}
	}
	
	
	// use realtype rather than integertype as input for flexibility reason 
	private static <B extends BooleanType<B>, T extends RealType<T>> List<IterableRegion<B>> labelMapToIterableRegions( RandomAccessibleInterval<T> labelMap )
	{
		// create an imgLabeling
		
		
		Img<IntType> imgStorage = Util.getArrayOrCellImgFactory( labelMap, new IntType()).create(labelMap, new IntType() );
		ImgLabeling<Integer,IntType> imgLabeling = new ImgLabeling<Integer,IntType>( imgStorage );
		
		Cursor<T> cMap = Views.iterable( labelMap ).cursor();
		RandomAccess< LabelingType<Integer> > labelingRA = imgLabeling.randomAccess();
		while( cMap.hasNext() )
		{
			T valT = cMap.next();
			if( valT.getRealFloat() != 0 )
			{
				labelingRA.setPosition( cMap );
				LabelingType<Integer> labelingType = labelingRA.get();
				labelingType.add( (int) valT.getRealFloat() );
			}
		}
		
		// convert list of LabelRegions
		List< IterableRegion<B> > regions = new ArrayList< IterableRegion<B> >();
		LabelRegions<Integer> labelRegions = new LabelRegions<Integer>( imgLabeling );
		Set<Integer> labels = labelRegions.getExistingLabels();
		
		for( Integer label : labels )
			regions.add( (IterableRegion<B>) labelRegions.getLabelRegion(label) );
		
		return regions;
	}
	
	
	
	private static <B extends BooleanType<B>> IterableRegion<B> maskToIterableRegion( RandomAccessibleInterval<B> mask )
	{
		// create an IterableRandomAccessibleRegion
		return  IterableRandomAccessibleRegion.create( mask );
	}


	
	
	public static <B extends BooleanType<B>> List<List<Roi>> toIJ1ROI( List<IterableRegion<B>> regions , CIPService cipService )
	{
		List<List<Roi>> roiListPerRegion = new ArrayList<List<Roi>>();
		for(IterableRegion<B> region : regions)
		{
			roiListPerRegion.add( toIJ1ROI( region , cipService ) );
		}
		return roiListPerRegion;
	}
	
	// TODO: add toIterableRegion(List<Roi>) toIterableRegion(List<List<Roi>>)
	
	
	
	
	public static <B extends BooleanType<B>> List<Roi> toIJ1ROI( IterableRegion<B> region , CIPService cipService )
	{
		// convert the region to an image plus mask
		int nDim = region.numDimensions();
		ImagePlus impReg = cipService.toImagegPlus( (RandomAccessibleInterval<B>) region );
		IJ.setThreshold(impReg, 0, 0.5);
		ThresholdToSelection roiMaker = new ThresholdToSelection();
		
		int x0 = (int)region.min(0); // region have no information on axes type so we default x,y to the 2 first axis
		int y0 = (int)region.min(1); // have region metadata would allow to keep track of that
		int z0 = 0;
		if( nDim>=2 )
		{
			z0 = (int)region.min(2);
		}
		
		
		// Iterate on the plane of the imageplus and create a roi on each plane
		List<Roi> roiList = new ArrayList<Roi>();
		
		int nSlice = impReg.getStackSize();
		if( nSlice==1 )
		{
			Roi roi = roiMaker.convert( impReg.getProcessor() );
			roi.setLocation(x0, y0);
			roi.setPosition( z0 + 1 );
			roiList.add(roi);
		}
		else
		{
			ImageStack stack = impReg.getStack();
			for(int i=1; i<=nSlice; i++)
			{
				Roi roi = roiMaker.convert( stack.getProcessor(i) );
				roi.setLocation(x0, y0);
				roi.setPosition( z0 + i );
				roiList.add(roi);
			}
		}
		
		return roiList;
	}
	
	
	
}
