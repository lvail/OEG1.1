package server;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;


public class Director {
	Operator[] operators;
	Grid grid;
	
	int currIndex;
	
	public Director() {
		operators = new Operator[150];
		
		//load data from XML.
		loadData();
		
		currIndex = 0;	
	}
	
	private void loadData() {
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			ParseSimulationXML handler = new ParseSimulationXML(this);

			saxParser.parse("trunk/datafiles/sample.xml", handler);

		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	public void setGrid(Grid grid) {
		this.grid = grid;
	}
	
	public void newClient(ClientMessages client, String name) {
		int exists = -1;
		//Checks to see if the operator (based on name) already exists
		for(int i = 0; i < currIndex; i++) {
			if(operators[i].getName().equals(name)) {
				exists = i;
			}
		}
		//if the operator is new 
		if(exists == -1) {
			//create a new Operator with team name and socket
			operators[currIndex] = new Operator(name, client);
			currIndex++;
		}
		else {
			//closes the operator's old socket
			operators[exists].getSocket().close();
			/*stores the client's new socket in operator
			 and requests a re-send of information*/
			operators[exists].restore(client);
			client.sendData("chat:Server: Welcome Back!");
		}
	}
	
	public void sendChat(String message) {
		for(int i = 0; i < currIndex; i++) {
			operators[i].getSocket().sendData(message);
		}
	}
	
	public void saveGame() {
		for(int i = 0; i < currIndex; i++) {
			operators[i].storeInformation();
		}
	}
}
