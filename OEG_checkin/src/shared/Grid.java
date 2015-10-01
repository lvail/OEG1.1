package shared;


import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;
import java.awt.*;

import org.xml.sax.SAXException;

import shared.action.Bid;
import au.com.bytecode.opencsv.*;

/**
 * Holds data about land information used in OEG.
 * Holds a two-dimensional array of Cells.
 * Acts as a detailed interface to Cell.
 * @Author Bruce Cheek / Joe Weber
 * @date November 17, 2011
 * @course Software Engineering CSIS 457-01
 * @related Cell 
 * @included Cell
 */
public class Grid {
	/** amount of layers */
	int numLayers; 
	/** dimensions of the grid */
	Point maxP; 

	private Cell grid[][];

	public static final int LAYER = 0;
	public static final int OIL = 1;
	public static final int GAS = 2;
	public static final int ROCK = 3;
	private static int fudgeValue = 10;
	private static int gridSize;
	private static double removePercentage = 70;
	
	/** 
	 * This constructor initializes and instantiates all of the cell objects 
	 * from the various config files.
	 * @param maxXNum is the amount of rows in the grid taken from a config file.
	 * @param colNum is the amount of columns in the grid taken from a config file.
	 * @param layerCount is the amount of layers in the grid, which controls how many
	 * 		  times the for loop iterates in initialize.
	 */
	public Grid(Point max, int layerCount) {
		setLimits(max,layerCount);
	}
	/**
	 * For creating a grid without initializing. USED in ParseSimulationXML.java.
	 */
	public Grid() {

	}

	public int getGridSize() {
		return gridSize;
	}
	public void setLimits(Point max, int layerCount) {
		this.maxP = max;

		this.numLayers = layerCount;

		/** instantiate Cells with the total amount of rows and cols that are present */
		grid = new Cell[maxP.y][maxP.x];
	}

	/**
	 * Initialize creates all of the cells at every single layer which makes up our
	 * entire grid.
	 * @param rowNum is the amount of rows in the grid taken from the config file.
	 * @param colNum is the amount of columns in the grid taken from the config file.
	 * @param layerCount is the amount of layers in the grid, which controls how many
	 * 	      times the for loop iterates.
	 */
	public void initializeAll() {
		/** instantiate Cells with the total amount of layers that are present */
		for (int x = 0; x < maxP.x; x++) {
			for (int y = 0; y < maxP.y; y++) {
				initializeCell(x,y);
			}
		}
	}

