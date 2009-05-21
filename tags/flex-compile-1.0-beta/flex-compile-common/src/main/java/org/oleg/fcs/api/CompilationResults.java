package org.oleg.fcs.api;

import java.io.Serializable;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class CompilationResults implements Serializable {

    private String projectName;

    private String componentName;

    private String artefactName;

    private boolean hasErrors;

    private boolean hasChanges;

    private String compileProtocol;

    public CompilationResults(String compileProtocol, boolean hasChanges, boolean hasErrors, String projectName, String componentName) {
        this.hasErrors = hasErrors;
        this.compileProtocol = compileProtocol;
        this.hasChanges = hasChanges;
        this.projectName = projectName;
        this.componentName = componentName;
    }

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public String getCompileProtocol() {
        return compileProtocol;
    }

    public void setCompileProtocol(String compileProtocol) {
        this.compileProtocol = compileProtocol;
    }

    public boolean isHasChanges() {
        return hasChanges;
    }

    public void setHasChanges(boolean hasChanges) {
        this.hasChanges = hasChanges;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getArtefactName() {
        return artefactName;
    }

    public void setArtefactName(String artefactName) {
        this.artefactName = artefactName;
    }

    public String toString() {
        return "Compilation Results (Project=" + projectName + ", Component=" + componentName + ", Artifact=" + artefactName + ", Changes=" + hasChanges + ", Errors=" + hasErrors + "): " + compileProtocol;
    }
}
