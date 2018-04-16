package bishop.engine;

import bishop.base.Color;
import utils.Mixer;

public class PawnEndingKey {
    private final long whitePawns;
    private final long blackPawns;

    private final int hash;

    public PawnEndingKey (final long... pawnMasks) {
        this.whitePawns = pawnMasks[Color.WHITE];
        this.blackPawns = pawnMasks[Color.BLACK];

        this.hash = Mixer.mixLongToInt(31 * whitePawns + blackPawns);
    }

    public long getWhitePawns() {
        return whitePawns;
    }

    public long getBlackPawns() {
        return blackPawns;
    }

    public long getPawnOccupancy() {
        return whitePawns | blackPawns;
    }

    public long getPawnMask(final int color) {
        if (color == Color.WHITE)
            return whitePawns;
        else
            return blackPawns;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj == null || this.getClass() != obj.getClass())
            return false;

        final PawnEndingKey that = (PawnEndingKey) obj;

        return this.hash == that.hash &&
                this.whitePawns == that.whitePawns &&
                this.blackPawns == that.blackPawns;
    }

}
