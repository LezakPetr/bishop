package bishopTests;

import bishop.base.*;
import bishop.engine.RepeatedPositionRegister;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class RepeatedPositionRegisterTest {
	private static final String HEADER = "[Event \"\"]\n" +
			"[Site \"\"]\n" +
			"[Date \"\"]\n" +
			"[Round \"\"]\n" +
			"[White \"\"]\n" +
			"[Black \"\"]\n" +
			"[Result \"*\"]\n";

	private static final String WHOLE_GAME = HEADER +
			"\n" +
			"1. e4 e5 2. Nf3 Nc6 3. Ng1 Nb8 4. Nf3 Nc6 5. Ng1 Nb8 *\n";

	private static final String GAME_PART_1 = HEADER +
			"\n" +
			"1. e4 e5 2. Nf3 Nc6 3. Ng1 *\n";   // Nb8 is omitted because the position is stored as initial position in part 2

	private static final String GAME_PART_2 = HEADER +
			"[FEN \"rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1\"]\n" +
			"\n" +
			"1. Nf3 Nc6 2. Ng1 Nb8 *\n";

	private static final String POSITION = "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1";

	private RepeatedPositionRegister getRegisterForGame(final String gameStr) throws IOException {
		return getRegisterForGame(gameStr, 0);
	}

	private RepeatedPositionRegister getRegisterForGame(final String gameStr, final int fromMove) throws IOException {
		final PgnReader pgnReader = new PgnReader();
		pgnReader.readPgnFromString(gameStr);

		final Game game = pgnReader.getGameList().get(0);
		final RepeatedPositionRegister register = new RepeatedPositionRegister();
		register.clearAndReserve(100);

		final ITreeIterator<IGameNode> it = game.getRootIterator();
		int index = 0;

		while (true) {
			final IGameNode gameNode = it.getItem();
			register.pushPosition(gameNode.getTargetPosition(), gameNode.getMove(), index >= fromMove);

			if (!it.hasChild())
				break;

			it.moveFirstChild();
			index++;
		}

		return register;
	}

	@Test
	public void testPushPop() throws IOException {
		final RepeatedPositionRegister register = getRegisterForGame(WHOLE_GAME);
		final Position position = Fen.positionFromString(POSITION);

		Assert.assertEquals(3, register.getPositionRepeatCount(position));

		register.popPosition();
		Assert.assertEquals(2, register.getPositionRepeatCount(position));
	}

	@Test
	public void testPushAll() throws IOException {
		final RepeatedPositionRegister register1 = getRegisterForGame(GAME_PART_1);
		final RepeatedPositionRegister register2 = getRegisterForGame(GAME_PART_2);
		register1.pushAll(register2);

		final Position position = Fen.positionFromString(POSITION);
		Assert.assertEquals(3, register1.getPositionRepeatCount(position));
	}

	@Test
	public void testPushPopNotStore() throws IOException {
		final RepeatedPositionRegister register = getRegisterForGame(WHOLE_GAME, 3);
		final Position position = Fen.positionFromString(POSITION);

		Assert.assertEquals(2, register.getPositionRepeatCount(position));

		register.popPosition();
		Assert.assertEquals(1, register.getPositionRepeatCount(position));
	}

	@Test
	public void testPushAllNotStore() throws IOException {
		final RepeatedPositionRegister register1 = getRegisterForGame(GAME_PART_1,3);
		final RepeatedPositionRegister register2 = getRegisterForGame(GAME_PART_2, 3);
		register1.pushAll(register2);

		final Position position = Fen.positionFromString(POSITION);
		Assert.assertEquals(1, register1.getPositionRepeatCount(position));
	}

}
