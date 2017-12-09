package invizio.cip.measure;


import java.awt.Polygon;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.process.FloatPolygon;
import invizio.cip.RegionCIP;
import invizio.cip.Regions;
import invizio.cip.parameters.DefaultParameter2;
import net.imagej.ops.OpService;
import net.imagej.ops.geom.geom3d.mesh.DefaultMesh;
import net.imagej.ops.geom.geom3d.mesh.Facet;
import net.imagej.ops.geom.geom3d.mesh.Mesh;
import net.imagej.ops.geom.geom3d.mesh.TriangularFacet;
import net.imagej.ops.geom.geom3d.mesh.Vertex;
import net.imglib2.RealLocalizable;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.real.DoubleType;
//import net.imglib2.roi.geometric.Polygon;




public class RegionCIPMeasureToolset<B extends BooleanType<B>> extends AbstractMeasureToolset<RegionCIP<B>,B>{

	private static final long serialVersionUID = 1L;
	
	OpService op;

	//private boolean useImageUnit;
	//Mesh mesh;
	//Polygon polygon;
	
	public RegionCIPMeasureToolset( OpService op ) //, boolean useImageUnit )
	{
		this.op = op;
		
		add( new PositionMeasureTool<RegionCIP<B>,B>() );
		add( new BoundarySizeMeasureTool<RegionCIP<B>,B>() );
		add( new SizeMeasureTool<RegionCIP<B>,B>() );
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
	
	
	
	public class PositionMeasureTool<N extends RegionCIP<C>, C extends BooleanType<C>> extends AbstractMeasureTool<N,C> {

		PositionMeasureTool( ){	
			name = "position";
			inputType = AbstractMeasureToolbox.MeasurableType.REGION;
			outputType = DefaultParameter2.Type.scalars;
		}
		
		@Override
		public Measure measure(N measurable) {
			
			List<String> axes = measurable.axes(); 
			int nDim = measurable.numDimensions();
			Map<String,Double> result = new HashMap<String,Double>();
			RealLocalizable position = op.geom().centroid( measurable );
			
			for( int d=0; d<nDim; d++ ) {
				result.put( axes.get(d) , position.getDoublePosition(d) );
			}
			
			return new Measure( name , outputType, result);			
		}
	}
	
	
	
	public class BoundarySizeMeasureTool<N extends RegionCIP<C>, C extends BooleanType<C>> extends AbstractMeasureTool<N,C> {
		
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
			
			List<Double> spacing = region.spacing();
			
			Double value = null;
			int nDim = region.numDimensions();
			if ( nDim == 2 ) {
				//boolean useJacobs = true;
				//Polygon polygon = op.geom().contour(region, useJacobs); // works only for connected component
				//value = op.geom().boundarySize(polygon).getRealDouble();
				List<Roi> roiList = Regions.toIJ1ROI(region);
				Roi[] roiList2;
				if ( roiList.get(0) instanceof ShapeRoi )
					roiList2 = ((ShapeRoi) roiList.get(0)).getRois();
				else
					roiList2 = new Roi[] { roiList.get(0) };
				
				value=0d;
				final double sx = spacing.get(0);
				final double sy = spacing.get(1);
				for( Roi roi : roiList2 ) {
					FloatPolygon poly = roi.getFloatPolygon();
					for(int i=0 ; i<poly.npoints-1 ; i++ ) {
						final double x0 = poly.xpoints[i];
						final double y0 = poly.ypoints[i];
						final double x1 = poly.xpoints[i+1];
						final double y1 = poly.ypoints[i+1];
						final double dx = (x1-x0)*sx;
						final double dy = (y1-y0)*sy;
						value += Math.sqrt( dx*dx + dy*dy ); 
					}
				}
			}
			else if( nDim == 3 ) {
				Mesh mesh = op.geom().marchingCubes( region ); // to check but marching cube should work fine with arbitrary mask, 
				
				final double sx = spacing.get(0);
				final double sy = spacing.get(1);
				final double sz = spacing.get(2);
				
				DefaultMesh mesh2 = new DefaultMesh();
				for( Facet facet : mesh.getFacets() ) {
					final TriangularFacet tfacet = (TriangularFacet)facet;
					final Vertex v0 = (Vertex) tfacet.getP0();
					final Vertex v0new = new Vertex( v0.getX()*sx, v0.getY()*sy, v0.getZ()*sz );
					final Vertex v1 = (Vertex) tfacet.getP1();
					final Vertex v1new = new Vertex( v1.getX()*sx, v1.getY()*sy, v1.getZ()*sz );
					final Vertex v2 = (Vertex) tfacet.getP2();
					final Vertex v2new = new Vertex( v2.getX()*sx, v2.getY()*sy, v2.getZ()*sz );
					
					mesh2.addFace( new TriangularFacet(v0new, v1new, v2new ) );
				}
				value = mesh2.getSurfaceArea();//op.geom().boundarySize(mesh2).getRealDouble();
			}
			
			// if one would need to take pixel size into account,
			// in general one would need to create adequate geometry (with unit pixel size ops algo)
			// and reposition mesh/poly points according to pixel size 
			// before doing the measure
			
			return new Measure( name , outputType, value);
			
		}
	}

	
	
	public class SizeMeasureTool<N extends RegionCIP<C>, C extends BooleanType<C>> extends AbstractMeasureTool<N,C> {

		SizeMeasureTool( ){	
			name = "size";
			inputType = AbstractMeasureToolbox.MeasurableType.REGION;
			outputType = DefaultParameter2.Type.scalar;
		}
		
		@Override
		public Measure measure(N measurable) {
			
			// opService should trigger the necessary conversion
			Double value = ( (DoubleType) op.run("geom.size", measurable ) ).getRealDouble();
			
			int nDim = measurable.numDimensions();
			List<Double> spacing = measurable.spacing();
			double pixSize = 1;
			for(int d=0; d<nDim; d++) {
				pixSize *= spacing.get(d);
			}
			
			value *= pixSize;
			
			// if one would need to take pixel size into account,
			// in general one would need to create adequate geometry (with unit pixel size algo)
			// and reposition mesh/poly points according to pixel size 
			// before doing the measure
			
			return new Measure( name , outputType, value);			
		}
	}

	
	
	
}
