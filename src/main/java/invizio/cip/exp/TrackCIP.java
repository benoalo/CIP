package invizio.cip.exp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import invizio.cip.CIP;
import net.imagej.ImageJ;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;


import ij.IJ;
import ij.ImagePlus;

//trackmate
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTracker;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.tracking.TrackerKeys; 
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.graph.ConvexBranchesDecomposition;
import fiji.plugin.trackmate.graph.TimeDirectedNeighborIndex;
import fiji.plugin.trackmate.graph.ConvexBranchesDecomposition.TrackBranchDecomposition;
import fiji.plugin.trackmate.Model;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;


/**
 * 
 * @author Benoit Lombardot
 *
 */

/*
 * minimal trackmate implementation taking a list of labelmap as input
 * and returning a list of tracks and a track graph
 * 
 * TODO:
 *  - pass pixel size => already using image pixel size (image dt is not tracked)
 *  - measure basic track features
 *  - measures basic spot features (allow to pass an image for intensity measures)
 *  - track along a user specified dimension, rather than using a list of labelmap ==> should be able to label slice by slice
 */


@Plugin(type = Op.class, name="TrackCIP", headless = true)  // & NativeType<T> 
public class TrackCIP  < T extends RealType<T> , U extends RealType<U>  > extends AbstractOp {

	
	@Parameter (type = ItemIO.INPUT)
	protected List<RandomAccessibleInterval<T>> inputImages;
	
	@Parameter( label="radius", persist=false, required=false ) // does trackmate handles anisotropic data
	protected Double radius;

	@Parameter( label="gap_frame", persist=false, required=false ) 
	protected Integer gap_frame;
	
	@Parameter( label="gap_radius", persist=false, required=false ) 
	protected Double gap_radius;
	
	@Parameter( label="split", persist=false, required=false ) 
	protected Boolean split;
	
	@Parameter( label="merge", persist=false, required=false ) 
	protected Boolean merge;
	
	@Parameter( label="output", choices={"measure","trackmate","all"}, persist=false, required=false ) 
	protected String output;
	
	/*
	@Parameter( label="display image", persist=false, required=false ) 
	protected RandomAccessibleInterval<U> display_image;
	*/
	
	@Parameter (type = ItemIO.OUTPUT)
	private Map<String, List<Object> > tracksData;
	
	@Parameter (type = ItemIO.OUTPUT)
	private Model trackmateModel;
	
	
	//@Parameter
	//private CIP cip;
	
	//private OpService op;
	
	
	
