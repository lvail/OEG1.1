package client;

public class TestRandom {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int t = 0, f = 0;
		for(int i = 0; i < 10000; i++) {
			if((Math.random() * 100) < 100) {
				System.out.println("true");
				t++;
			} else {
				System.out.println("false");
				f++;
			}
		}
		
		System.out.println("true: " + t + " false: " + f);
	}

}
