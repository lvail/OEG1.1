package server;


public class CellKnowledge {
	
	private Cell[] cells;
	private int currIndex;
	
	public CellKnowledge(){
		
	}
	
	public int getGas(int x, int y, int depth){
		return findCell(x,y).gas[depth];
	}
	
	public int getOil(int x, int y, int depth){
		return findCell(x,y).oil[depth];
	}
	
	public int getLayer(int x, int y, int depth){
		return findCell(x,y).layers[depth];
	}
	
	private Cell findCell(int x, int y){
		for(int i = 0; i < currIndex; i++) {
			if(cells[i].xCoord == x && cells[i].yCoord == y) {
				return cells[i];
			}
		}		
		return null;
	}
}
