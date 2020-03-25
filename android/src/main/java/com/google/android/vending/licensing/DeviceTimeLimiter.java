package com.google.android.vending.licensing;

import android.util.Log;

public class DeviceTimeLimiter implements DeviceLimiter{
	private static final String TAG = "DeviceTimeLimiter";

	@Override
	public int isDeviceAllowed(String userId, ResponseData data) {
		try {
			long validityTimestamp = Long.parseLong(data.extras.get("VT"));
			long currentServerTimestamp = data.timestamp;
			if (validityTimestamp < currentServerTimestamp) {
				return Policy.RETRY;
			}
			// The required time accuracy is 95%
			long errorRange = (long) ((validityTimestamp - currentServerTimestamp) * 5d / 100d);
			long ts = System.currentTimeMillis();
			if (ts < currentServerTimestamp - errorRange || currentServerTimestamp + errorRange < ts) {
				Log.d(TAG, "current server timestamp  : " + currentServerTimestamp);
				Log.d(TAG, "currentTimeMillis: " + ts);
				Log.d(TAG, "errorRange: " + errorRange);
				Log.d(TAG, "Cannot accept the license because device's time is not correct!");
				return Policy.RETRY;
			}
		} catch (NumberFormatException e) {
			// No response or not parsable or VT is empty.
		}
		return Policy.LICENSED;
	}
}
