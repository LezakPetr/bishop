package magicCalculator;

import java.util.Random;

import optimization.IState;

public class MagicState implements IState<MagicState, MagicSettings> {
	 
	private long coeff;

	public MagicState() {
	}
	
	public MagicState (final MagicState orig) {
		this.coeff = orig.coeff;
	}
	
	@Override
	public void randomInitialize(final Random random, final MagicSettings settings) {
		coeff = random.nextLong() & ~settings.getSpareBitMask();
	}
	
	@Override
	public void randomChange(final Random random, final MagicSettings settings) {
		final int firstSpareBit = settings.getFirstSpareBit();
		final int index = random.nextInt(firstSpareBit);
		coeff ^= 1L << index;
	}

	@Override
	public MagicState copy() {
		return new MagicState (this);
	}

	public long getCoeff() {
		return coeff;
	}

}
