package org.oleg.fcs.project.model;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class Artefact {

    public static enum Type {module, application}

    private String fileName;

    private Type type;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Artefact(" + type + ") '" + fileName + "'";
    }
}
