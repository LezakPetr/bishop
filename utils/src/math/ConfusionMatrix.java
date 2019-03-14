package math;

public class ConfusionMatrix {
	private final long[][] counts;

	public ConfusionMatrix(final int size) {
		counts = new long[size][size];
	}

	public void addSample(final int actual, final int predicted) {
		counts[actual][predicted]++;
	}

	public void log() {
		for (int i = 0; i < counts.length; i++) {
			for (int j = 0; j < counts[i].length; j++) {
				System.out.print(counts[i][j] + " ");
			}

			System.out.println();
		}
	}
}
