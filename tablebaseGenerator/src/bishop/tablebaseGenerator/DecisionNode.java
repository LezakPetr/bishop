package bishop.tablebaseGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import utils.ICopyable;

public class DecisionNode implements ICopyable<DecisionNode> {
	
	private String divisor;
	private final Map<Long, DecisionNode> children;
	private double undividedSize;
	
	public DecisionNode() {
		this.children = new HashMap<Long, DecisionNode>();
	}

	public String getDivisor() {
		return divisor;
	}

	public void setDivisor(String divisor) {
		this.divisor = divisor;
	}

	public double getUndividedSize() {
		return undividedSize;
	}

	public void setUndividedSize(final double size) {
		this.undividedSize = size;
	}
	
	public double getDividedSize() {
		if (children.isEmpty())
			return undividedSize;
		
		double size = 0.0;
		
		for (DecisionNode child: children.values()) 
			size += child.getUndividedSize();
		
		return size;
	}
	
	public void addChild (final long key, final DecisionNode child) {
		this.children.put(key, child);
	}

	@Override
	public DecisionNode copy() {
		final DecisionNode result = new DecisionNode();
		
		result.divisor = divisor;
		result.undividedSize = undividedSize;
		
		for (Entry<Long, DecisionNode> child: children.entrySet())
			result.children.put(child.getKey(), child.getValue().copy());
		
		return result;
	}
	
}
