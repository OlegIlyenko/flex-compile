package org.oleg.fcs.compiler.listener;

import org.oleg.fcs.event.EventListener;
import org.oleg.fcs.event.Event;
import org.oleg.fcs.compiler.CompilerEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class LogListener implements EventListener {

    private final Log log = LogFactory.getLog("Compilation messages");
    private final Log reportsLog = LogFactory.getLog("reports");

    public void onEvent(Event e) {
        if (e instanceof CompilerEvent && ((CompilerEvent) e).getEventType() != CompilerEvent.EventType.Process) {
            log.info(e.toString());

            CompilerEvent ce = (CompilerEvent) e;
            if (ce.getEventType() != CompilerEvent.EventType.Success || ce.getEventType() != CompilerEvent.EventType.Errors) {
                reportsLog.info(ce.toString());
            }
        }
    }
}
