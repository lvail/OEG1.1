package client;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.lang.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

import shared.Grid;
import shared.action.Bid;
import shared.action.SeismicRequest;

/** testMap
 *
 * @author Joe Schindel
 * @id      	jschinde
 * @OEG
 */

class Map extends JPanel implements ActionListener, MouseListener, MouseMotionListener
{
	private static final long serialVersionUID = 1L;
	private int s, h, w; //size height and width variables
	private int[] xMinCords; //this an array of the x minimum values for each square on the map
	private int[] yMinCords; //this an array of the y minimum values for each square on the map
	private int squareSize; //this value is used by several methods, mainly draw
	private Requests request;
	public Color curBGColor = new Color(240,240,240);
	/** sMode is the toggle for the seismic line */
	public int sMode=0;
	int lastX = 0;
	int lastY = 0;
	/** used by clickColor to change the previous square back to default */
	private int oldx=0, oldy=0;
	public int currentLayer=0;
	


	/**Constructor that creates a grid the sizes set in the constructor
	 * @param request The GUI object
	 */
	public Map(Requests request){
		//not being used
		s=15;
		h=790;
		w=790;
		this.request = request;
		initilize();
	}

	/**Constructor that creates a grid based on the size specified.
	 * Currently, the size must be a square.
	 * @param request The GUI object
	 * @param size the amount of squares
	 * @param p The size size of the grid, represented as a Point.
	 */
	public Map(Requests request, int size, Point p)
	{
		this.request = request;
		s=p.x;
		h=800;
		w=800;
		initilize();
	}

