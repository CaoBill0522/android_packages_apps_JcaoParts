/*
 * Copyright (C) 2026 CaoBill0522
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jcao.parts;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class JcaoSettingsFragment extends PreferenceFragmentCompat {
    private static final String KEY_SIM_CATEGORY = "sim_category";
    private static final String KEY_FORCE_ESIM = "force_esim";

    private static final String PROP_ESIM = "persist.sys.jcao.esim";
    private static final String PROP_SKU = "ro.boot.product.hardware.sku";
    // This board has its own eSIM switch under Settings > Network & internet
    private static final String SKU_WITH_EUICC = "ditingp";

    private static final ComponentName ESIM_SWITCH_ACTIVITY = new ComponentName(
            "org.diting.esim", "org.diting.esim.EsimSwitchActivity");

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.jcao_settings, rootKey);

        if (SKU_WITH_EUICC.equals(SystemProperties.get(PROP_SKU))) {
            Preference category = findPreference(KEY_SIM_CATEGORY);
            if (category != null) category.setVisible(false);
            return;
        }
        SwitchPreferenceCompat forceEsim = findPreference(KEY_FORCE_ESIM);
        if (forceEsim != null) {
            forceEsim.setOnPreferenceChangeListener((pref, value) -> {
                startActivity(new Intent()
                        .setComponent(ESIM_SWITCH_ACTIVITY)
                        .putExtra("enabled", (Boolean) value));
                // Applied by DitingEsim after a confirmation and a reboot
                return false;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SwitchPreferenceCompat forceEsim = findPreference(KEY_FORCE_ESIM);
        if (forceEsim != null) {
            forceEsim.setChecked(SystemProperties.getBoolean(PROP_ESIM, false));
        }
    }
}
