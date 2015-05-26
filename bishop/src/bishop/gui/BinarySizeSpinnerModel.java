package bishop.gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractSpinnerModel;

@SuppressWarnings("serial")
public class BinarySizeSpinnerModel extends AbstractSpinnerModel {
	
	private int offset;
	private int currentExponent;
	private int minExponent;
	private int maxExponent;
	
	private static final String[] UNITS = { "B", "kB", "MB", "GB" };
	
	private String exponentToString(final int exponent) {
		final int sizeExponent = exponent + offset;
		
		for (int i = UNITS.length - 1; i >= 0; i--) {
			final int quantityOffset = 10 * i;
			
			if (sizeExponent >= quantityOffset) {
				final long value = 1 << ((long) (sizeExponent - quantityOffset));
				
				return Long.toString(value) + UNITS[i];
			}
		}
		
		throw new RuntimeException("Exponent is negative");		
	}
	
	private Map<Integer, String> forwardTable;
	private Map<String, Integer> backwardTable;
	
	
	public BinarySizeSpinnerModel(final int exponent, final int offset, final int minExponent, final int maxExponent) {
		this.minExponent = minExponent;
		this.maxExponent = maxExponent;
		this.offset = offset;
		
		setExponent(exponent);
		
		// Create tables
		forwardTable = new HashMap<Integer, String>();
		backwardTable = new HashMap<String, Integer>();
		
		for (int i = minExponent; i <= maxExponent; i++) {
			final String str = exponentToString (i);
			
			forwardTable.put(i, str);
			backwardTable.put(str, i);
		}
	}
	
	public Object getNextValue() {
		final String value = (currentExponent < maxExponent) ? forwardTable.get(currentExponent + 1) : null;
		
		return value;
	}

	public Object getPreviousValue() {
		final String value = (currentExponent > minExponent) ? forwardTable.get(currentExponent - 1) : null;
		
		return value;
	}

	public Object getValue() {
		final String value = forwardTable.get(currentExponent);
		
		return value;
	}

	public void setValue(final Object value) {
		setExponent(backwardTable.get(value.toString()));
	}
	
	public int getExponent() {
		return currentExponent;
	}
	
	public void setExponent(final int exponent) {
		currentExponent = Math.min(Math.max(exponent, minExponent), maxExponent);
		fireStateChanged();
	}

}
