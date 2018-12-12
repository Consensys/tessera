package com.quorum.tessera.config.util;

//Provide a mockable wrapper for environment variable retrieval
public class EnvironmentVariableProvider {

    public String getEnv(String name) {
        return System.getenv(name);
    }

    public char[] getEnvAsCharArray(String name) {
        return System.getenv(name).toCharArray();
    }

}
