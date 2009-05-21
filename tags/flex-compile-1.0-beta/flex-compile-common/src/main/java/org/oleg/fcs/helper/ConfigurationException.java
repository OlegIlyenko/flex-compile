package org.oleg.fcs.helper;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ConfigurationException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ConfigurationException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
