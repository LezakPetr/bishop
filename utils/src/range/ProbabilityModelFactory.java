package range;

public class ProbabilityModelFactory {
	
	private static final int MAX_FRACTION = 64;
	private static final IProbabilityModel[][] BINARY_FRACTIONS = initializeBinaryFractions();
	
	private static final IProbabilityModel[][] initializeBinaryFractions() {
		final IProbabilityModel[][] fractions = new IProbabilityModel[MAX_FRACTION + 1][];
		
		for (int i = 0; i < fractions.length; i++)
			fractions[i] = new IProbabilityModel[i+1];
		
		for (int denominator = 1; denominator < fractions.length; denominator++) {
			for (int numerator = 0; numerator <= denominator; numerator++) {
				if (fractions[denominator][numerator] == null) {
					final int treshold = Math.max(1, Math.min(RangeBase.MAX_SYMBOL_CDF - 1, (RangeBase.MAX_SYMBOL_CDF * numerator + denominator / 2) / denominator));
					final IProbabilityModel model = new BinaryProbabilityModel(treshold);
					
					for (int i = 1; denominator * i <= MAX_FRACTION; i++)
						fractions[i*denominator][i*numerator] = model;
				}
			}
		}
		
		return fractions;
	}
	
	private static void validateSymbolProbabilities(final int[] symbolProbabilities) {
		long sum = 0;
		
		if (symbolProbabilities.length > RangeBase.MAX_SYMBOL_CDF / RangeBase.MIN_SYMBOL_PROBABILITY)
			throw new RuntimeException("Too many symbols");
		
		for (int i = 0; i < symbolProbabilities.length; i++) {
			final int probability = symbolProbabilities[i];
			
			if (probability < RangeBase.MIN_SYMBOL_PROBABILITY)
				throw new RuntimeException("Probability out of range: " + probability);
			
			sum += probability;
		}
		
		if (sum != RangeBase.MAX_SYMBOL_CDF)
			throw new RuntimeException("Wrong sum of probabilities");
	}

	/**
	 * Calculates probability model from given frequency array.
	 * @param symbolFrequencies array of symbol frequencies
	 * @return probability model
	 */
	public static IProbabilityModel fromUnnormalizedProbabilities(final long... symbolFrequencies) {
		final int[] probabilities = normalizeProbabilities(symbolFrequencies);
		
		return fromProbabilities(probabilities);
	}

	/**
	 * Calculates probability array from given frequency array.
	 * @param symbolFrequencies array of symbol frequencies
	 * @return probability array
	 */
	public static int[] normalizeProbabilities(final long... symbolFrequencies) {
		// Calculate sum of frequencies
		long frequencySum = 0;
		
		for (long frequency: symbolFrequencies) {
			frequencySum += frequency;
			
			if (frequencySum < 0)
				throw new RuntimeException("Too big symbol frequencies, overflow may occur"); 
		}
		
		if (frequencySum >= (Long.MAX_VALUE / (2 * RangeBase.MAX_SYMBOL_CDF)))
			throw new RuntimeException("Too big symbol frequencies, overflow may occur");
		
		// Distribute the probability range. We have to ensure that every symbol will have at least MIN_SYMBOL_PROBABILITY probability.
		final int symbolCount = symbolFrequencies.length;
		final int[] symbolProbabilities = new int[symbolCount];
		int remainingProbabilityRange = RangeBase.MAX_SYMBOL_CDF;
		
		for (int i = 0; i < symbolCount; i++) {
			final long frequency = symbolFrequencies[i];
			
			// probability = round (frequency * remainingProbabilityRange / frequencySum)
			int probability = (frequency == 0) ? 0 : (int) ((frequency * remainingProbabilityRange + frequencySum / 2) / frequencySum);
			
			final int remainingSymbolCount = symbolCount - i - 1;
			probability = Math.min(probability, remainingProbabilityRange - remainingSymbolCount);
			probability = Math.max (probability, RangeBase.MIN_SYMBOL_PROBABILITY);
			
			symbolProbabilities[i] = probability;
			
			remainingProbabilityRange -= probability;
			frequencySum -= frequency;
		}
		
		symbolProbabilities[symbolProbabilities.length - 1] += remainingProbabilityRange;
		
		return symbolProbabilities;
	}

	public static IProbabilityModel fromProbabilities (final int...symbolProbabilities) {
		validateSymbolProbabilities(symbolProbabilities);
		
		switch (symbolProbabilities.length) {
			case 1:
				return UnaryProbabilityModel.getInstance();
				
			case 2:
				return new BinaryProbabilityModel(symbolProbabilities[0]);
				
			default:
				return new EnumerationProbabilityModel(symbolProbabilities);
		}
	}

	public static IProbabilityModel createBinaryFraction (final int numerator, final int denominator) {
		if (denominator < 0 || numerator < 0)
			throw new RuntimeException("Internal error: invalid fraction");
		
		return BINARY_FRACTIONS[denominator][numerator];
	}
}
