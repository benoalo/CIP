package invizio.cip.misc;

import java.awt.Color;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.ui.UIService;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import fiji.plugin.trackmate.visualization.trackscheme.TrackScheme;
import fiji.plugin.trackmate.visualization.PerTrackFeatureColorGenerator;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.WindowManager;
import ij.text.TextWindow;
import ij.gui.Overlay;
import ij.gui.Roi;
import invizio.cip.CIPService;
import invizio.cip.Regions;
import invizio.cip.parameters.FunctionParameters2;
import net.imagej.ImageJService;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.lut.LUTService;
import net.imglib2.display.ColorTable;



/**
 * 
 * @author Benoit Lombardot
 *
 **/



@Plugin(type = Service.class)
public class ShowCIPService extends AbstractService implements ImageJService {
	
	@Parameter
	private CIPService cipService;

	@Parameter
	private DisplayService displayService;
	
	@Parameter
	private UIService uiService;
	
	@Parameter
	private LUTService lutService;
	
	
	
	public void showLog( FunctionParameters2 params ) {
		
		List<String> messages = cipService.strings( params.get("message").value );
		
		for(String message : messages )
			IJ.log( message );
		
	}
	
	
	
	// still need to handle the case where the region belong to a particular timestep in the sequence
	public void showRegion( FunctionParameters2 params ) {
		
		ImagePlus imp;
		
		// grab the imagePlus corresponding to the imageHandle
		String imageHandle = (String) params.get("imageHandle").value;
		if ( imageHandle==null || imageHandle.toLowerCase().equals("current") )
			imp = WindowManager.getCurrentImage();
		else	
			imp = WindowManager.getImage( imageHandle );
		
		if( imp==null )
			return;
		
		int nCh = imp.getNChannels();
		
		// convert regions to list of rois
		Object regions =  params.get("region").value;
    	List<List<Roi>> roisPerRegion = Regions.toIJ1ROI( regions ); // should also handle the case of roi, list<roi>, and list<list<roi>>
    	
    	
    	// handle array of values
    	String mode; 
    	List<Double> values = cipService.scalars( params.get("scalars").value );
		if( values==null ) {
			mode="color";
			values = new ArrayList<Double>();
		}
		else {
			mode = "lut";
			// normalize the list
			double max = Collections.max(values);
			double min = Collections.min(values);
			
			for( int i=0; i< values.size(); i++)
				values.set( i , (values.get(i) - min)/(max-min) );
		}
		
    	// adjust the line coloring strategy
    	String colorString = (String) params.get("color").value;
    	Color color = stringToColor( colorString);
    	if( color==null || mode.equals("lut"))
    	{
    		ColorTable colorTable = lut( colorString );
    		if ( colorTable != null) {
    			mode = "lut";
    			double nReg = (double) roisPerRegion.size();
    			for(int i=0; i<nReg; i++)
    				values.add( (double)i / (nReg-1) );
    		}
    	}
    	
    	// line width
    	double width = cipService.scalar( params.get("width").value );
    	
    	// create or reset the
    	Overlay ov = imp.getOverlay();
    	if( (Boolean) params.get("reset").value || ov==null)
    		ov = new Overlay();
    	
    	// Set the region in an overlay with the proper color     		
    	int count=0;
    	if( nCh==1 ) {
	    	for( List<Roi> roiList : roisPerRegion ) {
	    		if (mode.equals("lut") ) {
	    			color = stringToColor(colorString, values.get(count) );
	    			count++;
	    		}
	    		for( Roi roi : roiList ) {
	    			if( roi != null) {
    					roi.setStrokeColor( color );
    					roi.setStrokeWidth(width);
    					ov.add(roi);
	    			}
	    		}
	    	}
	    }
    	else {
    		for( List<Roi> roiList : roisPerRegion ) {
	    		if (mode.equals("lut") ) {
	    			color = stringToColor(colorString, values.get(count) );
	    			count++;
	    		}
	    		for( Roi roi : roiList ) {
	    			if( roi != null) {
	    				for(int ch=1; ch<=nCh; ch++) {
	    					Roi roi2 = (Roi) roi.clone();
	    					roi2.setStrokeColor( color );
	    					roi2.setStrokeWidth(width);
	    					roi2.setPosition(ch, roi.getPosition(), 1);
	    					ov.add(roi2);
	    				}
	    			}
	    		}
	    	}
    	}
    	imp.setOverlay(ov);
    	
    	if ( imp.isVisible() )
	    	imp.updateAndRepaintWindow();
    	else
    		imp.show();
    	

	}
	
	
	public String showImage(FunctionParameters2 params ) {
		
		
		Object image = params.get("inputImage").value;
    	ImgPlus<?> imgPlus = cipService.toImgPlus( image ); 
    	
    	
    	int chIndex = imgPlus.dimensionIndex( Axes.CHANNEL );
    	int nCh = 1;
    	if ( chIndex >= 0 ) {
    		nCh = (int)imgPlus.dimension( chIndex );
    	}
    	imgPlus.initializeColorTables(nCh);
		
    	List<String> lutNames;
    	Object lutNamesObj = params.get("color").value;
    	if ( lutNamesObj instanceof String) {
    		lutNames = new ArrayList<String>();
    		lutNames.add( (String) lutNamesObj );
    	}
    	else {
    		lutNames = (List<String>) lutNamesObj;
    	}
    	
    	if ( lutNames.size() == 1  && lut(lutNames.get(0))==null )
		{
			// try to parse the string as first letter of basic colors
			lutNames = parseStringToBasicColorLuts( lutNames.get(0) );
		}
		
		if( lutNames != null )
	    {
			int nLut = lutNames.size();
    	    for( int ch=0; ch<nCh; ch++)
	    	{
    			int lutIdx = Math.min(ch, nLut-1);
	    		ColorTable cMap = lut( lutNames.get(lutIdx) );
	    		imgPlus.setColorTable(cMap, ch);
	    	}
    	}
	    
	    // create a unique name for the display
	    String name0 = imgPlus.getName();
	    if( name0 == null) {
	    	name0="image";
	    }
	    String name = name0; 
	    if ( ! displayService.isUniqueName(name) )
	    	name = name +" - 1";
	    int count = 1;
	    while( ! displayService.isUniqueName(name) )
	    {
	    	count++;
	    	name = name.replace(" - "+(count-1), " - "+count );
	    }
	    
	    // create and show the display
	    Display<?> display = displayService.createDisplay(name, imgPlus);
	    uiService.show( display );
		
	    
	    return name;
	}
	
	
	
