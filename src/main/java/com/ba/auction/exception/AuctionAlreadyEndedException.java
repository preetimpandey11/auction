package com.ba.auction.exception;

public class AuctionAlreadyEndedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8938442431310366515L;

	public AuctionAlreadyEndedException(String message) {
		super(message);
	}

}
