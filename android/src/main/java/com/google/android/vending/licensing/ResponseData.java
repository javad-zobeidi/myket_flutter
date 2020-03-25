/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.vending.licensing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * ResponseData from licensing server.
 */
public class ResponseData {

    public int responseCode;
    public int nonce;
    public String packageName;
    public String versionCode;
    public String userId;
    public long timestamp;
    /** Response-specific data. */
    public Map<String, String> extras;

    /**
     * Parses response string into ResponseData.
     *
     * @param responseData response data string
     * @throws IllegalArgumentException upon parsing error
     * @return ResponseData object
     */
    public static ResponseData parse(String responseData) {
        // Must parse out main response data and response-specific data.
    	int index = responseData.indexOf(':');
    	String mainData, extraData;
    	if ( -1 == index ) {
    		mainData = responseData;
    		extraData = "";
    	} else {
    		mainData = responseData.substring(0, index);
    		extraData = index >= responseData.length() ? "" : responseData.substring(index+1);
    	}

        String [] fields = TextUtils.split(mainData, Pattern.quote("|"));
        if (fields.length < 6) {
            throw new IllegalArgumentException("Wrong number of fields.");
        }

        ResponseData data = new ResponseData();
        data.responseCode = Integer.parseInt(fields[0]);
        data.nonce = Integer.parseInt(fields[1]);
        data.packageName = fields[2];
        data.versionCode = fields[3];
        // Application-specific user identifier.
        data.userId = fields[4];
        data.timestamp = Long.parseLong(fields[5]);

        data.extras = decodeExtras(extraData);

        return data;
    }

    private static Map<String, String> decodeExtras(String extras) {
        Map<String, String> results = new HashMap<>();
        URI rawExtras;
        try {
            rawExtras = new URI("?" + extras);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid syntax error while decoding extras data from " +
                    "server.");
        }

        List<NameValuePair> extraList = URLEncodedUtils.parse(rawExtras, "UTF-8");
        for (NameValuePair item : extraList) {
            String name = item.getName();
            int i = 0;
            while (results.containsKey(name)) {
                name = item.getName() + ++i;
            }
            results.put(name, item.getValue());
        }
        return results;
    }

    private ResponseData() {
    }

    @Override
    public String toString() {
        return TextUtils.join("|", new Object [] { responseCode, nonce, packageName, versionCode,
            userId, timestamp });
    }
}
