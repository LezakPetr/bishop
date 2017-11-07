package bishop.engine;

import java.util.ArrayList;
import java.util.List;

public class CoeffLink {
	
	public static class Node {
		private final int coeff;
		private final double multiplier;
		
		public Node (final int coeff, final double multiplier) {
			this.coeff = coeff;
			this.multiplier = multiplier;
		}

		public int getCoeff() {
			return coeff;
		}

		public double getMultiplier() {
			return multiplier;
		}
	}
	
	private final List<Node> nodeList = new ArrayList<>();
	private final double weight;
	
	public CoeffLink (final int coeff1, final int coeff2, final double weight) {
		addNode(coeff1, 1.0);
		addNode(coeff2, -1.0);
		
		this.weight = weight;
	}
	
	public CoeffLink (final double weight) {
		this.weight = weight;
	}
	
	public CoeffLink addNode (final int coeff, final double multiplicator) {
		this.nodeList.add(new Node (coeff, multiplicator));
		
		return this;
	}

	public double getWeight() {
		return weight;
	}
	
	public int getNodeCount() {
		return nodeList.size();
	}
	
	public Node getNodeAt (final int index) {
		return nodeList.get(index);
	}
}
