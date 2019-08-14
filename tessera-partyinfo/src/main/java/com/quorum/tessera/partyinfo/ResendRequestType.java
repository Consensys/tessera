package com.quorum.tessera.partyinfo;

import io.swagger.annotations.ApiModel;

/**
 * The request type for a {@link ResendRequest}
 *
 * <p>ALL specifies to resend all transactions for a given recipient public key
 *
 * <p>INDIVIDUAL specifies to resend a single transaction (hash is provided) if the given public key is a recipient
 */
@ApiModel
public enum ResendRequestType {
    ALL,
    INDIVIDUAL
}
