/*
 * Copyright (C) 2026 CaoBill0522
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jcao.parts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.jcao.parts.battery.ChargeUtils;
import org.jcao.parts.refreshrate.RefreshUtils;
import org.jcao.parts.thermal.ThermalUtils;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        ChargeUtils.restore(context);
        RefreshUtils.initialize(context);
        ThermalUtils.startService(context);
    }
}
