package magicCalculator;

import java.util.Arrays;

import optimization.SimulatedAnnealing;

import bishop.base.BitBoard;
import bishop.base.CrossDirection;
import bishop.base.LineIndexer;
import bishop.base.Square;

public class MagicOptimizer {

	private static final int OPTIMIZATION_STEP_COUNT = 1000000;
	
	private long[] bestCoeffs = new long[CrossDirection.LAST * Square.LAST];
	private int[] bestDepths = new int[CrossDirection.LAST * Square.LAST];
	private int notOptimalCellCount;
	
	public MagicOptimizer() {
		Arrays.fill(bestDepths, Integer.MAX_VALUE);
	}
	
	public void fillFromLineIndexer() {
		System.arraycopy(LineIndexer.getCoeffs(), 0, bestCoeffs, 0, bestCoeffs.length);
		System.arraycopy(LineIndexer.getDepths(), 0, bestDepths, 0, bestDepths.length);
	}
	
	public void optimize() {
		do {
			notOptimalCellCount = 0;
			
			for (int direction = CrossDirection.FIRST; direction < CrossDirection.LAST; direction++) {
				for (int square = Square.FIRST; square < Square.LAST; square++) {
					optimizeLine(direction, square);
				}
			}
						
			printResults();

			System.out.println ("Non-optimal squares: " + notOptimalCellCount);
		} while (notOptimalCellCount > 0);
	}
		
	private void optimizeLine (final int direction, final int square) {
		final int cellIndex = LineIndexer.getCellIndex (direction, square);
		final long mask = LineIndexer.calculateDirectionMask(direction, square);
		final int bitCount = BitBoard.getSquareCount(mask);
		final int oldBestDepth = bestDepths[cellIndex];
		
		if (oldBestDepth > bitCount) {
			final MagicSettings settings = new MagicSettings();
			final int testedDepth = Math.min(oldBestDepth - 1, bitCount + 2);
			
			settings.setDepth(testedDepth);
			settings.setMask(mask);
			
			final SimulatedAnnealing<MagicState, MagicSettings> annealing = new SimulatedAnnealing<MagicState, MagicSettings>(MagicState.class, CollisionCalculator.class);
			annealing.setOptimizationStepCount(OPTIMIZATION_STEP_COUNT);
			annealing.setSettings(settings);
			
			final CollisionCalculator calculator = new CollisionCalculator();
			calculator.setSettings(settings);
	
			final MagicState state = annealing.optimize();
	
			if (!calculator.hasCollisions(state)) {
				bestDepths[cellIndex] = testedDepth;
				bestCoeffs[cellIndex] = state.getCoeff();
			}
			
			if (bestDepths[cellIndex] > bitCount)						
				notOptimalCellCount++;
		}
	}
	
	private void printResults() {
		printCoeffs();
		System.out.println();
		printDepths();
	}

	private void printCoeffs() {
		System.out.println ("\tprivate static final long[] COEFFS = new long[] {");
		
		for (int i = 0; i < bestCoeffs.length; i++) {
			if (i % 4 == 0)
				System.out.print ("\t\t");
			
			System.out.print (bestCoeffs[i] + "L");
			
			if (i != bestCoeffs.length - 1)
				System.out.print (", ");
			
			if (i % 4 == 3)
				System.out.println();

		}
		
		System.out.println ("\t};");
	}
	
	private void printDepths() {
		System.out.println ("\tprivate static final int[] DEPTHS = {");
		
		for (int i = 0; i < bestDepths.length; i++) {
			if (i % 8 == 0)
				System.out.print ("\t\t");
			
			System.out.print (bestDepths[i]);
			
			if (i != bestDepths.length - 1)
				System.out.print (", ");
			
			if (i % 8 == 7)
				System.out.println();
		}
		
		System.out.println ("\t};");
	}

}
