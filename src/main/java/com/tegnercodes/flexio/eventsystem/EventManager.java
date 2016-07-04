package com.tegnercodes.flexio.eventsystem;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EventManager {
	private static List<Listener> registered = new ArrayList<Listener>();
	public static EventManager instance = null;

	public EventManager() {
		instance = this;
	}

	public void registerEvents(Listener listener) {
		if (!registered.contains(listener)) {
			registered.add(listener);
		}
	}

	public void unregister(Listener listener) {
		if (registered.contains(listener)) {
			registered.remove(listener);
		}
	}

	public List<Listener> getRegistered() {
		return registered;
	}

	public void callEvent(final Event event) {
				call(event);
	}

	private static void call(final Event event) {
		for (Listener listener : registered) {
			Method[] methods = listener.getClass().getMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(EventHandler.class)) {
					try {
						method.invoke(listener, event);
					} catch (Exception exception) {
						exception.printStackTrace();
					}
				}
			}
		}
	}


}
