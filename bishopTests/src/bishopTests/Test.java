package bishopTests;

public class Test {

	public static final int CNT = 1000000;
	public static final int[] x = new int[CNT];
	
	public static void a() {
		for (int i = 0; i < 10000; i++) {
			int sum = 0;
			
			for (int j = 0; j < CNT; j++) {
				sum += x[j];
				x[j] = sum;
			}
		}
	}
	
	public static void main(String[] args) {
		a();
		
		final long t1 = System.currentTimeMillis();
		a();
		final long t2 = System.currentTimeMillis();
		
		System.out.println (t2 - t1);
	}
}