	public String showMeasure(FunctionParameters2 params ) {
		
		
		// parse parameters
		@SuppressWarnings("unchecked")
		Map<String,List<Object>> measures = (Map<String,List<Object>>) params.get("measures").value;
		String resultTableName = (String) params.get("handle").value;
		boolean reset = (Boolean) params.get("reset").value;
		
		
		// get the results table
		// TODO: if resultTableName==null resultTableName="Results" 
		Window win = WindowManager.getWindow(resultTableName);
		ResultsTable resultTable = null;
		if ( (win!=null) && win instanceof TextWindow)
		{
			resultTable = ((TextWindow)win).getTextPanel().getResultsTable();
			if ( reset ) {
				resultTable.reset();
			}
		}
		else
		{
			resultTable = new ResultsTable();
		}
		
		
		// insert measures in the result table
		
		int rowOffset = resultTable.getCounter();
		//System.out.println("count: "+ rowOffset);
		boolean isFirstCol = true;
		
		for( Entry<String,List<Object>> e : measures.entrySet() )
		{
			String colName = e.getKey();
			List<?> colValues = e.getValue();

			if(  colValues.size()>0  )
			{
				if( colValues.get(0) instanceof String )
				{
					@SuppressWarnings("unchecked")
					List<String> colValues2 = (List<String>) colValues;
					addStringColumn( colValues2, colName, rowOffset, resultTable, isFirstCol);
				}
				else if ( colValues.get(0) instanceof Double )
				{
					@SuppressWarnings("unchecked")
					List<Double> colValues2 = (List<Double>) colValues;
					addDoubleColumn( colValues2, colName, rowOffset, resultTable, isFirstCol);	
				}
				else
				{
					List<String> colValues2 = new ArrayList<String>(colValues.size());
					for( Object value : colValues)
						colValues2.add( value.toString() );
					addStringColumn( colValues2, colName, rowOffset, resultTable, isFirstCol);
				}
				
			}
			isFirstCol = false;
			
		}
		
		resultTable.show( resultTableName );
		
		return resultTableName;
	}
		
	
	
	
	
	
	static Map<String,Integer> trackDisplayMode;
	static 
	{
		trackDisplayMode = new HashMap<String,Integer>();
		// in { 0:all, 1:local, 2:backward, 3:forward, 4:selection }
		trackDisplayMode.put("all",0);
		trackDisplayMode.put("local",1);
		trackDisplayMode.put("backward",2);
		trackDisplayMode.put("forwad",3);
		trackDisplayMode.put("selection",4);
		
	}
	
	
	
	
	public void showTrack( Model trackmateModel, FunctionParameters2 params ) {
		//Model trackmateModel, ImagePlus imp_display, String mode ){
		
		ImagePlus imp= null;
		if( params.get("image").value != null )
			imp = cipService.toImagegPlus( params.get("image").value );
		
		
		String mode = ((String)params.get("mode").value);
		if( mode == null )
			mode = "image"; // in "image","trackscheme","all"
		mode = mode.toLowerCase();
		
		String trackStyle = ((String)params.get("track style").value);
		if( trackStyle == null )
			trackStyle = "all";
		trackStyle = trackStyle.toLowerCase();
		
		int trackDepth = 0;
		if( params.get("track depth").value != null ) {
			trackDepth = (int)(double) cipService.scalar( params.get("track depth").value );
		}
		if( trackDepth <= 0 )
			trackDepth = 10;
		
		String displayColorMode = "TRACK_INDEX";
		
		
		
		SelectionModel selectionModel = new SelectionModel( trackmateModel );
		TrackScheme trackscheme = null;
		HyperStackDisplayer displayer = null;
		
		switch( mode ){
		case "image" :
			if( imp == null) 
				displayer =  new HyperStackDisplayer(trackmateModel, selectionModel);
			else
				displayer =  new HyperStackDisplayer(trackmateModel, selectionModel, imp );
			break;
			
		case "trackscheme" :
			trackscheme = new TrackScheme(trackmateModel, selectionModel);
			break;
			
		case "all":
			if( imp == null) 
				displayer =  new HyperStackDisplayer(trackmateModel, selectionModel);
			else
				displayer =  new HyperStackDisplayer(trackmateModel, selectionModel, imp );
			trackscheme = new TrackScheme(trackmateModel, selectionModel);
			break;
			
		default :
			break;
		}
		
		
		if( trackscheme != null )
		{
			trackscheme.render();
		}
		
		//PerTrackFeatureColorGenerator color = new PerTrackFeatureColorGenerator(trackmateModel, displayColorMode);
		if( displayer != null )
		{
			displayer.setDisplaySettings("TrackDisplaymode", trackDisplayMode.get(trackStyle)); // in { 0:all, 1:local, 2:backward, 3:forward, 4:selection }
			displayer.setDisplaySettings("TrackDisplayDepth", trackDepth);
			//displayer.setDisplaySettings("TrackColoring", color);
			if( imp != null )
				imp.show();
			displayer.render();
			displayer.refresh();
		}
		
		
		
	}
	

	
	
	
	private <T> void addStringColumn(List<String> colValues, String colName, int rowOffset, ResultsTable resultTable, boolean isFirstColumn)
	{
		if ( isFirstColumn )
		{
			for( String value : colValues)
			{
				resultTable.incrementCounter();
				resultTable.addValue( colName , value );
			}
		}
		else {
			int index = 0;
			for( String value : colValues)
			{
				resultTable.setValue( colName, rowOffset+index , value );
				index++;
			}
		}		
	}
	
	
	
