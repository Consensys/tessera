package com.github.nexus.service;

public interface TransactionService {

    byte[] send();

    void receive();

    void delete();

    void resend();

    void push();

}
