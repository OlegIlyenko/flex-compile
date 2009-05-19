package org.oleg.fcs.project.dao;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class ProjectException extends RuntimeException {
    public ProjectException() {
        super();
    }

    public ProjectException(String message) {
        super(message);
    }

    public ProjectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProjectException(Throwable cause) {
        super(cause);
    }
}
