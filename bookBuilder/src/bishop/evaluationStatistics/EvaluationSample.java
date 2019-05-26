package bishop.evaluationStatistics;

import bishop.base.Color;
import bishop.base.IMaterialHashRead;
import bishop.base.MaterialHash;
import bishop.base.PieceType;
import bishop.engine.CoeffCountPositionEvaluation;
import bishop.engine.CoeffRegistry;
import bishop.engine.PositionEvaluationCoeffs;
import bishop.tablebase.ClassificationProbabilityModelSelector;
import math.IVector;
import math.IVectorRead;
import math.Vectors;
import regression.ISample;
import utils.IoUtils;

import java.io.*;


public class EvaluationSample implements ISample {
	public static final int POSITIONAL_FEATURE_COUNT = PositionEvaluationCoeffs.getCoeffRegistry().getFeatureCount();
	public static final int FEATURE_COUNT = POSITIONAL_FEATURE_COUNT + PieceType.VARIABLE_COUNT;

    private static final int COEFF_COUNT_CATEGORY_PLUS_ONE = 0;
	private static final int COEFF_COUNT_CATEGORY_MINUS_ONE = 1;
	private static final int COEFF_COUNT_CATEGORY_LARGE = 2;

	private static final int COEFF_COUNT_CATEGORY_LAST = 3;

	private static final int MAX_SMALL_INDEX_DIFF = 256 / COEFF_COUNT_CATEGORY_LAST;
	private static final int LARGE_INDEX_DIFF_MARKER = 0;

	private static final int LARGE_INDEX_DIFF_BYTES = 3;
	private static final int LARGE_COEFF_COUNT_BYTES = 2;

    private final byte[] compressedCoeffs;
    private final long materialHash;
    private final float evaluation;

    public EvaluationSample (final int[] coeffIndices, final int[] coeffCounts, final IMaterialHashRead materialHash, final float evaluation) {
    	this.compressedCoeffs = compressCoeffs(coeffIndices, coeffCounts);
        this.materialHash = materialHash.getHash();
        this.evaluation = evaluation;
    }

    private int getCountCategory (final int coeffCount) {
    	switch (coeffCount) {
			case +CoeffCountPositionEvaluation.COEFF_MULTIPLICATOR:
				return COEFF_COUNT_CATEGORY_PLUS_ONE;

			case -CoeffCountPositionEvaluation.COEFF_MULTIPLICATOR:
				return COEFF_COUNT_CATEGORY_MINUS_ONE;

			default:
				return COEFF_COUNT_CATEGORY_LARGE;
		}
	}

    private byte[] compressCoeffs (final int[] coeffIndices, final int[] coeffCounts) {
    	try {
			final ByteArrayOutputStream stream = new ByteArrayOutputStream();
			int lastIndex = 0;

			for (int i = 0; i < coeffIndices.length; i++) {
				final int coeffCount = coeffCounts[i];

				if (coeffCount != 0) {
					final int indexDiff = coeffIndices[i] - lastIndex;
					final int countCategory = getCountCategory(coeffCount);
					final int smallIndexDiff = (indexDiff < MAX_SMALL_INDEX_DIFF) ? indexDiff : LARGE_INDEX_DIFF_MARKER;
					final int firstByte = countCategory * MAX_SMALL_INDEX_DIFF + smallIndexDiff;
					stream.write(firstByte);

					if (countCategory == COEFF_COUNT_CATEGORY_LARGE)
						IoUtils.writeNumberBinary(stream, coeffCount, LARGE_COEFF_COUNT_BYTES);

					if (smallIndexDiff == LARGE_INDEX_DIFF_MARKER)
						IoUtils.writeNumberBinary(stream, indexDiff, LARGE_INDEX_DIFF_BYTES);

					lastIndex = coeffIndices[i];
				}
			}

			return stream.toByteArray();
		}
		catch (IOException ex) {
    		throw new RuntimeException("Cannot compress coefficients");
		}
	}

	private int getCoeffCountFromStream (final int countCategory, final InputStream stream) throws IOException {
    	switch (countCategory) {
			case COEFF_COUNT_CATEGORY_PLUS_ONE:
				return +CoeffCountPositionEvaluation.COEFF_MULTIPLICATOR;

			case COEFF_COUNT_CATEGORY_MINUS_ONE:
				return -CoeffCountPositionEvaluation.COEFF_MULTIPLICATOR;

			default:
				return (int) IoUtils.readSignedNumberBinary(stream, LARGE_COEFF_COUNT_BYTES);
		}
	}

	private int getIndexDiffFromStream (final int smallIndexDiff, final InputStream stream) throws IOException {
    	if (smallIndexDiff == LARGE_INDEX_DIFF_MARKER)
			return (int) IoUtils.readUnsignedNumberBinary(stream, LARGE_INDEX_DIFF_BYTES);
		else
			return smallIndexDiff;
	}

	@Override
    public IVectorRead getInput() {
    	try {
			final IVector vector = Vectors.sparse(FEATURE_COUNT);
			addPositionalCoeffs(vector);
			addMaterialCoeffs(vector);

			return vector.freeze();
		}
		catch (IOException ex) {
    		throw new RuntimeException("Cannot decompress coefficients", ex);
		}
    }

	private void addMaterialCoeffs(IVector vector) {
		final MaterialHash hash = new MaterialHash(materialHash);

		for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
			final int pieceCountDiff = hash.getPieceCount(Color.WHITE, pieceType) - hash.getPieceCount(Color.BLACK, pieceType);
			vector.setElement(getIndexOfPieceTypeFeature(pieceType), pieceCountDiff);
		}
	}

	private void addPositionalCoeffs(IVector vector) throws IOException {
		final ByteArrayInputStream stream = new ByteArrayInputStream(compressedCoeffs);
		int lastIndex = 0;

		while (true) {
			final int firstByte = stream.read();

			if (firstByte < 0)
				break;

			final int countCategory = firstByte / MAX_SMALL_INDEX_DIFF;
			final int smallIndexDiff = firstByte % MAX_SMALL_INDEX_DIFF;

			final int coeffCount = getCoeffCountFromStream(countCategory, stream);
			final int indexDiff = getIndexDiffFromStream(smallIndexDiff, stream);
			lastIndex += indexDiff;

			final IVectorRead features = PositionEvaluationCoeffs
					.getCoeffRegistry()
					.getFeaturesOfCoeff(lastIndex)
					.multiply((double) coeffCount / (double) CoeffCountPositionEvaluation.COEFF_MULTIPLICATOR);

			Vectors.addInPlace(
					vector,
					features
			);
		}
	}

	public static int getIndexOfPieceTypeFeature (final int pieceType) {
        return pieceType - PieceType.VARIABLE_FIRST + POSITIONAL_FEATURE_COUNT;
    }

    @Override
    public IVectorRead getOutput() {
        return Vectors.of(evaluation);
    }

    @Override
    public double getWeight() {
        return 1.0;
    }

    public long estimateMemoryConsumption() {
        final long objSize = 12 + 4 + 4 + 4 + 8;
        final long compressedCoeffsSize = 12 + 4 + compressedCoeffs.length;

        return objSize + compressedCoeffsSize;
    }
}