	@Override
	public void run() {
		
		
		if( gap_radius == null  || gap_radius<=0)
			gap_radius = radius;
		if( gap_frame == null || gap_radius<0)
			gap_frame = 0;
		if( split == null )
			split = false;
		if( merge == null )
			merge = false;
		if( output == null )
			output = "measure";
			
			
		
		//////////////////////////////////////////////////////////////////
		// create list of spots a each time step /////////////////////////
		//////////////////////////////////////////////////////////////////
		CIP cip = new CIP();
		cip.setContext( this.ops().getContext() );
		cip.setEnvironment( this.ops() );
		
		SpotCollection spotCollection = new SpotCollection();
		int nDim = inputImages.get(0).numDimensions();
		List<String> axes = cip.axes( inputImages.get(0) );
		int nFrame = inputImages.size();
		for(int t=0 ; t<nFrame ; t++ )
		{
			RandomAccessibleInterval<T> img = inputImages.get(t);
			Object regions = cip.region( img );
			
			// measure position using pixel size using 
			@SuppressWarnings("unchecked")
			LinkedHashMap<String,List<Object>> measures = (LinkedHashMap<String,List<Object>>) cip.measure(regions , CIP.list("position","size") );
			
			int nRegion = measures.get("object").size();
			
			for( int i=0 ; i<nRegion ; i++ )
			{
				double size = (Double) measures.get("size").get(i);
				double radius = Math.sqrt( size/Math.PI );
				
				double[] pos = new double[3];
				for( int j=0 ; j<3; j++) {
					if( j<nDim )
						pos[j] = (Double) measures.get("position_"+axes.get(j)).get(i);
					else
						pos[j] = 1.0;
				}
				
				double quality = 1.0;
				String name = "region "+i+" , time "+t; 
				
				Spot spot = new Spot( pos[0], pos[1], pos[2], radius, quality, name );
				spotCollection.add(spot, t);
			}	
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// do the sport tracking with trackmate LAP tracker ///////////////////////
		///////////////////////////////////////////////////////////////////////////
		
		Map< String, Object > settings = new HashMap< String, Object >();
		settings.put( TrackerKeys.KEY_LINKING_MAX_DISTANCE, radius );
		settings.put( TrackerKeys.KEY_LINKING_FEATURE_PENALTIES , new HashMap<String,Double>() );
		settings.put( TrackerKeys.KEY_ALTERNATIVE_LINKING_COST_FACTOR,  1.05 );
		settings.put( TrackerKeys.KEY_CUTOFF_PERCENTILE, TrackerKeys.DEFAULT_CUTOFF_PERCENTILE );
		
		settings.put( TrackerKeys.KEY_ALLOW_GAP_CLOSING, (gap_frame>0) );
		settings.put( TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE, gap_radius );
		settings.put( TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, gap_frame );
		
		settings.put( TrackerKeys.KEY_ALLOW_TRACK_SPLITTING, split );
		settings.put( TrackerKeys.KEY_SPLITTING_MAX_DISTANCE, gap_radius );
		settings.put( TrackerKeys.KEY_ALLOW_TRACK_MERGING, merge);
		settings.put( TrackerKeys.KEY_MERGING_MAX_DISTANCE, gap_radius );
		
		// optional
		//settings.put( TrackerKeys.KEY_LINKING_FEATURE_PENALTIES, );
		//settings.put( TrackerKeys.KEY_GAP_CLOSING_FEATURE_PENALTIES, );
		//settings.put( TrackerKeys.KEY_SPLITTING_FEATURE_PENALTIES , );
		//settings.put( TrackerKeys.KEY_MERGING_FEATURE_PENALTIES, );
		//settings.put( TrackerKeys.KEY_BLOCKING_VALUE,    ); 
		
		SparseLAPTracker tracker = new SparseLAPTracker( spotCollection,  settings );
		tracker.process();
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// extract tracking information  ///////////////////////
		///////////////////////////////////////////////////////////////////////////

		SimpleWeightedGraph< Spot, DefaultWeightedEdge > graph = tracker.getResult();
		
		Model model= new Model();
		model.beginUpdate();
		
		model.setSpots( spotCollection, false );
		model.setTracks( graph, false );
		
		model.endUpdate();
		
		TrackModel trackModel  = model.getTrackModel();
		final TimeDirectedNeighborIndex neighborIndex = trackModel.getDirectedNeighborIndex();

		tracksData = new LinkedHashMap<String, List<Object> >();
		tracksData.put( "trackid" , 	new ArrayList<Object>() );
		tracksData.put( "branchid" , 	new ArrayList<Object>() );
		tracksData.put( "spotid" , 		new ArrayList<Object>() );
		tracksData.put( "x" , 			new ArrayList<Object>() );
		tracksData.put( "y" , 			new ArrayList<Object>() );
		tracksData.put( "z" , 			new ArrayList<Object>() );
		tracksData.put( "frame" , 		new ArrayList<Object>() );
		tracksData.put( "inbranch" , 	new ArrayList<Object>() );
		tracksData.put( "outbranch" , 	new ArrayList<Object>() );
		tracksData.put( "generation" , 	new ArrayList<Object>() );
		
		// for each track (branching tree, or branch graph)
		for(Integer trackid : trackModel.trackIDs( false ) )
		{
			//for each branch in the track (branch graph)
			final TrackBranchDecomposition branchDecomposition = ConvexBranchesDecomposition.processTrack( trackid, trackModel, neighborIndex, true, false );
			final SimpleDirectedGraph< List< Spot >, DefaultEdge > branchGraph = ConvexBranchesDecomposition.buildBranchGraph( branchDecomposition );
			
			// initialize generation of branch
			Queue<List<Spot>> branchQueue = new LinkedList<List<Spot>>();
			Map<List<Spot>, Integer> branchids = new HashMap<List<Spot>, Integer>();
			getStartingBranches( branchGraph, branchids, branchQueue );
			Map<List<Spot>, Integer> generation = getBranchGeneration(branchGraph,  branchQueue);
			
			// initialize exploration of the track branches
			branchQueue = new LinkedList<List<Spot>>(); 
			branchids = new HashMap<List<Spot>, Integer>();
			int branchid = getStartingBranches( branchGraph, branchids, branchQueue );
			while ( !branchQueue.isEmpty() )
			{
				
				List<Spot> branch = branchQueue.poll();
				
				//  update branch queue and give an id to each branch
				List<Integer> outbranch = new ArrayList<Integer>();
				int nChildBranch = branchGraph.outDegreeOf(branch);
				if (nChildBranch > 0 )
				{	for(DefaultEdge edge : branchGraph.outgoingEdgesOf(branch) )
					{	List<Spot> childBranch = branchGraph.getEdgeTarget(edge);
						branchQueue.add(childBranch);
						branchid++;
						branchids.put(childBranch, branchid);
						outbranch.add(branchid);
					}
				}
				
				List<Integer> inbranch = new ArrayList<Integer>(); 
				int nParentBranch = branchGraph.inDegreeOf(branch);
				if (nParentBranch > 0 )
				{	for(DefaultEdge edge : branchGraph.incomingEdgesOf(branch) )
					{	List<Spot> parentBranch = branchGraph.getEdgeSource(edge);
						inbranch.add(branchids.get(parentBranch) );
					}
				}
				
				int cur_branchid = branchids.get(branch);
				int gen = generation.get(branch); 
				
						
				// for each spot write down information in the dictionary use to output tracking data
				for(Spot spot : branch )
				{
					tracksData.get("trackid").add(trackid);
					tracksData.get("branchid").add(cur_branchid);
					tracksData.get("spotid").add(spot.ID());
					tracksData.get("x").add(spot.getFeature("POSITION_X"));
					tracksData.get("y").add(spot.getFeature("POSITION_Y"));
					tracksData.get("z").add(spot.getFeature("POSITION_Z"));
					tracksData.get("frame").add(spot.getFeature("FRAME")+1);
					tracksData.get("inbranch").add(inbranch);
					tracksData.get("outbranch").add(outbranch);
					tracksData.get("generation").add(gen);
					
				}

				
			}
		}
		
		
		switch( output) {
		case "measure":
			// tracksData is already assigned
			break;
		case "trackmate":
			tracksData = null;
			trackmateModel = model;
			break;
		case "all":
			//trackData is already assigned
			trackmateModel = model;
			break;
		default: // same as measure
			//trackData is already assigned
			break;
		}
		
		
	}
	
	
	private int getStartingBranches(SimpleDirectedGraph< List< Spot >, DefaultEdge > branchGraph, Map<List<Spot>, Integer> branchids, Queue<List<Spot>> branchQueue)
	{
		// find starting branches
		//Queue<List<Spot>> branchQueue = new LinkedList<List<Spot>>(); 
		int branchid=0;
		for ( final List< Spot > branch : branchGraph.vertexSet() ){
			if( branchGraph.inDegreeOf(branch) == 0 ){
				branchQueue.add(branch);
				branchid++;
				branchids.put(branch, branchid);
			}
		}
		return branchid;
	}
	
	
	private Map<List<Spot>, Integer> getBranchGeneration(SimpleDirectedGraph< List< Spot >, DefaultEdge > branchGraph, Queue<List<Spot>> branchQueue ){
		
		Map<List<Spot>, Integer> generation = new HashMap<List<Spot>, Integer>();
		for ( final List< Spot > branch : branchQueue ){
				generation.put(branch,0);
		}
		// all branch generation
		while ( !branchQueue.isEmpty() )
		{
			List<Spot> branch = branchQueue.poll();
			int gen = generation.get(branch); 
			
			// retrieve descendant branches
			if ( branchGraph.outDegreeOf(branch) > 0 ){
				for(DefaultEdge edge : branchGraph.outgoingEdgesOf(branch) ){
					List<Spot> childBranch = branchGraph.getEdgeTarget(edge);
					generation.put( childBranch, gen+1 );
					branchQueue.add( childBranch);
				}
			}
		}
		return generation;
	}
	
	
	
	public static void main(final String... args)
	{	
		
		ImageJ ij = new ImageJ();
		ij.ui().showUI();
		
		ImagePlus imp = IJ.openImage(	"F:/projects/Benoit_test/tracking_ops/data/FakeTracks_thresholded.tif" 	);
		
		CIP cip = new CIP();
		cip.setContext( ij.getContext() );
		cip.setEnvironment( ij.op() );
		
		ij.ui().show(imp);
		
		
		int maxT = imp.getDimensions()[4];

		List<Object> labelmaps = new ArrayList<Object>();
		for( int t=0; t<maxT; t++ )
		{

			Object img = cip.slice( imp , 2, t);
			labelmaps.add( cip.label(img, 128) );
		}
		//tracksdata = cip.track( labelmaps, 'method', 'LAP', 'radius', 10, 'gap', 2)
		float radius=10.0f;
		//float gap_frame=2.0f;
		//float gap_radius=15.0f;
		//boolean split=true;
		//boolean merge=false;
		//boolean display = true;
		ImagePlus imp_display = IJ.openImage(	"F:/projects/Benoit_test/tracking_ops/data/FakeTracks.tif" 	);
		//Img<FloatType> img_display = ImageJFunctions.wrap(imp_display);
		//Map<String, List<Object>> tracksdata =  (Map<String, List<Object>>)  ij.op().run( "TrackCIP", labelmaps, radius, gap_frame, gap_radius, split, merge, cip);
		
		@SuppressWarnings("unchecked")
		List<Object> results = (List<Object>) cip.track(labelmaps, radius,"gap frame", 2, "output", "all");
		
		cip.show(results.get(0), "tracks data" );
		cip.show(results.get(1), imp_display );

		//print labelmaps
		cip.show(labelmaps.get(25), "glasbey_on_dark");
		
	}
	
	
}
