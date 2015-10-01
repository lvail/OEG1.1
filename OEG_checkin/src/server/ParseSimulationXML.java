package server;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import shared.*;
import shared.action.*;

/** ParseSimulationXML uses callback methods and the SAX library to parse the OEG
 * .xml simulation configuration file.
 * @author jweber2 */
public class ParseSimulationXML extends DefaultHandler {

	/** List of every element in OEG .xml file for reference in this code */
	public enum Element {
		NONE, SIMDATA, SIMNAME, SIMDOMAIN, XDIM, YDIM, GRIDSIZE, LAYER, TOP,
		OIL, GAS, ROCKTYPE, BOTTOM, SIMTIMESTEPS, NUMSTEPS, STEPTIME,
		SIMMONEY, SEISMICSETUP, SEISMICLINEAR, STARTCASH, DRILLCOST,
		MINLEASECOST, OILINCOME, GASINCOME, FUDGE, REMOVEPERCENT
	}

	/** elementMap to map the relation from an element's string name to its
	 * Element enum */
	Map<String, Element> elementMap;
	/** parentMap to map the relation from an Element enum to its parent Element */
	Map<Element, Element> parentMap;
	/** stack to keep track of the current hierarchical level in the xml file. */
	Stack<Element> stack;

	/** The Grid object where information from the XML file is being placed. */
	private Grid grid;
	/** Boolean that is true if the grid object has been instantiated and is
	 * ready to be used. Certain information must be known about the Grid from
	 * the XML before other information can be added to it. */
	private boolean gridStart;
	/** The number of layers in the simulation, as given in the XML file. */
	private int numLayer;
	/** The number of rows, or y values, being simulated as given in the XML */
	private int numRow;
	/** The number of columns, or x values, being simulated as given in the XML */
	private int numCol;
	/** The layer currently being added from the XML file. */
	private int curLayer = 0;
	
	private File xmlFile;

	/** Constructor that creates the stack, and two elementMap and parentMap
	 * objects.
	 * @param xmlFile 
	 * @param grid The Grid object that information in the XML is being added
	 * to. */
	public ParseSimulationXML(File xmlFile, Grid grid) {
		this.grid = grid;
		this.xmlFile = xmlFile;
		gridStart = false;

		stack = new Stack<Element>();
		stack.push(Element.NONE);

		elementMap = new HashMap<String, Element>();
		parentMap = new HashMap<Element, Element>();

		// create a Hierarchy array of every parent to use in the for loop
		Hierarchy[] parents = { new NONE(), new SIMDATA(), new SIMDOMAIN(),
				new SIMTIMESTEPS(), new SIMMONEY(), new LAYER() };

		// for every Hierarchy in parents
		for (Hierarchy h : parents) {
			// for every Element in the children of h
			for (Element e : h.children) {
				// map the name of e with e itself
				elementMap.put(e.name(), e);
				// map e with e's parent
				parentMap.put(e, h.parent);
			}
		}
	}

	/** Callback method called when a start element tag is found
	 * @param qName gives the name of the element
	 * @param attributes gives any attribute values tied in the element start
	 * tag. */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		// if the element name is valid and in the elementMap
		if (elementMap.containsKey(qName.toUpperCase())) {
			Element name = elementMap.get(qName.toUpperCase());
			// if the parent of the element is on the stack
			if (stack.peek() == parentMap.get(name))
				// push the element onto the stack
				stack.push(name);
			else {
				throw new SAXException("Element type " + qName
						+ " has incorrect nesting");
			}
		} else {
			throw new SAXException("Unknown Start Element Type: " + qName + ".");
		}

