package bishop.controller;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import bishop.base.GlobalSettings;

public class LocalizedComponentRegisterImpl implements ILocalizedComponentRegister {

	private final Set<ILocalizedComponent> componentSet;
	private final ILocalization localization;
	
	public LocalizedComponentRegisterImpl(final ILocalization localization) {
		this.componentSet = new HashSet<ILocalizedComponent>();
		this.localization = localization;
	}
	
	/**
	 * Adds component from the register.
	 * @param component registered component
	 */
	public void addComponent (final ILocalizedComponent component) {
		componentSet.add(component);
		component.updateLanguage(localization);
	}
	
	/**
	 * Removes component from the register.
	 * @param component registered component
	 */
	public void removeComponent (final ILocalizedComponent component) {
		if (!componentSet.remove(component)) {
			if (GlobalSettings.isDebug())
				throw new RuntimeException ("Component '" + component.getClass().getName() + "' is not registered");
		}
	}
	
	/**
	 * Prints registered components into given writer.
	 */
	public void printRegisteredComponents(final PrintStream stream, final String message) {
		if (!componentSet.isEmpty()) {
			stream.println (message);
			
			for (ILocalizedComponent component: componentSet) {
				stream.println (component.getClass().getName());
			}
		}
	}
	
	/**
	 * Updates languages of registered components.
	 */
	public void updateLanguage() {
		for (ILocalizedComponent component: componentSet) {
			component.updateLanguage(localization);
		}
	}
}
