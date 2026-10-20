/*
 * Copyright (C) 2026 CaoBill0522
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jcao.parts;

import android.os.Bundle;
import android.os.SystemProperties;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class JcaoSettingsFragment extends PreferenceFragmentCompat {
    private static final String KEY_SYSTEM_UPDATE = "system_update";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.jcao_settings, rootKey);

        Preference update = findPreference(KEY_SYSTEM_UPDATE);
        if (update != null) {
            update.setSummary(getString(R.string.system_update_summary,
                    SystemProperties.get("ro.custom.version")));
        }
    }
}
