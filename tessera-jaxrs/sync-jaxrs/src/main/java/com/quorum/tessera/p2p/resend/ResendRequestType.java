package com.quorum.tessera.p2p.resend;

/**
 * The request type for a {@link ResendRequest}
 *
 * <p>ALL specifies to resend all transactions for a given recipient public key
 *
 * <p>INDIVIDUAL specifies to resend a single transaction (hash is provided) if the given public key
 * is a recipient
 */
public enum ResendRequestType {
  ALL,
  INDIVIDUAL
}
