package bishop.gui;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import bishop.controller.ILocalization;

public enum GameSelectorMode {
	
	LOAD("GameSelectorDialog.titleLoad", Collections.emptyList()),
	OVERWRITE(
		"GameSelectorDialog.titleOverwrite",
		Collections.unmodifiableList(Collections.singletonList("GameSelectorDialog.addNewGame"))
	);
	
	private final String titleKey;
	private final List<String> additionalItemKeys;
	
	private GameSelectorMode(final String titleKey, final List<String> additionalItemKeys) {
		this.titleKey = titleKey;
		this.additionalItemKeys = additionalItemKeys;
	}

	public String getTitleKey() {
		return titleKey;
	}

	public List<String> getAdditionalItemKeys() {
		return additionalItemKeys;
	}
	
	public List<String> getAdditionalItems (final ILocalization localization) {
		return additionalItemKeys.stream()
				.map(k -> localization.translateString(k))
				.collect(Collectors.toList());
	}
	
	
}
