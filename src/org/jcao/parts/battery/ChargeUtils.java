/*
 * Copyright (C) 2026 CaoBill0522
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jcao.parts.battery;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import org.jcao.parts.R;
import org.jcao.parts.utils.FileUtils;

/**
 * Charging controls backed by the Xiaomi/QTI battery charger driver.
 *
 * Charge protection uses the ADSP "night charging" mode: charging stops at 80%
 * and resumes below 75%. Bypass power sets the fast-charge current limit to 0,
 * so the charger keeps powering the phone while the battery is left alone;
 * writing -1 hands the limit back to the charger firmware.
 */
public final class ChargeUtils {
    private static final String TAG = "JcaoCharge";

    private static final String NIGHT_CHARGING = "/sys/class/qcom-battery/night_charging";
    private static final String FCC = "/sys/class/power_supply/battery/constant_charge_current";
    private static final String FCC_BYPASS = "0";
    private static final String FCC_UNLIMITED = "-1";

    public static final String KEY_CHARGE_PROTECT = "charge_protect";
    public static final String KEY_BYPASS = "bypass_charging";

    /** Bypass turns itself off at or below this level so the phone is never stranded. */
    public static final int BYPASS_MIN_LEVEL = 20;

    private ChargeUtils() {}

    private static SharedPreferences prefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean isChargeProtectSupported() {
        return FileUtils.isFileWritable(NIGHT_CHARGING);
    }

    public static boolean isBypassSupported() {
        return FileUtils.isFileWritable(FCC);
    }

    public static boolean isChargeProtectEnabled(Context context) {
        return prefs(context).getBoolean(KEY_CHARGE_PROTECT, false);
    }

    public static boolean isBypassEnabled(Context context) {
        return prefs(context).getBoolean(KEY_BYPASS, false);
    }

    public static boolean setChargeProtect(Context context, boolean enabled) {
        if (!FileUtils.writeLine(NIGHT_CHARGING, enabled ? "1" : "0")) {
            Log.w(TAG, "Could not write " + NIGHT_CHARGING);
            return false;
        }
        prefs(context).edit().putBoolean(KEY_CHARGE_PROTECT, enabled).apply();
        return true;
    }

    public static boolean setBypass(Context context, boolean enabled) {
        if (!FileUtils.writeLine(FCC, enabled ? FCC_BYPASS : FCC_UNLIMITED)) {
            Log.w(TAG, "Could not write " + FCC);
            return false;
        }
        prefs(context).edit().putBoolean(KEY_BYPASS, enabled).apply();
        return true;
    }

    /** Called on boot: restore charge protection, never carry bypass across a reboot. */
    public static void restore(Context context) {
        if (isChargeProtectEnabled(context)) {
            setChargeProtect(context, true);
        }
        if (isBypassEnabled(context) || FCC_BYPASS.equals(FileUtils.readOneLine(FCC))) {
            setBypass(context, false);
        }
    }

    /** Turns bypass off once the battery falls to {@link #BYPASS_MIN_LEVEL}. */
    public static void onBatteryChanged(Context context, Intent intent) {
        if (!isBypassEnabled(context)) {
            return;
        }
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        if (level < 0 || scale <= 0) {
            return;
        }
        if (level * 100 / scale <= BYPASS_MIN_LEVEL && setBypass(context, false)) {
            Toast.makeText(context, R.string.bypass_auto_off, Toast.LENGTH_LONG).show();
        }
    }
}
