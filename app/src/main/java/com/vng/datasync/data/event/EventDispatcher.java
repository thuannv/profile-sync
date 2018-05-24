package com.vng.datasync.data.event;

import android.util.SparseArray;

import java.util.HashSet;
import java.util.Set;

/**
 * @author thuannv
 * @since 17/07/2017
 */
public class EventDispatcher {

    private static volatile EventDispatcher sInstance = null;

    private final SparseArray<Set<EventListener>> mObservers = new SparseArray<>();

    private final Object mSync = new Object();

    public static EventDispatcher getInstance() {
        EventDispatcher localInstance = sInstance;
        if (localInstance == null) {
            synchronized (EventDispatcher.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = localInstance = new EventDispatcher();
                }
            }
        }
        return localInstance;
    }

    public void addListener(int eventId, EventListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (mSync) {
            Set<EventListener> listeners = mObservers.get(eventId);
            if (listeners == null) {
                listeners = new HashSet<>();
                mObservers.put(eventId, listeners);
            }
            listeners.add(listener);
        }
    }

    public void removeListener(int eventId, EventListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (mSync) {
            Set<EventListener> listeners = mObservers.get(eventId);
            if (listeners != null && !listeners.isEmpty()) {
                listeners.remove(listener);
            }
        }
    }

    public void post(int eventId, Object... args) {
        Set<EventListener> listeners = null;
        synchronized (mSync) {
            listeners = mObservers.get(eventId);
        }
        if (listeners != null && !listeners.isEmpty()) {
            for (EventListener listener : listeners) {
                listener.onEvent(eventId, args);
            }
        }
    }
}
