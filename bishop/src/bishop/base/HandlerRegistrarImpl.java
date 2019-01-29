package bishop.base;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class HandlerRegistrarImpl<H> implements IHandlerRegistrar<H> {
	
	private final Set<H> handlerSet;
	private boolean changesEnabled;
	
	public HandlerRegistrarImpl() {
		handlerSet = new HashSet<>();
		changesEnabled = true;
	}
	
	private void checkChangesEnabled() {
		if (!changesEnabled)
			throw new RuntimeException("Changes of this registrar are not enabled");
	}

	/**
	 * Adds handler to this registrar.
	 * @param handler handler
	 */
	public synchronized void addHandler (final H handler) {
		checkChangesEnabled();
		
		if (!handlerSet.add(handler))
			throw new RuntimeException("Given handler is already registered to this registrar");
	}

	/**
	 * Removes handler from this registrar.
	 * @param handler handler
	 */
	public synchronized void removeHandler (final H handler) {
		checkChangesEnabled();

		if (!handlerSet.remove(handler))
			throw new RuntimeException("Given handler is not registered to this registrar");		
	}
	
	/**
	 * Checks if given handler is registered to this registrar.
	 * @param handler handler
	 * @return true is handler is registered, false if not
	 */
	public synchronized boolean isHandlerRegistered (final H handler) {
		return handlerSet.contains(handler);
	}
	
	public synchronized void setChangesEnabled (final boolean changesEnabled) {
		this.changesEnabled = changesEnabled;
	}
	
	public synchronized Collection<H> getHandlers() {
		return handlerSet;
	}

}
