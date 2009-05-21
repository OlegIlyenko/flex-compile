package org.oleg.fcs.api;

import java.io.Serializable;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class UnexpectedExceptionValue implements Serializable {

    private Exception cause;

    public UnexpectedExceptionValue(Exception cause) {
        this.cause = cause;
    }

    public Exception getCause() {
        return cause;
    }

    public void setCause(Exception cause) {
        this.cause = cause;
    }
}