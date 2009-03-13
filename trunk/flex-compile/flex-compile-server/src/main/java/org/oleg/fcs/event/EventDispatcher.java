package org.oleg.fcs.event;

import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class EventDispatcher extends Thread implements EventProducer {

    private Queue<Event> eventQueue = new LinkedList<Event>();

    private List<EventListener> eventListeners = new ArrayList<EventListener>();

    public void run() {
        while (true) {
            List<Event> eventsToProcess = new ArrayList<Event>();
            synchronized (eventQueue) {
                while (eventQueue.isEmpty()) {
                    try {
                        eventQueue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }

                for (Event e : eventQueue) {
                    eventsToProcess.add(e);
                }
                eventQueue.clear();
            }

            for (Event e : eventsToProcess) {
                synchronized (eventListeners) {
                    for (EventListener listener : eventListeners) {
                        listener.onEvent(e);
                    }
                }
            }
        }
    }

    public void addEventListener(EventListener listener) {
        synchronized (eventListeners) {
            eventListeners.add(listener);
        }
    }

    public synchronized void removeEventListener(EventListener eventListener) {
        synchronized (eventListener) {
            eventListeners.remove(eventListener);
        }
    }

    public Queue<Event> getEventQueue() {
        return eventQueue;
    }
}
