/*
 * Copyright (C) 2026 CaoBill0522
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jcao.parts;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.jcao.parts.battery.ChargeUtils;

/** Persistent process: keeps an eye on the battery while bypass power is on. */
public class JcaoApplication extends Application {
    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChargeUtils.onBatteryChanged(context, intent);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
}
