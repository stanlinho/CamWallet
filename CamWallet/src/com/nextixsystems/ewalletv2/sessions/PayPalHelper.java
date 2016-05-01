package com.nextixsystems.ewalletv2.sessions;

public class PayPalHelper {
	
	// multiplier for currency conversion
	// this is an estimate. change this when
	// the actual rate for the service is known
	public static final float SERVICE_RATE = 1.035f;
	
	public static float computeDollarCharge(float peso_amount, float dollar_rate){
		return (peso_amount * SERVICE_RATE/dollar_rate);
	}
}
