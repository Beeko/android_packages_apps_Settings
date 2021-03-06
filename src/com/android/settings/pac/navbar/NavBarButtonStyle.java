/*
 * Copyright (C) 2012-2015 Slimroms
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

package com.android.settings.pac.navbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class NavBarButtonStyle extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "NavBarButtonStyle";
    private static final String PREF_NAV_BUTTON_COLOR = "nav_button_color";
    private static final String PREF_NAV_BUTTON_COLOR_MODE = "nav_button_color_mode";
    private static final String PREF_NAV_GLOW_COLOR = "nav_button_glow_color";

    private static final int MENU_RESET = Menu.FIRST;

    private boolean mCheckPreferences;

    ColorPickerPreference mNavigationBarButtonColor;
    ListPreference mNavigationBarButtonColorMode;
    ColorPickerPreference mNavigationBarGlowColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshSettings();
    }

    private PreferenceScreen refreshSettings() {
        mCheckPreferences = false;
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.navbar_button_style);

        prefs = getPreferenceScreen();

        mNavigationBarButtonColor = (ColorPickerPreference) findPreference(PREF_NAV_BUTTON_COLOR);
        mNavigationBarButtonColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.PAC.getInt(getContentResolver(),
                    Settings.PAC.NAVIGATION_BAR_BUTTON_TINT, -2);
        if (intColor == -2) {
            intColor = getResources().getColor(
                    com.android.internal.R.color.white);
            mNavigationBarButtonColor.setSummary(getResources().getString(R.string.default_string));
        } else {
            String hexColor = String.format("#%08x", (0xffffffff & intColor));
            mNavigationBarButtonColor.setSummary(hexColor);
        }
        mNavigationBarButtonColor.setNewPreviewColor(intColor);

        mNavigationBarGlowColor = (ColorPickerPreference) findPreference(PREF_NAV_GLOW_COLOR);
        mNavigationBarGlowColor.setOnPreferenceChangeListener(this);
        intColor = Settings.PAC.getInt(getContentResolver(),
                    Settings.PAC.NAVIGATION_BAR_GLOW_TINT, -2);
        if (intColor == -2) {
            intColor = getResources().getColor(
                    com.android.internal.R.color.white);
            mNavigationBarGlowColor.setSummary(getResources().getString(R.string.default_string));
        } else {
            String hexColor = String.format("#%08x", (0xffffffff & intColor));
            mNavigationBarGlowColor.setSummary(hexColor);
        }
        mNavigationBarGlowColor.setNewPreviewColor(intColor);

        mNavigationBarButtonColorMode =
            (ListPreference) prefs.findPreference(PREF_NAV_BUTTON_COLOR_MODE);
        int navigationBarButtonColorMode = Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.NAVIGATION_BAR_BUTTON_TINT_MODE, 0);
        mNavigationBarButtonColorMode.setValue(String.valueOf(navigationBarButtonColorMode));
        mNavigationBarButtonColorMode.setSummary(mNavigationBarButtonColorMode.getEntry());
        mNavigationBarButtonColorMode.setOnPreferenceChangeListener(this);

        updateColorPreference();

        setHasOptionsMenu(true);
        mCheckPreferences = true;
        return prefs;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.profile_reset_title)
                .setIcon(com.android.internal.R.drawable.ic_menu_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.profile_reset_title);
        alertDialog.setMessage(R.string.navbar_button_style_reset_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Settings.PAC.putInt(getActivity().getContentResolver(),
                        Settings.PAC.NAVIGATION_BAR_BUTTON_TINT, -2);
                Settings.PAC.putInt(getActivity().getContentResolver(),
                       Settings.PAC.NAVIGATION_BAR_BUTTON_TINT_MODE, 0);
                Settings.PAC.putInt(getActivity().getContentResolver(),
                        Settings.PAC.NAVIGATION_BAR_GLOW_TINT, -2);
                refreshSettings();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!mCheckPreferences) {
            return false;
        }
        if (preference == mNavigationBarButtonColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.PAC.putInt(getActivity().getContentResolver(),
                    Settings.PAC.NAVIGATION_BAR_BUTTON_TINT, intHex);
            return true;
        } else if (preference == mNavigationBarButtonColorMode) {
            int index = mNavigationBarButtonColorMode.findIndexOfValue((String) newValue);
            int value = Integer.valueOf((String) newValue);
            Settings.PAC.putInt(getContentResolver(),
                    Settings.PAC.NAVIGATION_BAR_BUTTON_TINT_MODE,
                    value);
            mNavigationBarButtonColorMode.setSummary(
                mNavigationBarButtonColorMode.getEntries()[index]);
            updateColorPreference();
            return true;
        } if (preference == mNavigationBarGlowColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.PAC.putInt(getActivity().getContentResolver(),
                    Settings.PAC.NAVIGATION_BAR_GLOW_TINT, intHex);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateColorPreference() {
        int navigationBarButtonColorMode = Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.NAVIGATION_BAR_BUTTON_TINT_MODE, 0);
        mNavigationBarButtonColor.setEnabled(navigationBarButtonColorMode != 3);
    }
}
