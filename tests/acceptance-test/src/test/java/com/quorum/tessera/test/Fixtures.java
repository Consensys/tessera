package com.quorum.tessera.test;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;

public class Fixtures {

    private static final String SCHEME_AND_HOST = "http://127.0.0.1";

    public static final URI NODE1_Q2T_URI = UriBuilder.fromUri(SCHEME_AND_HOST).port(18080).build();
    public static final URI NODE1_P2P_URI = UriBuilder.fromUri(SCHEME_AND_HOST).port(8080).build();

    public static final URI NODE2_Q2T_URI = UriBuilder.fromUri(SCHEME_AND_HOST).port(18081).build();
    public static final URI NODE2_P2P_URI = UriBuilder.fromUri(SCHEME_AND_HOST).port(8081).build();

    public static final URI NODE3_Q2T_URI = UriBuilder.fromUri(SCHEME_AND_HOST).port(18082).build();
    public static final URI NODE3_P2P_URI = UriBuilder.fromUri(SCHEME_AND_HOST).port(8082).build();

    public static final byte[] TXN_DATA = "Zm9v".getBytes();

    public static final String PTY1_KEY = "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=";

    public static final String PTY2_KEY = "yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=";

    public static final String PTY3_KEY = "giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=";
    
    public static final String PTY4_KEY = "jP4f+k/IbJvGyh0LklWoea2jQfmLwV53m9XoHVS4NSU=";
}
