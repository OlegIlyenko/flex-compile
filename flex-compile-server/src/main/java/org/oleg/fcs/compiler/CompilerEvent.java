package org.oleg.fcs.compiler;

import org.oleg.fcs.event.Event;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class CompilerEvent extends Event {

    public enum EventType {Initialized, Started, Compilation, Errors, Success, Process, ProjectStart, ProjectFinishSuccess, ProjectFinishErrors}

    private EventType eventType;

    private String mainArtifact;

    private int compilationProcess;

    private long compilationTime;

    private String compilationProtocol;

    public CompilerEvent() {
        super();
    }

    public CompilerEvent(Object source, String message, EventType eventType, String mainArtifact) {
        super(source, message);
        this.eventType = eventType;
        this.mainArtifact = mainArtifact;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getMainArtifact() {
        return mainArtifact;
    }

    public void setMainArtifact(String mainArtifact) {
        this.mainArtifact = mainArtifact;
    }

    public int getCompilationProcess() {
        return compilationProcess;
    }

    public void setCompilationProcess(int compilationProcess) {
        this.compilationProcess = compilationProcess;
    }

    public long getCompilationTime() {
        return compilationTime;
    }

    public void setCompilationTime(long compilationTime) {
        this.compilationTime = compilationTime;
    }

    public String getCompilationProtocol() {
        return compilationProtocol;
    }

    public void setCompilationProtocol(String compilationProtocol) {
        this.compilationProtocol = compilationProtocol;
    }

    public String toString() {
        String s = "CompilerEvent [" + eventType + ", artifact=" + mainArtifact + ", compilationTime=" + compilationTime + " ms] Message: " + getMessage();
        if (compilationProtocol != null) {
            s += ", Protocol: \n" + compilationProtocol;
        }

        return s;
    }
}
