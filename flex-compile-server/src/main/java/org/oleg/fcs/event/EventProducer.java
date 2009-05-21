package org.oleg.fcs.event;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public interface EventProducer {

    void addEventListener(EventListener listener);

    void removeEventListener(EventListener eventListener);
    
}