	private <T> void addDoubleColumn(List<Double> colValues, String colName, int rowOffset, ResultsTable resultTable, boolean isFirstColumn)
	{
		if ( isFirstColumn )
		{
			for( Double value : colValues)
			{
				resultTable.incrementCounter();
				resultTable.addValue( colName , value );
			}
		}
		else {
			int index = 0;
			for( Double value : colValues)
			{
				resultTable.setValue( colName, rowOffset+index , value );
				index++;
			}
		}
	}
	
	
	
	Map<String,URL> lutsURL;
	Map<String,ColorTable> cachedLut;
	
	public ColorTable lut( String lutString ) 
	{	
		lutString = lutString.replace("_", " ");
		lutString = lutString.toLowerCase();
		
		if( lutsURL == null )
			initLutURL();
		
		if ( ! lutsURL.containsKey(lutString) )
			return null;
		ColorTable colorTable = null;
		
		if ( cachedLut.containsKey( lutString ) )
			return cachedLut.get(lutString);
		
		try {
			colorTable = lutService.loadLUT( lutsURL.get(lutString) );
			cachedLut.put(lutString, colorTable);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return colorTable;
	}
	
	
	
	private void initLutURL()
	{
		
		/*
		File lutDir = new File( IJ.getDirectory("luts") );
		File[] lutsFileRaw = lutDir.listFiles();
		Map<String, File> lutsFile = new HashMap<String,File>();
		for(File f : lutsFileRaw) {
			if( f.isFile() && ( f.getName().endsWith(".txt") || f.getName().endsWith(".lut") ) )
			{
				String key = f.getName();
				key = key.replaceAll(".txt$", "");
				key = key.replaceAll(".lut$", "");
				lutsFile.put(key.toLowerCase(), f );
			}
		}
		*/
		
		lutsURL = new HashMap<String,URL>();
		// find and format available luts
		Map<String,URL> lutURLRaw = lutService.findLUTs();
		for(Entry<String,URL> e : lutURLRaw.entrySet() ) {
			String key = e.getKey();
			String key2 = key.replaceAll( ".lut$", "");
			key2 = (new File(key2)).getName();
			key2 = key2.toLowerCase();
			key2 = key2.replace("_", " ");
			lutsURL.put(key2.toLowerCase(), e.getValue() );
		}
		
		
		// create short cut for the basic color
		// r g b c y m w
		lutsURL.put("r", lutsURL.get("red") );
		lutsURL.put("g", lutsURL.get("green") );
		lutsURL.put("b", lutsURL.get("blue") );
		lutsURL.put("c", lutsURL.get("cyan") );
		lutsURL.put("y", lutsURL.get("yellow") );
		lutsURL.put("m", lutsURL.get("magenta") );
		lutsURL.put("w", lutsURL.get("grays") );
		
		cachedLut = new HashMap<String,ColorTable>();
		
	}
	
	static Map<String,Color> stringToJavaColor;
	static Map<String,String> letterToColor;
	static {
		letterToColor = new HashMap<String,String>();
		letterToColor.put("r", "red");
		letterToColor.put("g", "green");
		letterToColor.put("b", "blue");
		letterToColor.put("c", "cyan");
		letterToColor.put("y", "yellow");
		letterToColor.put("m", "magenta");
		letterToColor.put("w", "grays");
		
		
		stringToJavaColor = new HashMap<String,Color>();
		stringToJavaColor.put("red", Color.red);
		stringToJavaColor.put("green", Color.green);
		stringToJavaColor.put("blue", Color.blue);
		stringToJavaColor.put("cyan", Color.cyan);
		stringToJavaColor.put("yellow", Color.yellow);
		stringToJavaColor.put("magenta", Color.magenta);
		stringToJavaColor.put("white", Color.white);
		stringToJavaColor.put("black", Color.black);
		stringToJavaColor.put("orange", Color.orange);
		stringToJavaColor.put("pink", Color.pink);
		stringToJavaColor.put("brown", new Color(128, 64, 0));
		stringToJavaColor.put("purple", new Color(127, 0, 127) );
		stringToJavaColor.put("lila", new Color(200,100,255) );
		
	}
	
	public List<String> parseStringToBasicColorLuts( String str )
	{
		List<String> lutNames = new ArrayList<String>();
		for(int i=0 ; i<str.length() ; i++ )
		{
			String letter = str.substring(i, i+1).toLowerCase();
			if ( letterToColor.containsKey( letter ) )
				lutNames.add( letterToColor.get(letter) );
			else
				return null;
		}
		return lutNames;
	}
	
	public Color stringToColor(String colorString ) {
		
		if( stringToJavaColor.containsKey( colorString) )
			return stringToJavaColor.get( colorString );
		
		return null;
	}
	
	
	public Color stringToColor(String lutString , double index ) {
		
		index = Math.max(0, index);
		index = Math.min(1, index);
		
		ColorTable colors = lut( lutString );
		int nColors = colors.getLength();
		int r = colors.get(0, (int)( index*(nColors-1) ) );
		int g = colors.get(1, (int)( index*(nColors-1) ) );
		int b = colors.get(2, (int)( index*(nColors-1) ) );
		
		return new Color(r,g,b);
	}
	
	
}
