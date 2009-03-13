package org.oleg.fcs.client;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class ConnectionException extends RuntimeException {
    public ConnectionException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ConnectionException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ConnectionException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
