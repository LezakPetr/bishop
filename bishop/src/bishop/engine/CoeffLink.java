package bishop.engine;

public class CoeffLink {
	
	private final int coeff1;
	private final int coeff2;
	private final double weight;
	
	public CoeffLink (final int coeff1, final int coeff2, final double weight) {
		this.coeff1 = coeff1;
		this.coeff2 = coeff2;
		this.weight = weight;
	}

	public int getCoeff1() {
		return coeff1;
	}

	public int getCoeff2() {
		return coeff2;
	}

	public double getWeight() {
		return weight;
	}
}
