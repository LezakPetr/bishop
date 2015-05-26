package bishop.tablebase;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import range.EnumerationProbabilityModel;
import utils.IoUtils;
import bishop.base.Position;

public class TableStatistics {
	
	private int[] symbolToResultMap;
	private Map<Integer, Integer> resultToSymbolMap;
	private long[][] symbolFrequencies;
	private Map<Long, int[]> symbolProbabilities;

	public void calculate (final ITable table, final int blockLength) {
		final Set<Integer> resultSet = getResultSet(table);
		fillSymbols(table, blockLength, resultSet);
	}
	
	private long[][] calculateSymbolFrequencies(final ITable table, final int blockLength) {
		final int symbolCount = symbolToResultMap.length;
		final PreviousSymbolProbabilityModelSelector selector = new PreviousSymbolProbabilityModelSelector(symbolCount, table.getDefinition().getMaterialHash());
		
		final int modelCount = selector.getModelCount();
		final long[][] symbolFrequencies = new long[modelCount][symbolCount];
		final Position position = new Position();
		
		long sampleCount = 0;
		
		for (ITableIterator it = table.getIterator(); it.isValid(); it.next()) {
			final int result = it.getResult();
			
			if (result != TableResult.ILLEGAL) {
				it.fillPosition(position);

				final int modelIndex = (int) selector.getModelIndex(position);
				final int symbol = resultToSymbolMap.get(result);
				selector.addSymbol(position, symbol);
				
				symbolFrequencies[modelIndex][symbol]++;
			}
			
			if (sampleCount % blockLength == 0) {
				selector.resetSymbols();
			}
			
			sampleCount++;
		}
		
		return symbolFrequencies;
	}
	
	private Set<Integer> getResultSet(final ITable table) {
		final Set<Integer> resultSet = new TreeSet<Integer>();
		
		for (ITableIterator it = table.getIterator(); it.isValid(); it.next()) {
			final int result = it.getResult();
			
			if (result != TableResult.ILLEGAL) {
				resultSet.add(result);
			}
		}
		
		return resultSet;
	}
	
	private void fillSymbols(final ITable table, final int blockLength, final Set<Integer> resultSet) {
		final int resultSize = resultSet.size();
		
		symbolToResultMap = new int[resultSize];
		resultToSymbolMap = new TreeMap<Integer, Integer>();
		
		int symbol = 0;
		
		for (int result: resultSet) {
			symbolToResultMap[symbol] = result;
			resultToSymbolMap.put(result, symbol);
			
			symbol++;
		}

		symbolFrequencies = calculateSymbolFrequencies(table, blockLength);
		symbolProbabilities = new HashMap<Long, int[]>();
		
		for (int modelIndex = 0; modelIndex < symbolFrequencies.length; modelIndex++) {
			final int[] probabilities = EnumerationProbabilityModel.calculateSymbolProbabilities(symbolFrequencies[modelIndex]);
			symbolProbabilities.put((long) modelIndex, probabilities);
		}
	}
	
	public int[] getSymbolToResultMap() {
		return Arrays.copyOf(symbolToResultMap, symbolToResultMap.length);
	}
	
	public Map<Integer, Integer> getResultToSymbolMap() {
		return resultToSymbolMap;
	}
	
	public Map<Long, int[]> getSymbolProbabilities() {
		return symbolProbabilities;
	}
	
	public void print (final PrintStream stream) {
		double shanons = 0.0;
		
		for (int modelIndex = 0; modelIndex < symbolFrequencies.length; modelIndex++) {
			stream.println ("Label: " + modelIndex);
			
			final long[] labelSymbolFrequencies = symbolFrequencies[modelIndex];
			final long labelSymbolCount = getLabelSymbolCount(labelSymbolFrequencies);
			
			double labelShanons = 0.0;
			
			for (int symbol = 0; symbol < labelSymbolFrequencies.length; symbol++) {
				final long frequency = labelSymbolFrequencies[symbol];
				
				if (frequency > 0) {
					final double prob = (double) frequency / (double) labelSymbolCount;
					labelShanons -= frequency * Math.log(prob) / Math.log(2);
					
					final String result = TableResult.toString (symbolToResultMap[symbol]);
					stream.println (result + ": " + frequency);
				}
			}
			
			stream.print ("Size ");
			IoUtils.writeSize(stream, labelShanons / 8);
			stream.println ();
			stream.println ();
			
			shanons += labelShanons;
		}
		
		stream.print ("Total size ");
		IoUtils.writeSize(stream, shanons / 8);
		stream.println ();
		stream.println ();
	}

	public double getSize() {
		double headerSize = 0.0;
		double shanons = 0.0;
		
		for (long[] labelSymbolFrequencies: symbolFrequencies) {
			final long labelSymbolCount = getLabelSymbolCount(labelSymbolFrequencies);
			
			for (int symbol = 0; symbol < labelSymbolFrequencies.length; symbol++) {
				final long frequency = labelSymbolFrequencies[symbol];
				
				if (frequency > 0) {
					final double prob = (double) frequency / (double) labelSymbolCount;
					shanons -= frequency * Math.log(prob) / Math.log(2);
					
					headerSize += 2;
				}
			}
		}
		
		return shanons / 8 + headerSize;
	}

	private long getLabelSymbolCount(final long[] labelSymbolFrequencies) {
		long totalResultCount = 0;
		
		for (long symbolFrequency: labelSymbolFrequencies) {
			totalResultCount += symbolFrequency;
		}
		return totalResultCount;
	}

}
