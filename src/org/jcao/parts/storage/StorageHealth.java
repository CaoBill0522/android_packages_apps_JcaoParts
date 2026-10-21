/*
 * Copyright (C) 2026 CaoBill0522
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jcao.parts.storage;

import android.content.Context;
import android.text.TextUtils;

import org.jcao.parts.R;
import org.jcao.parts.utils.FileUtils;

import java.io.File;

/** Reads the UFS health descriptor (JEDEC bDeviceLifeTimeEstA/B and bPreEOLInfo). */
public final class StorageHealth {
    private static final String SOC = "/sys/devices/platform/soc";
    private static final String HEALTH = "health_descriptor";
    // Labelled sysfs_ufs_health by the device sepolicy; other hosts may not be readable
    private static final String DITING_UFS_HOST = "1d84000.ufshc";

    private StorageHealth() {}

    private static File findUfsHost() {
        File diting = new File(SOC, DITING_UFS_HOST);
        if (new File(diting, HEALTH).isDirectory()) {
            return diting;
        }
        File[] hosts = new File(SOC).listFiles(
                (dir, name) -> name.endsWith(".ufshc") && new File(dir, name + "/" + HEALTH).isDirectory());
        return hosts == null || hosts.length == 0 ? null : hosts[0];
    }

    private static int readHex(File host, String node) {
        String value = FileUtils.readOneLine(new File(host, HEALTH + "/" + node).getPath());
        if (TextUtils.isEmpty(value)) {
            return -1;
        }
        try {
            return Integer.decode(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Human readable summary, or null when the descriptor is unavailable. */
    public static String getSummary(Context context) {
        File host = findUfsHost();
        if (host == null) {
            return null;
        }
        // 0x01..0x0A: 0-10% .. 90-100% of the rated life used, 0x0B: exceeded.
        int life = Math.max(readHex(host, "life_time_estimation_a"),
                readHex(host, "life_time_estimation_b"));
        int eol = readHex(host, "eol_info");
        if (life <= 0) {
            return null;
        }

        String used;
        if (life >= 0x0B) {
            used = context.getString(R.string.storage_life_exceeded);
        } else {
            used = context.getString(R.string.storage_life_used, (life - 1) * 10, life * 10);
        }

        int state;
        switch (eol) {
            case 0x02:
                state = R.string.storage_eol_warning;
                break;
            case 0x03:
                state = R.string.storage_eol_urgent;
                break;
            default:
                state = R.string.storage_eol_normal;
                break;
        }
        return context.getString(R.string.storage_life_summary, used, context.getString(state));
    }
}
