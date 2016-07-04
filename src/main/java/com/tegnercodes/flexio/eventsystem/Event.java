package com.tegnercodes.flexio.eventsystem;

public abstract class Event {

    /**
     * Get event type name.
     *
     * @return event name
     */
    protected String getEventName() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getEventName() + " (" + this.getClass().getName() + ")";
    }


}
