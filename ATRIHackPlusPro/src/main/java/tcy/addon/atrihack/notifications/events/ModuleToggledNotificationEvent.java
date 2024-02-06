package tcy.addon.atrihack.notifications.events;

import meteordevelopment.meteorclient.systems.modules.Module;
import tcy.addon.atrihack.notifications.NotificationEvent;

public class ModuleToggledNotificationEvent extends NotificationEvent {

	private final Module toggledModule;
	private final boolean newState;

	public ModuleToggledNotificationEvent(Module toggledModule) {
		this.toggledModule = toggledModule;
		this.newState = toggledModule.isActive();
	}

	public Module getToggledModule() {
		return toggledModule;
	}

	public boolean getNewState() {
		return newState;
	}
}
