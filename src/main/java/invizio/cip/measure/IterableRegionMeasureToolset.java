package invizio.cip.measure;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ij.gui.Roi;
import invizio.cip.Regions;
import invizio.cip.parameters.DefaultParameter2;
import net.imagej.ops.OpService;
import net.imagej.ops.geom.geom3d.mesh.Mesh;
import net.imglib2.RealLocalizable;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.roi.geometric.Polygon;



public class IterableRegionMeasureToolset<B extends BooleanType<B>> extends AbstractMeasureToolset<IterableRegion<B>,B>{

	private static final long serialVersionUID = 1L;
	
	OpService op;

	//private boolean useImageUnit;
	//Mesh mesh;
	//Polygon polygon;
	
	public IterableRegionMeasureToolset( OpService op ) //, boolean useImageUnit )
	{
		//this.useImageUnit = useImageUnit;
		this.op = op;
		
		add( new PositionMeasureTool<IterableRegion<B>,B>() );
		add( new BoundarySizeMeasureTool<IterableRegion<B>,B>() );
		add( new SizeMeasureTool<IterableRegion<B>,B>() );
	}
		
	
//	private void geometry( IterableRegion<B> region )
//	{
//		int nDim = region.numDimensions();
//		if( nDim==2 ) {
//			boolean useJacobs = false;
//			polygon = op.geom().contour(region, useJacobs);
//		}
//		if( nDim==3 ) {
//			mesh = op.geom().marchingCubes( region );
//		}
//			
//	}
	
	
	
	public class PositionMeasureTool<N extends IterableRegion<C>, C extends BooleanType<C>> extends AbstractMeasureTool<N,C> {

		PositionMeasureTool( ){	
			name = "position";
			inputType = AbstractMeasureToolbox.MeasurableType.REGION;
			outputType = DefaultParameter2.Type.scalars;
		}
		
		@Override
		public Measure measure(N measurable) {
			
			List<String> axes = Arrays.asList( "X" , "Y" , "Z" , "T" ); 
			int nDim = measurable.numDimensions();
			Map<String,Double> result = new HashMap<String,Double>();
			RealLocalizable position = op.geom().centroid( measurable );
			
			for( int d=0; d<nDim; d++ ) {
				result.put( axes.get(d) , position.getDoublePosition(d) );
			}
			
			return new Measure( name , outputType, result);			
		}
	}
	
	
	
	public class BoundarySizeMeasureTool<N extends IterableRegion<C>, C extends BooleanType<C>> extends AbstractMeasureTool<N,C> {
		
		//IterableRegionMeasureToolset<C> toolset;
		
		BoundarySizeMeasureTool() { // IterableRegionMeasureToolset<C> toolset ){	
			name = "boundary";
			inputType = AbstractMeasureToolbox.MeasurableType.REGION;
			outputType = DefaultParameter2.Type.scalar;
			//this.toolset = toolset;
		}
		
		@Override
		public Measure measure(N region) {
			
			// // opService should trigger the necessary conversion (did not work, or only for labelRegion)
			// Double value = ( (DoubleType) op.run("geom.boundarySize", measurable ) ).getRealDouble();
			
			Double value = null;
			int nDim = region.numDimensions();
			if ( nDim == 2 ) {
				//boolean useJacobs = true;
				//Polygon polygon = op.geom().contour(region, useJacobs); // works only for connected component
				//value = op.geom().boundarySize(polygon).getRealDouble();
				List<Roi> roiList = Regions.toIJ1ROI(region);
				value = roiList.get(0).getLength();		
			}
			else if( nDim == 3 ) {
				Mesh mesh = op.geom().marchingCubes( region ); // to check but marching cube should work fine with arbitrary mask, 
				value = op.geom().boundarySize(mesh).getRealDouble();
			}
			
			// if one would need to take pixel size into account,
			// in general one would need to create adequate geometry (with unit pixel size ops algo)
			// and reposition mesh/poly points according to pixel size 
			// before doing the measure
			
			return new Measure( name , outputType, value);
			
		}
	}

	
	
	public class SizeMeasureTool<N extends IterableRegion<C>, C extends BooleanType<C>> extends AbstractMeasureTool<N,C> {

		SizeMeasureTool( ){	
			name = "size";
			inputType = AbstractMeasureToolbox.MeasurableType.REGION;
			outputType = DefaultParameter2.Type.scalar;
		}
		
		@Override
		public Measure measure(N measurable) {
			
			// opService should trigger the necessary conversion
			Double value = ( (DoubleType) op.run("geom.size", measurable ) ).getRealDouble();
			
			// if one would need to take pixel size into account,
			// in general one would need to create adequate geometry (with unit pixel size algo)
			// and reposition mesh/poly points according to pixel size 
			// before doing the measure
			
			return new Measure( name , outputType, value);			
		}
	}

	
	
	
}
