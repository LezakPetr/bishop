package bishopTests;

import bishop.base.Color;
import bishop.base.Rank;
import org.junit.Assert;
import org.junit.Test;

public class RankTest {
	@Test
	public void getOppositeRankTest() {
		Assert.assertEquals(Rank.R8, Rank.getOppositeRank(Rank.R1));
		Assert.assertEquals(Rank.R7, Rank.getOppositeRank(Rank.R2));
		Assert.assertEquals(Rank.R6, Rank.getOppositeRank(Rank.R3));
		Assert.assertEquals(Rank.R5, Rank.getOppositeRank(Rank.R4));
		Assert.assertEquals(Rank.R4, Rank.getOppositeRank(Rank.R5));
		Assert.assertEquals(Rank.R3, Rank.getOppositeRank(Rank.R6));
		Assert.assertEquals(Rank.R2, Rank.getOppositeRank(Rank.R7));
		Assert.assertEquals(Rank.R1, Rank.getOppositeRank(Rank.R8));
	}

	@Test
	public void getAbsoluteTest() {
		Assert.assertEquals(Rank.R1, Rank.getAbsolute(Rank.R1, Color.WHITE));
		Assert.assertEquals(Rank.R2, Rank.getAbsolute(Rank.R2, Color.WHITE));
		Assert.assertEquals(Rank.R3, Rank.getAbsolute(Rank.R3, Color.WHITE));
		Assert.assertEquals(Rank.R4, Rank.getAbsolute(Rank.R4, Color.WHITE));
		Assert.assertEquals(Rank.R5, Rank.getAbsolute(Rank.R5, Color.WHITE));
		Assert.assertEquals(Rank.R6, Rank.getAbsolute(Rank.R6, Color.WHITE));
		Assert.assertEquals(Rank.R7, Rank.getAbsolute(Rank.R7, Color.WHITE));
		Assert.assertEquals(Rank.R8, Rank.getAbsolute(Rank.R8, Color.WHITE));

		Assert.assertEquals(Rank.R8, Rank.getAbsolute(Rank.R1, Color.BLACK));
		Assert.assertEquals(Rank.R7, Rank.getAbsolute(Rank.R2, Color.BLACK));
		Assert.assertEquals(Rank.R6, Rank.getAbsolute(Rank.R3, Color.BLACK));
		Assert.assertEquals(Rank.R5, Rank.getAbsolute(Rank.R4, Color.BLACK));
		Assert.assertEquals(Rank.R4, Rank.getAbsolute(Rank.R5, Color.BLACK));
		Assert.assertEquals(Rank.R3, Rank.getAbsolute(Rank.R6, Color.BLACK));
		Assert.assertEquals(Rank.R2, Rank.getAbsolute(Rank.R7, Color.BLACK));
		Assert.assertEquals(Rank.R1, Rank.getAbsolute(Rank.R8, Color.BLACK));
	}

	@Test
	public void getRelativeTest() {
		Assert.assertEquals(Rank.R1, Rank.getRelative(Rank.R1, Color.WHITE));
		Assert.assertEquals(Rank.R2, Rank.getRelative(Rank.R2, Color.WHITE));
		Assert.assertEquals(Rank.R3, Rank.getRelative(Rank.R3, Color.WHITE));
		Assert.assertEquals(Rank.R4, Rank.getRelative(Rank.R4, Color.WHITE));
		Assert.assertEquals(Rank.R5, Rank.getRelative(Rank.R5, Color.WHITE));
		Assert.assertEquals(Rank.R6, Rank.getRelative(Rank.R6, Color.WHITE));
		Assert.assertEquals(Rank.R7, Rank.getRelative(Rank.R7, Color.WHITE));
		Assert.assertEquals(Rank.R8, Rank.getRelative(Rank.R8, Color.WHITE));

		Assert.assertEquals(Rank.R8, Rank.getRelative(Rank.R1, Color.BLACK));
		Assert.assertEquals(Rank.R7, Rank.getRelative(Rank.R2, Color.BLACK));
		Assert.assertEquals(Rank.R6, Rank.getRelative(Rank.R3, Color.BLACK));
		Assert.assertEquals(Rank.R5, Rank.getRelative(Rank.R4, Color.BLACK));
		Assert.assertEquals(Rank.R4, Rank.getRelative(Rank.R5, Color.BLACK));
		Assert.assertEquals(Rank.R3, Rank.getRelative(Rank.R6, Color.BLACK));
		Assert.assertEquals(Rank.R2, Rank.getRelative(Rank.R7, Color.BLACK));
		Assert.assertEquals(Rank.R1, Rank.getRelative(Rank.R8, Color.BLACK));
	}
}
