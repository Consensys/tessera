
package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;

/**
 * Use polymorphic inheritance to allow transaction manager impl to 
 * persist transactions during replay mode. 
 * 
 * //TODO: We should create and implement ResendStoreDelegate and compose object
 * rather than using polymorphism
 */
public interface ResendStoreDelegate {
    MessageHash storePayloadBypass(byte[] payload);
}
