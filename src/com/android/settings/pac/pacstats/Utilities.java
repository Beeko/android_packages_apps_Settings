/*
 /*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.android.settings.pac.pacstats;

import java.math.BigInteger;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Locale;

import android.content.Context;
import android.telephony.TelephonyManager;

public class Utilities {
    public static final String SETTINGS_PREF_NAME = "PACStats";
    public static final String TAG = "PACStats";

    // For the Unique ID, I still use the IMEI or WiFi MAC address
    // CyanogenMod switched to use the Settings.Secure.ANDROID_ID
    // This is because the ANDROID_ID could change on hard reset, while IMEI remains equal
    public static String getUniqueID(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

        String device_id = digest(tm.getDeviceId());
        if (device_id == null) {
            String wifiInterface = LocalTools.getProp("wifi.interface");
            try {
                String wifiMac = new String(NetworkInterface.getByName(wifiInterface).getHardwareAddress());
                device_id = digest(wifiMac);
            } catch (Exception e) {
                device_id = null;
            }
        }

        return device_id;
    }

    public static String getStatsUrl() {
        String returnUrl = LocalTools.getProp("ro.pacstats.url");

        if (returnUrl.isEmpty()) {
            return null;
        }

        // if the last char of the link is not /, add it
        if (!returnUrl.substring(returnUrl.length() - 1).equals("/")) {
            returnUrl += "/";
        }

        return returnUrl;
    }

    public static String getCarrier(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String carrier = tm.getNetworkOperatorName();
        if ("".equals(carrier)) {
            carrier = "Unknown";
        }
        return carrier;
    }

    public static String getCarrierId(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierId = tm.getNetworkOperator();
        if ("".equals(carrierId)) {
            carrierId = "0";
        }
        return carrierId;
    }

    public static String getCountryCode(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getNetworkCountryIso();
        if (countryCode.equals("")) {
            countryCode = "Unknown";
        }
        return countryCode;
    }

    public static String getDevice() {
        return LocalTools.getProp("ro.product.model");
    }

    public static String getModVersion() {
        return LocalTools.getProp("ro.build.display.id");
    }

    public static String getRomName() {
        return LocalTools.getProp("ro.pacstats.name");
    }

    public static String getRomVersion() {
        return LocalTools.getProp("ro.pacstats.version");
    }

    public static long getTimeFrame() {
        String tFrameStr = LocalTools.getProp("ro.pacstats.tframe");
        return Long.valueOf(tFrameStr);
    }

    public static String digest(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return new BigInteger(1, md.digest(input.getBytes())).toString(16).toUpperCase(Locale.US);
        } catch (Exception e) {
            return null;
        }
    }
}