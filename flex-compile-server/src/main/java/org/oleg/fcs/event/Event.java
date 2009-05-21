package org.oleg.fcs.event;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class Event {

    private Object source;

    private String message;

    public Event() {
    }

    public Event(Object source, String message) {
        this.source = source;
        this.message = message;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
