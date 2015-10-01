package server;

/* 
 * "Description of what class does goes here"
 * @Author Bruce Cheek / Joe Weber
 * @date November 17, 2011
 * @course Software Engineering CSIS 457-01
 * @related Cell 
 * @included Cell
 */
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import au.com.bytecode.opencsv.*;

public class Grid {
	int numLayers; /** amount of layers */
	int row, col; /** dimensions of the grid */

	private Cell grid[][];
	
	public static final int LAYER = 0;
	public static final int OIL = 1;
	public static final int GAS = 2;
	public static final int ROCK = 3;

	/** This constructor initializes and instantiates all of the cell objects 
	 * 	from the files
	 * @param rowNum is the amount of rows in the grid. Taken from the config file
	 * @param colNum is the amount of columns in the grid. Taken from the config file
	 * @param layerCount is the amount of layers in the grid, which controls how many
	 * 		  times the loop iterates.
	 */
	public Grid(int rowNum, int colNum, int layerCount) {
		this.row = rowNum;
		this.col = colNum;

		this.numLayers = layerCount;

		/** instantiate Cells with the total amount of layers that are present */
		grid = new Cell[rowNum][colNum];
		
		for (int x = 0; x < row; x++) {
			for (int y = 0; y < col; y++) {
				grid[x][y] = new Cell(layerCount);
				for(int z = 0; z < layerCount; z++) {
					grid[x][y].oil[z] = 0;
					grid[x][y].gas[z] = 0;
					grid[x][y].rocktype[z] = "";
				}

			}
		}
	}
	public void importCSV(String fileName, int layer, int type) {
		/**
		 * This for loop imports all of teh dataz from each layer into the
		 * corresponding cell
		 */
		String fullName = "";
		try {
			String path = "trunk/datafiles/";
			String ext = ".csv";
			
			CSVReader reader = null;

			fullName = path + fileName;
			reader = new CSVReader(new FileReader(fullName));

			parseCSV(reader, type, layer);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("The selected file " + fullName
					+ " was not found.");
		}
	}
	public void addRock(String rocktype, int layer) {
		for(int x = 0; x < row; x++) {
			for(int y = 0; y < col; y++) {
				grid[x][y].rocktype[layer] = rocktype;
			}
		}
	}

	/**
	 * @param reader is the current file that the informations are being read from
	 * @param type is the type of layer that is being imported
	 * @param c is the current layer number that we are on
	 */
	private void parseCSV(CSVReader reader, int type, int layer) {
		String[] nextLine;
		int i, j;
		String value;
		i = j = 0;
		try {
			while ((nextLine = reader.readNext()) != null) {
				for (j = 0; j < nextLine.length; j++) {
					if (nextLine[j].equals("")) {
						value = "0";
					} else {
						value = nextLine[j];
					}						
					switch(type) {
						case LAYER:	grid[i][j].layers[layer] = Integer.parseInt(value);
							break;
						case OIL: grid[i][j].oil[layer] = Integer.parseInt(value);
							break;
						case GAS: grid[i][j].gas[layer] = Integer.parseInt(value);
							break;
						case ROCK: grid[i][j].rocktype[layer] = value;
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
	 * This method is what is actually putting the data into the cells
	 * 
	 * @param x is the cell's row location
	 * @param y is the cell's col location
	 * @param layers is the total amount of layers.
	 * @param gas contains the rate at which the gas gives teh moniez. Is 0 if
	 *            no gas is contained in the cell.
	 * @param oil contains the rate at which the oil gives teh moniez. Is 0 if
	 *            no oil is contained in the cell.
	 */
	public void addCell(int x, int y, int[] layers, int gas, int oil) {
		//grid[x][y] = new Cell(layers, gas, oil);
	}

	/**
	 * This method gets the rate of the gas
	 * 
	 * @param x is the cell's row location
	 * @param y is the cell's col location
	 * @return The rate of the gassy flow at the x, y location.
	 */
	public int getGas(int x, int y, int depth) {
		return grid[x][y].gas[depth];
	}

	/**
	 * This method gets the rate of the oil.
	 * 
	 * @param x is the cell's row location
	 * @param y is the cell's col location
	 * @return The rate of the oil flow at the x, y location.
	 */
	public int getOil(int x, int y, int depth) {
		return grid[x][y].oil[depth];
	}

	/**
	 * This method gets a particular layer's information.
	 * 
	 * @param x is the cell's row location
	 * @param y  is the cell's col location
	 * @param depth is the layer's depth (ft. above sea level?)* 
	 * @return The x, y location as well as the depth of the layer. 
	 */
	int getLayer(int x, int y, int depth) {
        return grid[x][y].layers[depth]; 
    } 
	
	/** 
	 * This method gets a seismic line.
	 * 
	 * @param x is the cell's row location
	 * @param y is the cell's col location
	 * @return the integer array of informations for teh seismology 
	 */
	public int[] getSeismicLine(int x, int y) {
		// for(int i = 0; i<numLayers; i++)
		return grid[x][y].layers;
	}
}

/**
 * future versions of the game will have gas/oil stored in layer instead of
 * cell, but for now it’s not since drilling goes through all layers Does not
 * need an in depth constructor because all of the cells are being initialized
 * in Grid when the dataz are being imported
 */
class Cell {
	public int[] layers; /** holds depths */
	public int[] gas; /** gas and oil hold the rates if any */
	public int[] oil;
	public int xCoord, yCoord;
	public String[] rocktype;
	public boolean isDrilled;
	public boolean isOwned;

	Cell(int layerCount) {
		layers = new int[layerCount];
		gas = new int[layerCount];
		oil = new int[layerCount];
		rocktype = new String[layerCount];
	}
	
	Cell(int x, int y) {
		xCoord = x;
		yCoord = y;
	}
}