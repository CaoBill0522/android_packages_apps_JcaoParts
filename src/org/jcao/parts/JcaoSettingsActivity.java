/*
 * Copyright (C) 2026 CaoBill0522
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jcao.parts;

import android.os.Bundle;

import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;

public class JcaoSettingsActivity extends CollapsingToolbarBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(com.android.settingslib.collapsingtoolbar.R.id.content_frame,
                            new JcaoSettingsFragment())
                    .commit();
        }
    }
}
