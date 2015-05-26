package bishop.gui;

import java.util.EnumMap;

import bishop.base.PositionValidator;
import bishop.controller.ILocalization;

public class MessagePositionValidator extends PositionValidator {
	
	private static final EnumMap<Error, String> MESSAGE_MAP = initializeMessageMap();
	
	public String getMessage(final ILocalization localization) {
		final StringBuilder builder = new StringBuilder();
		
		for (Error error: getErrorSet()) {
			if (builder.length() > 0) {
				builder.append("\n");
			}
			
			builder.append(localization.translateString(MESSAGE_MAP.get(error)));
		}
		
		return builder.toString();
	}

	private static EnumMap<Error, String> initializeMessageMap() {
		final EnumMap<Error, String> messageMap = new EnumMap<Error, String>(Error.class);
		
		messageMap.put(Error.KING_COUNT, "MessagePositionValidator.errorKingCount");
		messageMap.put(Error.NOT_ON_TURN_CHECK, "MessagePositionValidator.errorNotOnTurnCheck");
		messageMap.put(Error.CASTLING_RIGHTS, "MessagePositionValidator.errorCastlingRights");
		messageMap.put(Error.PAWNS_ON_RANK_18, "MessagePositionValidator.errorPawnsOnRank18");
		messageMap.put(Error.WRONG_EP_FILE, "MessagePositionValidator.errorWrongEpFile");
		
		return messageMap;
	}

}
