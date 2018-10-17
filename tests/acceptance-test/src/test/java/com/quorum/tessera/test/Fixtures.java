package com.quorum.tessera.test;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;

public class Fixtures {

    private static final String SCHEME_AND_HOST = "http://127.0.0.1";

    public static final URI NODE1_URI = UriBuilder.fromUri(SCHEME_AND_HOST).port(8080).build();

    public static final URI NODE3_URI = UriBuilder.fromUri(SCHEME_AND_HOST).port(8081).build();
    
    public static final URI NODE2_URI = UriBuilder.fromUri(SCHEME_AND_HOST).port(8082).build();

    public static final byte[] TXN_DATA = "Zm9v".getBytes();

    public static final String SENDER_KEY = "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=";

    public static final String RECIPIENT_ONE = "yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=";

    public static final String RECIPIENT_TWO = "giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=";
    
    public static final String RECIPIENT_THREE = "jP4f+k/IbJvGyh0LklWoea2jQfmLwV53m9XoHVS4NSU=";
}
