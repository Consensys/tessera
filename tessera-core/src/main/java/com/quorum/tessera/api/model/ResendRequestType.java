package com.quorum.tessera.api.model;

import io.swagger.annotations.ApiModel;

/**
 * The request type for a {@link ResendRequest}
 *
 * ALL specifies to resend all transactions for a given recipient public key
 *
 * INDIVIDUAL specifies to resend a single transaction (hash is provided) is
 * the given public key is a recipient
 */
@ApiModel
public enum ResendRequestType {
    ALL, INDIVIDUAL
}
