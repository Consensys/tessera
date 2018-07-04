package com.github.nexus.config;

import java.util.Stack;

public enum KeyDataConfigStore {

    INSTANCE;

    private final Stack<KeyDataConfig> stored = new Stack<>();

    public void push(KeyDataConfig config) {
        stored.push(config);
    }

    public KeyDataConfig pop() {
        return stored.pop();
    }

    public boolean isEmpty() {
        return stored.isEmpty();
    }

    public void clear() {
        stored.clear();
    }

}
