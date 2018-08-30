package bishop.evaluationStatistics;

import bishop.base.Color;
import bishop.base.IMaterialHashRead;
import bishop.base.MaterialHash;
import bishop.base.PieceType;
import bishop.engine.CoeffCountPositionEvaluation;
import bishop.engine.PositionEvaluationCoeffs;
import bishop.engine.TableMaterialEvaluator;
import math.IVector;
import math.IVectorRead;
import math.Vectors;
import regression.ISample;


public class EvaluationSample implements ISample {
    public static final int FEATURE_COUNT = PositionEvaluationCoeffs.LAST + PieceType.VARIABLE_COUNT;

    private final short[] coeffIndices;
    private final short[] coeffCounts;
    private final long materialHash;
    private final float evaluation;

    public EvaluationSample (final short[] coeffIndices, final short[] coeffCounts, final IMaterialHashRead materialHash, final float evaluation) {
        this.coeffIndices = coeffIndices;
        this.coeffCounts = coeffCounts;
        this.materialHash = materialHash.getHash();
        this.evaluation = evaluation;
    }

    @Override
    public IVectorRead getInput() {
        final IVector vector = Vectors.sparse(FEATURE_COUNT);

        for (int i = 0; i < coeffIndices.length; i++)
            vector.setElement(coeffIndices[i], (double) coeffCounts[i] / (double) CoeffCountPositionEvaluation.COEFF_MULTIPLICATOR);

        final MaterialHash hash = new MaterialHash(materialHash);

        for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
            final int pieceCountDiff = hash.getPieceCount(Color.WHITE, pieceType) - hash.getPieceCount(Color.BLACK, pieceType);
            vector.setElement(getIndexOfPieceTypeFeature(pieceType), pieceCountDiff);
        }

        return vector;
    }

    public static int getIndexOfPieceTypeFeature (final int pieceType) {
        return pieceType - PieceType.VARIABLE_FIRST + PositionEvaluationCoeffs.LAST;
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
        final long indicesSize = 12 + 4 + 2 * coeffIndices.length;
        final long countsSize = 12 + 4 + 2 * coeffCounts.length;

        return objSize + indicesSize + countsSize;
    }
}
