
package com.quorum.tessera.sync;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import javax.websocket.Session;

public enum SessionStoreImpl implements SessionStore {
    
    INSTANCE;
    
    private final Map<String,Session> sessions = new HashMap<>();
    
    @Override
    public void remove(Session session) {
        synchronized(sessions) {
            sessions.remove(session.getId());
        }
    }
    
    @Override
    public void store(Session session) {
        synchronized(sessions) {
            sessions.put(session.getId(),session);
        }
    }
    
    
    @Override
    public Optional<Session> find(Predicate<Session> filter) {
        return sessions.values().stream()
                .filter(filter)
                .findAny();
    }

    @Override
    public void clear() {
        synchronized(sessions) {
            sessions.clear();
        }
    }

    @Override
    public Session findByUri(URI uri) {
        return find(s -> s.getRequestURI().equals(uri)).get();
    }

 
}
