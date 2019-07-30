package com.quorum.tessera.sync;

import java.net.URI;
import java.util.Optional;
import java.util.function.Predicate;
import javax.websocket.Session;

public interface SessionStore {

    void remove(Session session);

    void store(Session session);

    Optional<Session> find(Predicate<Session> filter);

    Session findByUri(URI uri);
    
    void clear();
    
    static SessionStore create() {
        return SessionStoreImpl.INSTANCE;
    }
    
}