	/**initilize the GUI!
	 *
	 */
	public void initilize(){
		//This sets h and w to be a square based on the smaller value
		if (h != w){
			if (h > w)
				h = w;
			else if (h < w)
				w = h;
		}

		//this sets square size 30 is the buffer times 2 (15*2)
		squareSize = h/s;//(h - 30)/s;

		//set minimum square size
		if(squareSize < 15) {
			squareSize = 15;
			h = w = squareSize * s;
		}

		setSize(w+60,h+60);
		setLayout(null);
		setPreferredSize(new Dimension(w+30, h+30));

		//set the arrays for the clickable part
		xMinCords = new int[s+1];
		yMinCords = new int[s+1];

		//Canvas is important to clickable
		setOpaque(true);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/* (non-Javadoc) paints on the canvas, happens automatically
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int x = 15;
		int y = 15; //buffers to add space on the right and top sides

		g.setColor(Color.black); //black lines for the grid

		//draw horizontal lines
		for(int i = 0; i < s+1; i++){
			g.drawLine(x, y, x, (15+(s*squareSize)));
			xMinCords[i] = x;
			x+=squareSize;
		}
		x = 15;//reset the values before drawing 
		y = 15;
		//draw vertical lines
		for(int i = 0; i < s+1; i++){
			g.drawLine(x, y, (15+(s*squareSize)), y);
			yMinCords[i] = y;
			y+=squareSize;
		}
		//paint all the colors /per mode
		paintColorsMode(g, lastX,lastY);
	}

	/** getCol returns the column a click is in
	 * @param x the x cordinate of the click
	 * @return the row the click was in
	 */
	public int getCol(int x)
	{
		int col = 0;
		for (int i = 0; i <=s; i++){
			if (x < xMinCords[i]){
				col = i-1;
				break;
			}
		}//end for
		return col;
	}

	/** getRow returns the column a click is in
	 * @param y the y cordinate of the click
	 * @return the row the click was in
	 */
	public int getRow(int y)
	{
		int row=0;
		for (int i = 0; i <=s; i++){
			if (y < yMinCords[i]){
				row = i-1;
				break;
			}
		}//end for
		return row;
	}


	/**  This Colors in a square specified
	 * @param g The Graphics object to use for painting.
	 * @param color - color to paint
	 * @param x - row
	 * @param y - column
	 */
	public void fillSquare(Graphics g, Color color, int x, int y)
	{
		if(g == null)
			g = this.getGraphics();
		//            	super.paintComponent(g);
		g.setColor(color);
		g.fillRect(xMinCords[x]+1, yMinCords[y]+1, squareSize-1, squareSize-1);
	}

	/** This colors the square specified as selected
	 * @param x
	 * @param y
	 */
	public void clickColor(Graphics g, Color c,int x, int y)
	{
		fillSquare(g, c, x, y);
		if(x != oldx && y != oldy)
			repaint();
		//			fillSquare(g, curBGColor, oldx, oldy);
		oldx=x;
		oldy=y;
	}
	
	/** This colors in squares that are vaild for seismic lines
	 * @param g The Graphics object to use for painting.
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void seismicColor(Graphics g,int x1, int y1, int x2, int y2)
	{
		colorSeismicKnowledge(g,currentLayer);

		if(x1==x2 && y1==y2){
			for(int i=0; i < x1; i++){//horizontal left ghost
				fillSquare(g,Color.pink,i,y1);
			}
			for(int i=(x1+1); i < s; i++){//horizontal right ghost
				fillSquare(g,Color.pink,i,y1);
			}
			for(int i=0; i < y1; i++){//vertical up ghost
				fillSquare(g,Color.pink,x1,i);
			}
			for(int i=(y1+1); i < s; i++){//vertical down ghost
				fillSquare(g,Color.pink,x1,i);
			}
			int j = 0;
			for (int i=(x1-1); i>=0; i--){//diagonal up/left ghost
				j= x1 - i;
				j= y1 - j;
				if(i >= 0 && j >= 0){
					fillSquare(g,Color.pink,i,j);
				}
				else break;
			}
			for(int i=(x1+1); i<=s; i++){//diagonal up/right ghost
				j = i - x1;
				j = y1 - j;
				if(i < s && j >= 0){
					fillSquare(g,Color.pink, i, j);
				}
				else break;
			}
			for (int i=(x1+1); i<=s; i++){//diagonal down/right ghost
				j = i - x1;
				j = y1 + j;
				if(i < s && j < s){
					fillSquare(g,Color.pink,i,j);
				}
				else break;
			}
			for(int i=(x1-1); i>=0; i--){//diagonal down/left ghost
				j = x1 - i;
				j = y1 + j;
				if(i >= 0 && j < s){
					fillSquare(g,Color.pink, i, j);
				}
				else break;
			}
			
			fillSquare(g,Color.red,x1,y1);
		//Colors Vertical lines
		}else if (x1==x2){
			if (y1>y2){
				for (;y2<=y1;y2++){
					fillSquare(g, Color.red, x2,y2);}}
			else
				for (;y1<=y2;y1++){
					fillSquare(g,Color.red, x1,y1);}
		}
		//colors Horizontal Lines
		else if(y1==y2){
			if (x1>x2){
				for (;x2<=x1;x2++){//fills in color in cells between x2 and x1
					fillSquare(g,Color.red, x2,y2);}}
			else
				for (;x1<=x2;x1++){//fills in color in cells between x1 and x2
					fillSquare(g,Color.red, x1,y1);}
		}
		else if(Math.abs(x1 - x2) == Math.abs(y1 - y2)){
			if (x2 < x1){
				if(y2 < y1){//
					System.out.print("startx"+x1+"  starty"+y1+"   endx"+x2+"   endy"+y2+"\n");
						for (; x2<=x1; x2++){
							fillSquare(g,Color.red,x2,y2);
							y2++;
						}
				}else {
					System.out.print("startx"+x1+"  starty"+y1+"   endx"+x2+"   endy"+y2+"\n");
						for(; x1>=x2; x1--){
							fillSquare(g,Color.red, x1, y1);
							y1++;
						}
				}
			}else{
				if(y2 < y1){
					System.out.print("startx"+x1+"  starty"+y1+"   endx"+x2+"   endy"+y2+"\n");
					for (; x1<=x2; x1++){
						fillSquare(g,Color.red,x1,y1);
						y1--;
					}
				}else {//
					System.out.print("startx"+x1+"  starty"+y1+"   endx"+x2+"   endy"+y2+"\n");
					for(; x1<=x2; x1++){
						fillSquare(g,Color.red, x1, y1);
						y1++;
					}
				}
			}
		}

	}
	
	/** This is what happens when the mouse is clicked!
	 * @param event
	 */
	public void onMouseClick(MouseEvent event) {
		int x = 0, y = 0;  //after methods will be a value inside the grid

		//This is a check of the cordinates to make sure they are in the grid. 
		if ((event.getX()) > (squareSize*s+15) ||
				event.getY() > squareSize*s+15 ||
				event.getX() < 15 || event.getY() < 15){
			;
		}else{//if it is then the x and y values get set
			lastX = x = getCol(event.getX());
			lastY = y = getRow(event.getY());
		}


		/*if the current action mode is seismic,
		alternate the sMode in between start end end */
		if (request.buttonGroup.getSelection().getActionCommand().equals("S"))
			sMode = (sMode == 1) ? 0 : 1; //this is used to determine what click it is

		//update the cell info box for the current selection
		setCellInfo(lastX, lastY);

		//repaint the map to display new selection
		repaint(); //calls paintComponent();

		//this is what happens when the right
		if ( event.isMetaDown()){                  	 
		}                 	 
	}

	/** Paint colors on the map based on the current mode indicated by the
	 * action radio buttons. Also highlights the current selected cell.
	 * @param g The Graphics object used to paint colors.
	 * @param x The x coordinate of the current selected cell.
	 * @param y The y coordinate of the current selected cell.
	 */
	public void paintColorsMode(Graphics g, int x, int y) {
		String rbs =request.buttonGroup.getSelection().getActionCommand(); //radio button selected

		if (rbs.equals("B")){   			 //If the bid radio box is selected
			colorBiddableLand(g);
			clickColor(g, Color.ORANGE,x, y);
			doBid(x, y);
		}
		else if(rbs.equals("D")){   		 //if the drill radio box is seleected
			colorDrillableLand(g);
			clickColor(g, Color.CYAN,x, y);
			doDrill(x, y);	
		}
		else if(rbs.equals("S")){   		 //if the seismic line radio box is selected
			colorSeismicKnowledge(g,currentLayer);

			int startX = 0, startY = 0, endX = 0, endY = 0;
			try {
				startX = request.getStartX();
				startY = request.getStartY();
				endX = request.getEndX();
				endY = request.getEndY();
			} catch (Exception e) {

			}

			//sMode is toggled with every mouse click in onMouseClick()
			//if sMode indicates setting the start value
			if (sMode==0){
				startX = x;
				startY = y;
				//display the starting x,y values in their textbox
				request.setStartX(startX);
				request.setStartY(startY);
				//clear the ending x,y values
				request.setendX("");
				request.setendY("");
				//instruct to select ending cell
				request.setseismicCostField("Click Second Cell.");
				//color the starting selection
				seismicColor(g, startX, startY, startX, startY);
			//if sMode indicates setting the end value
			}else{
				endX = x;
				endY = y;
				//display the x,y values in their textbox
				request.setendX(endX+"");
				request.setendY(endY+"");

				//get the cost and display it
				String cost = request.operator.
				getCostSeismic(startX, startY, endX, endY);
				request.setseismicCostField(cost);
				//color the selection
				seismicColor(g, startX, startY, endX, endY);

				/*if the selection is not valid,
				display a helpful message. */
				if(cost.equals("Invalid Coordinates.")) {
					request.setseismicCostField("Problem with Request.");
				}
			}
		}
	}

	public void doDrill(int col, int col2) {
		request.setStartX(col);
		request.setStartY(col2);
	}

	/**
	 * @param col
	 * @param col2
	 */
	public void doBid(int col, int col2) {
		request.setStartX(col);
		request.setStartY(col2);

	}
	
	/**
	 * This method iterates through each square and colors it if there is no owner
	 * @param g The Graphics object to use for painting.
	 */
	public void colorBiddableLand(Graphics g){
		String player = Requests.teamName;
		String owner;
		Point p;
		Grid grid = request.operator.getGrid();
		
		for(int i = 0; i < s; i++){
			for(int j = 0; j < s; j++) {
				p = new Point(j,i);
				owner=grid.getOwner(p);
				if(!grid.exists(p)||owner==null)
				{
					Color herp=new Color(240,240,240);
					fillSquare(g,herp,p.x,p.y);
				}
				else if(!grid.isDrilled(p))
				{
					if(owner.equals(player)){
						Color herp=new Color(46, 184, 0);
						fillSquare(g, herp,p.x,p.y);
					}
					else if(!owner.equals(player)){
						Color herp = new Color(123,0,185);
						fillSquare(g,herp,p.x,p.y);
					}
					else{
						Color herp = new Color(240,240,240);
						fillSquare(g,herp,p.x,p.y);
					}
				}
				else if (grid.isDrilled(p)){//light green
					Color herp=new Color(177,250,162);
					fillSquare(g,herp,p.x,p.y);
					if(grid.getOilRate(p)>0||grid.getGasRate(p)>0){
						if(owner.equals(player)){
							herp= new Color(0,0,0);
							fillSquare(g,herp,p.x,p.y);
						}
						else {
							herp = new Color(100,100,100);
							fillSquare(g,herp,p.x,p.y);
						}
					}
				}
				else{
					Color grey=new Color(240,240,240);
					fillSquare(g,grey,p.x,p.y);
				}
			}
		}

	}
	public Color breakColor(Color color, double change){
			int red= (int)((double)color.getRed()*change);
			int green = (int)((double)color.getGreen()*change);
			int blue = (int)((double)color.getBlue()*change);
			if (red < 0) red = 0;
			if (green < 0) green = 0;
			if (blue < 0) blue = 0;
			Color newColor= new Color (red, green, blue);
			return newColor;
		}
	/**
	 * Colors the land if the current player knows seismic information about a cell
	 * @param g The Graphics object to use for painting.
	 */
	public void colorSeismicKnowledge(Graphics g, int currentLayer){
		Point p;
		Grid grid = request.operator.getGrid();
		
		int lowest=grid.getLowest(); 
		int highest=grid.getHighest();
		int elevation;
		double paintColor,range,elev;
		range=highest-lowest;
		
		Color darkgreen= new Color(167,218,0);

		for(int i = 0; i < s; i++){
			for(int j = 0; j < s; j++) {
				p = new Point(j,i);
				elevation=grid.getElevation(p);
				elev=elevation-lowest;
				paintColor=(elev/range);
				paintColor=(paintColor * 0.5+0.5);
				Color gradient=breakColor(darkgreen,paintColor);
				
				
				if(grid.exists(p))
				{
					
					
					if(grid.seismicExists(p))
					{
						Color herp=new Color(82, 61, 0);
						fillSquare(g, herp,p.x,p.y);
					}
					else{
						Color grey=gradient;
						fillSquare(g, grey,p.x,p.y);
					}
				}else{
					Color grey=gradient;
					fillSquare(g, grey,p.x,p.y);}
			}
		}
	}

	/**
	 * @param g1 The Graphics object to use for painting.
	 */
	public void paintSeismicKnowledge(Graphics g1){
		Point p;
	//	int paintColor=0;

	//	int elevation=0;
		super.paintComponent(g1);
		Grid grid = request.operator.getGrid();
		
		for(int i = 0; i < s; i++){
			for(int j = 0; j < s; j++) {
				p = new Point(j,i);  
				
	//			elevation = grid.getElevation(p);
	//			paintColor=255*((elevation-lowest)/(highest-lowest));
				
				if(grid.exists(p))
				{					
					Integer seismicInfo[]=grid.getLayerArray(p);
					
					if(seismicInfo!=null&&seismicInfo[0]!=0)
					{
						Color herp=new Color(82, 61, 0);
						//      						fillSquare(herp,p.x,p.y);
						g1.setColor(herp);
						g1.fillRect(xMinCords[p.x]+1, yMinCords[p.y]+1, squareSize-1, squareSize-1);
					}
					else{
						Color grey=new Color(240,240,240);
						//	  						 fillSquare(grey,p.x,p.y);
						g1.setColor(grey);
						g1.fillRect(xMinCords[p.x]+1, yMinCords[p.y]+1, squareSize-1, squareSize-1);
					}
				}else{
					Color grey=new Color(240,240,240);
					//						 fillSquare(grey,p.x,p.y);
					g1.setColor(grey);
					g1.fillRect(xMinCords[p.x]+1, yMinCords[p.y]+1, squareSize-1, squareSize-1);
				}
			}
		}
	}



	/** Colors the land that the player can drill on
	 * @param g The Graphics object to use for painting.
	 */
	public void colorDrillableLand(Graphics g){
		String player = Requests.teamName;
		String owner;
		Point p;
		Grid grid = request.operator.getGrid();
		for(int i = 0; i < s; i++){
			for(int j = 0; j < s; j++) {
				p = new Point(j,i);
				owner = grid.getOwner(p);
				if(grid.exists(p)){
					if(grid.isDrilled(p)){
						if(owner.equals(player)){
							if((grid.getOilRate(p) > 0) || (grid.getGasRate(p) > 0)){
								Color oil = new Color(0,0,0); //Black (team oil)
								fillSquare(g, oil,p.x,p.y);
							}
							else{
								Color barren = new Color(255,0,127); //Barren (no oil)
								fillSquare(g, barren,p.x,p.y);
							}
						}
						else if(owner != null){
							if((grid.getOilRate(p) > 0) || (grid.getGasRate(p) > 0)){
								Color oil = new Color(100,100,100); //Gray (other team oil)
								fillSquare(g, oil,p.x,p.y);
							}
							else{
								Color barren = new Color(255,0,127); //Barren (no oil)
								fillSquare(g, barren,p.x,p.y);
							}
						}
					}
				}
				else{
					Color empty = new Color(240,240,240); //White
					fillSquare(g, empty,p.x,p.y);
				}
			}
		}
	}
	public void setCellInfo(int x, int y) {
		//get the grid from operator
		Grid grid = request.operator.getGrid();   				 
		Point p = new Point(x,y);    
		int depth = 0;

		//strings to be displayed
		String owner, oil, gas, layers, rocktype;
		//if the grid location exists (not null)
		if(grid.exists(p)) {   							 
			//get the owner name at p
			owner = grid.getOwner(p);
			//if the string is null or empty, display "Not Owned."
			if (owner == null || owner.equals("")) {
				owner = "Not Owned.";
			}

			//get the location of the top layer for seismic and rocktype
			while((grid.layerExists(p,depth) == false)
					&& depth < grid.getNumLayers())
				depth++;

			//if seismic data exists, display all of it, else display the top
			if(grid.seismicExists(p)) {
				//get the layer (seismic) array at p
				Integer[] layersA = grid.getLayerArray(p);
				layers = grid.removeEmptyFromArray(p,layersA);
			} else {
				layers = grid.getLayer(p,depth) + "";
			}

			//if p has been drilled, display all info, else display only the top rock.
			if(grid.isDrilled(p)) {
				//get the rock type array at p
				String[] rocktypeA = grid.getRockArray(p);
				rocktype = grid.removeEmptyFromArray(p,rocktypeA);

				//get the oil rate array at p
				Integer[] oilA = grid.getOilArray(p);
				oil = grid.removeEmptyFromArray(p, oilA);

				//get the gas rate array at p
				Integer[] gasA = grid.getGasArray(p);
				gas = grid.removeEmptyFromArray(p,gasA);

			} else {
				oil = gas = "No Information.";
				rocktype = grid.getRock(p, depth);
			}

			//if the gird location does not exist
		} else {
			owner = "Not Owned.";   						 //If the cell does not exist it says not owned
			oil = gas = layers = rocktype = "No Information.";   	 //NO INFO FOR YOU!
		}

		request.setCellInfo(owner, oil, gas, layers, rocktype);    //sets all of the data inside of requests
	}
	
	/* Method to place images on Map cells. Development incomplete. 
	 * Kept in case helpful for the future.
	 
	public void setCellImages(Graphics g) {
		Integer oilTemp[];
		Integer gasTemp[];
		LinkedList<Bid> bidQ;
		LinkedList<SeismicRequest> seismicQ;
		BufferedImage img = null;
		//Graphics g = this.getGraphics();
		//g.fillRect(5,5,100,100);

		Grid grid = request.operator.getGrid();
		bidQ = request.operator.getBidQueue();
		seismicQ = request.operator.getSeismicQueue();

		//loop through every cell
		for (int x = 0; x < s; x++){
			for (int y = 0; y < s; y++){
				boolean hasResource = false;
				Point p = new Point (x,y);

				if (grid.exists(p)){
					oilTemp = grid.getOilArray(p);
					gasTemp = grid.getGasArray(p);

					//draw oil image if oil is present
					for(int ot : oilTemp) {
						if (ot > 0){
							hasResource = true;

							try {
								img = ImageIO.read(new File("trunk/src/client/images/oil_small.gif"));
							} catch (IOException e){
								e.printStackTrace();
							}

							g.drawImage(img, x, y, null);
						}
					}

					//draw gas image if gas is present
					for(int gt : gasTemp) {
						if (gt > 0){
							hasResource = true;

							try {
								img = ImageIO.read(new File("trunk/src/client/images/gas_small.gif"));        	
							} catch (IOException e){
								e.printStackTrace();
							}

							g.drawImage(img, x, y, null);
						}

					}

					//draw empty image if neither is present
					if (hasResource == false){
						try {
							img = ImageIO.read(new File("trunk/src/client/images/empty_small.gif"));        	
						} catch (IOException e){
							e.printStackTrace();
						}

						g.drawImage(img, x, y, null);
					}
				}
			}
		}

		//draw bid image if point is being bid on
		for(Bid b : bidQ) {
			Point bidPoint = b.getPoint();  		try {
				img = ImageIO.read(new File("trunk/src/client/images/dollar.gif"));        	
			} catch (IOException e){
				e.printStackTrace();
			}

			g.drawImage(img, bidPoint.x, bidPoint.y, null);
		}

		//draw seismic image if point is requested
		for(SeismicRequest sr : seismicQ){
			int i = 0;
			Point seismicPoints[] = sr.getPoints();

			try {
				img = ImageIO.read(new File("trunk/src/client/images/seismicline_horizontal_small.gif"));        	
			} catch (IOException e){
				e.printStackTrace();
			}

			while (seismicPoints[i] != null){
				g.drawImage(img,seismicPoints[i].x, seismicPoints[i].y, null);
			}
		}
	} */

	@Override
	public void mouseClicked(MouseEvent event) {}
	@Override
	public void mouseEntered(MouseEvent event) {}
	@Override
	public void mouseExited(MouseEvent event) {}
	@Override
	public void mousePressed(MouseEvent event) {}
	@Override
	/** Use mouseRelease so that clicks can be registered
	when the mouse is in motion. */
	public void mouseReleased(MouseEvent event) {
		onMouseClick(event);
	}
	@Override
	public void actionPerformed(ActionEvent event) {}

	@Override
	public void mouseDragged(MouseEvent arg0) {}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		request.sethoverLabel(getCol(arg0.getX()), getRow(arg0.getY()));
		//set info on hover? -- setCellInfo(getCol(arg0.getX()),getRow(arg0.getY()));
	}
}

//bleeding the oil down to nothing
//disaster on drills where you have to redrill
//creating pictures for the type of rock