		String fileName;
		File csvFile;
		// switch mainly used to process attribute values of elements
		switch (stack.peek()) {
			case SIMDOMAIN:
				numLayer = Integer.parseInt(attributes.getValue("layers"));
				break;
			case SIMTIMESTEPS:
				Director.setTimeStep(Integer.parseInt(attributes
						.getValue("simsteptime")));
				break;
			case LAYER:
				if (!gridStart) {
					grid.setLimits(new Point(numRow, numCol), numLayer);
					grid.initializeAll();
					gridStart = true;
				}
				curLayer = Integer.parseInt(attributes.getValue("lay")) - 1;
				break;
			case TOP:
				fileName = attributes.getValue("src");
				csvFile = new File(xmlFile.getParentFile().getAbsolutePath(),fileName);
				grid.importCSV(csvFile, curLayer, Grid.LAYER);
				break;
			case OIL:
				fileName = attributes.getValue("src");
				csvFile = new File(xmlFile.getParentFile().getAbsolutePath(),fileName);
				grid.importCSV(csvFile, curLayer, Grid.OIL);
				break;
			case GAS:
				fileName = attributes.getValue("src");
				csvFile = new File(xmlFile.getParentFile().getAbsolutePath(),fileName);
				grid.importCSV(csvFile, curLayer, Grid.GAS);
				break;
		}
	}

	/** Callback method called when an end element tag is found */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		// if the element name is valid and in the elementMap
		if (elementMap.containsKey(qName.toUpperCase())) {
			Element name = elementMap.get(qName.toUpperCase());
			// take it off the stack
			stack.pop();
			// if the next item on the stack is not the parent of the element
			if (stack.peek() != parentMap.get(name)) {
				throw new SAXException("Element type " + qName
						+ " has incorrect nesting");
			}
		} else {
			throw new SAXException("Unknown End Element Type " + qName + ".");
		}
	}

	/** Callback method called when parsing a string within an element. */
	public void characters(char ch[], int start, int length)
			throws SAXException {
		if (!stack.empty()) {
			switch (stack.peek()) {
				case XDIM:
					numCol = Integer.parseInt(new String(ch, start, length));
					break;
				case YDIM:
					numRow = Integer.parseInt(new String(ch, start, length));
					break;
				case FUDGE:
					grid.setFudgePercent(Integer.parseInt(new String(ch,start,length)));
					break;
				case REMOVEPERCENT:
					grid.setRemovePrecent(Integer.parseInt(new String(ch,start,length)));
					break;				
				case ROCKTYPE:
					grid.addRock(new String(ch, start, length), curLayer);
					break;
				case STEPTIME:
					Director.setRoundTime((long) (60000 * Double
							.parseDouble(new String(ch, start, length))));
					break;
				case NUMSTEPS:
					Director.setNumRounds(Integer.parseInt(new String(ch,
							start, length)));
					break;
				case SEISMICSETUP:
					Action.setSeismicSetup(Integer.parseInt(new String(ch,
							start, length)));
					break;
				case SEISMICLINEAR:
					Action.setSeismicRate(Integer.parseInt(new String(ch,
							start, length)));
					break;
				case STARTCASH:
					BankAccount.setStart(Integer.parseInt(new String(ch, start,
							length)));
					break;
				case DRILLCOST:
					Action.setDrillCost(Integer.parseInt(new String(ch, start,
							length)));
					break;
				case MINLEASECOST:
					Action.setMinBid(Integer.parseInt(new String(ch, start,
							length)));
					break;
				case OILINCOME:
					Action.setOilRate(Integer.parseInt(new String(ch, start,
							length)));
					break;
				case GASINCOME:
					Action.setGasRate(Integer.parseInt(new String(ch, start,
							length)));
					break;
			}
		}
	}

	/** Callback method called when the end of the .xml document is reached */
	public void endDocument() throws SAXException {}

	/** Called when an error occurs while parsing the XML file. Most likely a
	 * syntax error. */
	public void fatalError(SAXParseException e) throws SAXException {
		throw new SAXException("File not valid OEG XML");
	}

	/** Hierarchical classes to represent the structure of the OEG .xml file.
	 * Each class has a Element parent, and an Element array of children. No
	 * element is included more than once. */
	private class Hierarchy {
		/** The child of the element */
		public Element[] children;
		/** The element itself */
		public Element parent;
	}

	/** Elements with no parent */
	private class NONE extends Hierarchy {
		{
			super.children = new Element[] { Element.SIMDATA };
			super.parent = Element.NONE;
		}
	}

	/** Elements with SIMDATA as parent */
	private class SIMDATA extends Hierarchy {
		{
			super.children = new Element[] { Element.SIMNAME,
					Element.SIMDOMAIN, Element.SIMTIMESTEPS, Element.SIMMONEY };
			super.parent = Element.SIMDATA;
		}
	}

	/** Elements with SIMDOMAIN as parent */
	private class SIMDOMAIN extends Hierarchy {
		{
			super.children = new Element[] { Element.FUDGE, Element.REMOVEPERCENT, Element.XDIM, Element.YDIM,
					Element.GRIDSIZE, Element.LAYER, Element.BOTTOM };
			super.parent = Element.SIMDOMAIN;
		}
	}

	/** Elements with SIMTIMESTEPS as parent */
	private class SIMTIMESTEPS extends Hierarchy {
		{
			super.children = new Element[] { Element.NUMSTEPS, Element.STEPTIME };
			super.parent = Element.SIMTIMESTEPS;
		}
	}

	/** Elements with SIMMONEY as parent */
	private class SIMMONEY extends Hierarchy {
		{
			super.children = new Element[] { Element.SEISMICSETUP,
					Element.SEISMICLINEAR, Element.STARTCASH,
					Element.DRILLCOST, Element.MINLEASECOST, Element.OILINCOME,
					Element.GASINCOME };
			super.parent = Element.SIMMONEY;
		}
	}

	/** Elements with LAYER as parent */
	private class LAYER extends Hierarchy {
		{
			super.children = new Element[] { Element.TOP, Element.OIL,
					Element.GAS, Element.ROCKTYPE };
			super.parent = Element.LAYER;
		}
	}
}
