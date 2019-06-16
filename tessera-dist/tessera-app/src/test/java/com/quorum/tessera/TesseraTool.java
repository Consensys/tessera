package com.quorum.tessera;

import org.bouncycastle.util.encoders.Hex;

import java.util.Base64;

/**
 *
 * This tool takes in a transaction hash and produces the cURL call needed to
 * retrieve that transaction via Quorum.
 *
 * It converts the Base64 hash to its Hex format, and then formats a cmdline
 * call to be pasted.
 *
 */
public class TesseraTool {

    private static final String VERSION = "\"jsonrpc\":\"2.0\"";

    private static final String METHOD = "\"method\":\"eth_getQuorumPayload\"";

    private static final String ID = "\"id\":67";

    private static final String TEMPLATE = "curl -X POST %s --data '{%s, %s, \"params\":[\"0x%s\"], %s}'";

    public static void main(String[] args) {

        //put the transaction hash here
        final String inputHash = "whA0hI7i3RC60lqlwJacz8qJ6pkDU9JqSCVRs8CTGr/pps9b8bBbk7OWUgq05tZ6PbGsjLTh9z5e+ccRjg+nLw==";
        final String url = "http://127.0.0.1:22000";

        final byte[] decodedBase64 = Base64.getDecoder().decode(inputHash);
        final String hexString = Hex.toHexString(decodedBase64);

        System.err.println(hexString);

        final String curlCall = String.format(TEMPLATE, url, VERSION, METHOD, hexString, ID);
        System.err.println(curlCall);

    }

}