	private void initializeCell(int x, int y) {
		grid[y][x] = new Cell(numLayers,x,y);
		for(int z = 0; z < numLayers; z++) {
			grid[y][x].oil[z] = 0;
			grid[y][x].gas[z] = 0;
			grid[y][x].rocktype[z] = "";
			grid[y][x].layerExists[z] = true;
		}
		grid[y][x].isOwned = false;
		grid[y][x].isDrilled = false;
		grid[y][x].seismicExists = false;
		grid[y][x].owner = Bid.noOwner;
	}
	/**
	 * Imports a CSV file that has a particular type on a certain
	 * layer "level".  
	 * @param file name of the file we are importing. 
	 * @param level is the integer representing the layer we are on. 
	 * @param type is either OIL, GAS, 
	 * @throws SAXException 
	 */
	public void importCSV(File file, int level, int type) throws SAXException {
		/**
		 * This for loop imports all of teh dataz from each layer into the
		 * corresponding cell
		 */
		try {
			CSVReader reader = null;

			reader = new CSVReader(new FileReader(file));

			parseCSV(reader, type, level);

		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.out.println("The selected file " + file
					+ " was not found.");
			throw new SAXException("The selected CSV file " + file
					+ " was not found.");
		}
	}
	/**
	 * @param reader is the current file that the information is being read from
	 * @param type is the type of layer that is being imported
	 * @param level is the current level number that we are on
	 */
	private void parseCSV(CSVReader reader, int type, int level) {
		String[] nextLine;
		String value;
		int i = 0, j;

		try {
			while ((nextLine = reader.readNext()) != null) {
				for (j = 0; j < nextLine.length; j++) {
					value = nextLine[j];					
					switch(type) {
					case LAYER:
						if (value.equals("")) {
							grid[i][j].layers[level] = null;
							grid[i][j].layerExists[level] = false;
						} else {
							grid[i][j].layers[level] =
									Integer.parseInt(nextLine[j]);
						/*			if (grid[i][j].layers[0]<lowest){
										lowest=grid[i][j].layers[level];
									}
									if (grid[i][j].layers[0]>highest){
										highest=grid[i][j].layers[level];
									}*/
						}			
						break;
					case OIL: grid[i][j].oil[level] = Integer.parseInt(value);
					break;
					case GAS: grid[i][j].gas[level] = Integer.parseInt(value);
					break;
					case ROCK: grid[i][j].rocktype[level] = value;
					break;
					default: System.out.println("WE HAZ TEH ERROZ?");
					}
				}
				i++;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	/**
	 * @param size is the Grid size in feet. E.G. 1320(5280/4) represents 
	 * a 4x4 discritinization of a square mile
	 */
	public static void setGridSize(int size){
		gridSize=size;
	}
	/**
	 * This method sets the owner of a particular cell 
	 * on the grid.
	 * 
	 * @param owner is an Operator that holds the name
	 *              of the team that won this cell in Auction. 
	 * @param x is the cell's row location
	 * @param y is the cell's col location
	 */
	public void setOwner(String owner, Point loc){
		if(!exists(loc))
			initializeCell(loc.x, loc.y);
		grid[loc.y][loc.x].owner = owner; 
	}
	public void setLayers(Point loc, Integer[] layers) {
		if(!exists(loc))
			initializeCell(loc.x, loc.y);
		grid[loc.y][loc.x].layers = layers;
	}

	/** Set the layer array from a socket string.
	 * Assumes that this is a full seismic info, and sets seismicExists to true
	 * @param in The socket string containing seismic info.
	 */
	public void setLayersSocket(String in) {
		String[] split = in.split("#");
		Point p = new Point(Integer.parseInt(split[0]),
				Integer.parseInt(split[1]));
		setSeismicExists(p, true);
		setLayers(p, getLayerArrayFromString(split[2]));

	}

	public void setOil(Point loc, Integer[] oil) {
		if(!exists(loc))
			initializeCell(loc.x, loc.y);
		grid[loc.y][loc.x].oil = oil; 
	}
	public void setOilSocket(String in) {
		String[] split = in.split("#");
		setOil(new Point(Integer.parseInt(split[0]),
				Integer.parseInt(split[1])),
				getArrayFromString(split[2]));
	}



	public void setGas(Point p, int gas, int depth) {
		grid[p.y][p.x].gas[depth] = gas;
	}
	public void setOil(Point p, int oil, int depth) {
		grid[p.y][p.x].oil[depth] = oil;
	}
	public void setLayer(Point p, Integer layer, int depth) {
		grid[p.y][p.x].layers[depth] = layer;
	}
	public void setRock(Point p, String rock, int depth) {
		grid[p.y][p.x].rocktype[depth] = rock;
	}
	public void setRock(Point loc, String[] rock) {
		grid[loc.y][loc.x].rocktype = rock; 
	}
	public void setGas(Point loc, Integer[] gas) {
		if(!exists(loc))
			initializeCell(loc.x, loc.y);
		grid[loc.y][loc.x].gas= gas; 
	}
	public void setGasSocket(String in) {
		String[] split = in.split("#");
		setGas(new Point(Integer.parseInt(split[0]),
				Integer.parseInt(split[1])),
				getArrayFromString(split[2]));
	}

	public void setRockSocket(String in) {
		String[] split = in.split("#");
		setRock(new Point(Integer.parseInt(split[0]),
				Integer.parseInt(split[1])),
				getRockArrayFromString(split[2]));
	}

	public void setDrilled(Point p, boolean in) {
		if(!exists(p))
			initializeCell(p.x, p.y);
		grid[p.y][p.x].isDrilled = in;
		if(in)
			grid[p.y][p.x].seismicExists = in;
	}
	public void setDrilledSocket(String in) {
		String[] split = in.split("#");
		setDrilled(new Point(Integer.parseInt(split[0]),
				Integer.parseInt(split[1])),true);
	}

	/** Converts a String representation of an integer array
	 * back into an array : "[1,2,3,4,5]" = [1,2,3,4,5]
	 * @param array The string representing an int array
	 * @return The int array
	 */
	private Integer[] getArrayFromString(String array) {
		String[] layerS = array.replace("[","").replace("]","").split(", ");
		Integer[] layer = new Integer[layerS.length];
		for(int i = 0; i < layerS.length; i++) {
			layer[i] = Integer.parseInt(layerS[i]);
		}
		return layer;
	}

	private Integer[] getLayerArrayFromString(String array) {
		String[] layerS = array.replace("[","").replace("]","").split(", ");
		Integer[] layer = new Integer[layerS.length];
		for(int i = 0; i < layerS.length; i++) {
			if(!layerS[i].equals("null"))
				layer[i] = Integer.parseInt(layerS[i]);
			else {
				layer[i] = null;
			}
		}
		return layer;
	}

	private String[] getRockArrayFromString(String array) {
		String[] rockS = array.replace("[","").replace("]","").split(", ");
		String[] rock = new String[rockS.length];
		for(int i = 0; i < rockS.length; i++) {
			if(!rockS[i].equals("null"))
				rock[i] = rockS[i];
			else {
				rock[i] = null;
			}
		}
		return rock;
	}
	private boolean[] getExistsArrayFromString(String string) {
		String[] layerS = string.replace("[","").replace("]","").split(", ");
		boolean[] layer = new boolean[layerS.length];
		for(int i = 0; i < layerS.length; i++) {
			layer[i] = Boolean.parseBoolean(layerS[i]);
		}
		return layer;
	}

	/**
	 * @param rocktype is the type of rock.
	 * @param layer is the layer at which that rock type is found.
	 */
	public void addRock(String rocktype, int layer) {
		for(int x = 0; x < maxP.x; x++) {
			for(int y = 0; y < maxP.y; y++) {
				grid[y][x].rocktype[layer] = rocktype;
			}
		}
	}

	/**
	 * This method gets the rate of the gas
	 * 
	 * @param p The location of the cell
	 * @return The rate of the gassy flow at the x, y location.
	 */
	public int getGas(Point p, int depth) {
		return grid[p.y][p.x].gas[depth];
	}

	public Integer[] getGasArray(Point p) {
		return grid[p.y][p.x].gas;
	}

	public String getGasSocket(Point p) {
		return p.x + "#" + p.y + "#" + Arrays.toString(getGasArray(p));
	}
	/**
	 * This method gets the rate of the oil.
	 * 
	 * @param p The locatoin of the cell
	 * @return The rate of the oil flow at the x, y location.
	 */
	public int getOil(Point p, int depth) {
		return grid[p.y][p.x].oil[depth];
	}

	public Integer[] getOilArray(Point p) {
		return grid[p.y][p.x].oil;
	}

	public String getOilSocket(Point p) {
		return p.x + "#" + p.y + "#" + Arrays.toString(getOilArray(p));
	}
	/**
	 * This method gets a particular layer's information.
	 * 
	 * @param x is the cell's row location
	 * @param y  is the cell's col location
	 * @param depth is the layer's depth (ft. above sea level?)* 
	 * @return The x, y location as well as the depth of the layer. 
	 */
	public Integer getLayer(Point p, int depth) {
		return grid[p.y][p.x].layers[depth]; 
	} 

	/** 
	 * This method gets all of the seismic line information for a cell.
	 * 
	 * @param x is the cell's row location
	 * @param y is the cell's col location
	 * @return the integer array of informations for teh seismology 
	 */
	public Integer[] getLayerArray(Point p){
		return Arrays.copyOf(grid[p.y][p.x].layers, grid[p.y][p.x].layers.length);
	}

	public String getLayersSocket(Point p) {
		return p.x + "#" + p.y + "#" + Arrays.toString(getLayerArray(p));
	}

	public String getRockSocket(Point p) {
		return p.x + "#" + p.y + "#" + Arrays.toString(getRockArray(p));
	}

	public String[] getRockArray(Point p) {
		return grid[p.y][p.x].rocktype;
	}


	public String getRock(Point p, int i) {
		return grid[p.y][p.x].rocktype[i];
	}

	public boolean exists(Point location) {
		if(location.y < 0 || location.x < 0)
			return false;
		return grid[location.y][location.x] != null;
	}
	public boolean isOwned(Point location) {
		return (!grid[location.y][location.x].owner.equals(Bid.noOwner));
	}
	public boolean isDrilled(Point location) {
		return grid[location.y][location.x].isDrilled;
	}
	public boolean seismicExists(Point p) {
		return grid[p.y][p.x].seismicExists;
	}
	public void setSeismicExists(Point p, boolean exists) {
		grid[p.y][p.x].seismicExists = exists;
	}
	public String getOwner(Point p) {
		if(exists(p))
			return grid[p.y][p.x].owner;
		return Bid.noOwner;
	}

	public Point getDimensions() {
		return maxP;
	}

	public int getNumLayers() {
		return numLayers;
	}

	public String[] getGridSocket() {
		String[] toReturn = new String[maxP.y*maxP.x];
		Point p;
		int k = 0;
		for(int i = 0; i < maxP.y; i++){
			for(int j = 0; j < maxP.x; j++) {
				p = new Point(j,i);
				if(exists(p))
					toReturn[k++] = cellToSocket(p);
			}
		}
		return toReturn;
	}

	public String cellToSocket(Point p) {
		String s = "";
		s += "x#" + p.x;
		s += "#y#" + p.y;
		s += "#gas#" + Arrays.toString(getGasArray(p));
		s += "#oil#" + Arrays.toString(getOilArray(p));
		s += "#layer#" + Arrays.toString(getLayerArray(p));
		s += "#rock#" + Arrays.toString(getRockArray(p));
		s += "#exists#" + Arrays.toString(getLayerExists(p));
		s += "#drilled#" + isDrilled(p);
		s += "#seismic#" + seismicExists(p);
		if(isOwned(p))
			s += "#owner#" + getOwner(p);

		return s;
	}

	public void cellFromSocket(String s) {
		String[] split = s.split("#");
		int x = Integer.parseInt(split[1]);
		int y = Integer.parseInt(split[3]);
		Point p = new Point(x,y);
		grid[y][x] = new Cell(x,y);
		setGas(p, getArrayFromString(split[5]));
		setOil(p, getArrayFromString(split[7]));
		setLayers(p, getLayerArrayFromString(split[9]));
		setRock(p, getRockArrayFromString(split[11]));
		setLayerExists(p, getExistsArrayFromString(split[13]));
		setDrilled(p, Boolean.parseBoolean(split[15]));
		setSeismicExists(p, Boolean.parseBoolean(split[17]));
		if(split.length > 18){
			setOwner(split[19],p);
		}
	}

	public static Integer[] fudge(Integer layer[]){
		Random rand = new Random();
		int len = layer.length-1;
		int i = 0, curDepth = 0;

		//skip to the location of the top layer (pass over nulls)
		while(layer[i] == null && i < len)
			i++;

		int top = curDepth = i;
		double depth;

		//do not fudge the top layer
		i++;

		//for the remaining layers
		for(; i<len; i++){
			BufferedWriter bw = null;
			try {
				//file for logging the fudge values
				bw = new BufferedWriter(new FileWriter("fudge.txt", true));
				
				//TODO Verify calculations are correct.
				//100/XML%
				//((100/Grid.removeValue*(len/i)) == 0)
				//randomly remove a layer from results
				if((rand.nextDouble()*100) < Grid.removePercentage ) {
					if(rand.nextInt((len/i)) == 0){
						//done by not incrementing curDepth
						bw.write("layer " + layer[i] + " removed.\n");
					}
				}
				//skew the depth of the layer in the results
				//if the layer exists
				else if (layer[i] != null){
					//curDepth to skip layers that are taken out
					curDepth++;

					//randomly choose to add or subtrace
					int a = rand.nextInt(1);
					
					//the depth of the current layer
					depth = layer[top] - layer[i];
					
					int before = layer[i];
					double random = rand.nextDouble();

					/*Uniformly distributed random decimal from 0 to 1,
							times the set XML percentage,
							times the elevation adjusted for depth */
					// (0.0 - 1.0) * (0.1) * (top layer + depth)
					double fudge = ((random * ((double)Grid.fudgeValue/100.0)) * depth);

					if(a==0)
						layer[curDepth] = layer[i] + (int)fudge;
					else
						layer[curDepth] = layer[i] - (int)fudge;

					int amount = Math.abs(before-layer[curDepth]);
					String output = String.format("fudgeValue: %d, rand: %.4f, depth: %2f, "+
							"before: %d, after: %d, amount: %3d, percent: %5.3f\n"
							, Grid.fudgeValue,random,depth,before,layer[i],
							amount,(double)((double)amount)/(double)before);
					bw.write(output);
				}
				bw.close();	
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Arrays.copyOfRange(layer,0,curDepth+1);
	}
	public int getGasRate(Point p) {
		Integer[] gas = getGasArray(p);
		int sum = 0;
		for (int x : gas) {
			sum += x;
		}
		return sum;
	}
	public int getOilRate(Point p) {
		Integer[] oil = getOilArray(p);
		int sum = 0;
		for (int x : oil) {
			sum += x;
		}
		return sum;
	}

	/** This removes zero data from arrays where a layer does not exist.
	 * Takes in an array of info and removes elements from it based on the
	 * null values that exist in the layer array for that cell. If an element
	 * is null in the layer array, it is from the passed array.
	 * @param p
	 * @param array
	 * @return
	 */
	public String removeEmptyFromArray(Point p, Object[] array) {
		int curDepth = 0;
		for(int i = 0; i < numLayers; i++) {
			if(grid[p.y][p.x].layerExists[i] && i < array.length) {
				array[curDepth++] = array[i];
			}
		}
		Object [] toReturn = Arrays.copyOfRange(array,0,curDepth);
		return Arrays.toString(toReturn).replace("[","").replace("]","");
	}
	public boolean[] getLayerExists(Point p) {
		return grid[p.y][p.x].layerExists;
	}
	public void setLayerExists(Point p, boolean[] none) {
		grid[p.y][p.x].layerExists = none;		
	}
	public boolean layerExists(Point p, int depth) {
		return grid[p.y][p.x] != null && grid[p.y][p.x].layerExists != null && grid[p.y][p.x].layerExists[depth];
	}
	public static void setFudgePercent(int fudge) {
		fudgeValue = fudge;		
	}
	public void setRemovePrecent(int parseInt) {
		removePercentage = parseInt;
	}
	public int getLowest(){
		
		return findLowest();
	}
	private int findLowest(){
		int lowest=9999999;
		int x;
		int y;
		int i;
		for(x=0;x< maxP.x; x++){
			for(y=0;y<maxP.y;y++){
				if (grid[x][y] != null && grid[x][y].layers != null){
					for(i=0; i<=20; i++){
						if(grid[x][y].layers[i] != null){
							break;
						}
					}
					if(grid[x][y].layers[i]<lowest){
							lowest = grid[x][y].layers[i];
					}
					
				}
			}
		}
		return lowest;
	}
	public int getHighest(){
		
		return findHighest();
	}
	private int findHighest(){
		int highest=-9999;
		int x;
		int y;
		int i;
		for(x=0; x< maxP.x; x++){
			for(y=0; y<maxP.y; y++){
				if (grid[x][y] != null && grid[x][y].layers != null){
					for(i=0; i<=20; i++){
						if(grid[x][y].layers[i] != null){
							break;
						}
					}
					if(grid[x][y].layers[i]>highest){
						highest=grid[x][y].layers[i];
					}
				}	
			}
		}
		return highest;
	}
	public int getElevation(Point p){
		int j=0;
		for (int i=0; i<=20; i++){
			if(layerExists(p,i)){
				j=i;
				break;
			}
		}
		return grid[p.y][p.x].layers[j];
	}
	
}




/** Cell holds all information that can be known about a particular section
 * in the Grid. No interface is given to Cell because Grid is the only
 * class to use this class directly. Grid contains the interface to cell.
 */
class Cell {

	/** Name of the owner of this cell, if any */
	public String owner;
	/** The altitude information for the different layers at this cell.
	 * Each element in the array represents the top altitude of a layer. */
	public Integer[] layers;
	/** The rate of gas flow at this cell, for each layer. */
	public Integer[] gas;
	/** The rate of oil flow at this cell, for each layer. */
	public Integer[] oil;
	/** The x coordinate of this cell (horizontal-right). */
	public int xCoord;
	/** The y coordinate of this cell (vertical-down). */
	public int yCoord;
	/** The type of rock, as a string at this cell, for each layer. */
	public String[] rocktype;
	/** Boolean value true if this cell has been drilled */
	public boolean isDrilled;
	/** Boolean value true if this cell is currently owned */
	public boolean isOwned;
	/** Boolean value true if seismic info exists for this cell */
	public boolean seismicExists;
	/** Boolean array indicating if the layer exists at this location */
	public boolean[] layerExists;

	/** Constructor to initialize all the arrays in Cell 
	 * @param layerCount The number of layers, or depth, of this cell.
	 * @param x The x coordinate of the cell.
	 * @param y The y coordinate of the cell. */
	Cell(int layerCount, int x, int y) {
		layers = new Integer[layerCount];
		gas = new Integer[layerCount];
		oil = new Integer[layerCount];
		layerExists = new boolean[layerCount];
		rocktype = new String[layerCount];
		this.xCoord=x;
		this.yCoord=y;
	}
	/** Constructor to simply create a cell at this location.
	 * @param x The x coordinate of the cell.
	 * @param y The y coordinate of the cell.
	 */
	Cell(int x, int y) {
		xCoord = x;
		yCoord = y;
	}
}