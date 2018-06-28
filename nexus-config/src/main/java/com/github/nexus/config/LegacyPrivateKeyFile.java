package com.github.nexus.config;

//{"data":{"bytes":"Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA="},"type":"unlocked"}

//{"data":{"aopts":{"variant":"id","memory":1048576,"iterations":10,"parallelism":4,"version":"1.3"},"snonce":"x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC","asalt":"7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=","sbox":"d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc"},"type":"argon2sbox"}
public class LegacyPrivateKeyFile {
    
    private final String bytes;
    
    private final PrivateKeyType type;

    private final String snonce;

    private final String asalt;

    private final String sbox;

    private final ArgonOptions argonOptions;

    public LegacyPrivateKeyFile(final String bytes,
                                final PrivateKeyType type,
                                final ArgonOptions argonOptions,
                                final String snonce,
                                final String asalt,
                                final String sbox) {
        this.bytes = bytes;
        this.type = type;
        this.argonOptions = argonOptions;
        this.snonce = snonce;
        this.asalt = asalt;
        this.sbox = sbox;
    }

    public String getBytes() {
        return bytes;
    }

    public PrivateKeyType getType() {
        return type;
    }

    public ArgonOptions getArgonOptions() {
        return argonOptions;
    }

    public String getSnonce() {
        return snonce;
    }

    public String getAsalt() {
        return asalt;
    }

    public String getSbox() {
        return sbox;
    }
}
