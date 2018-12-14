package com.itachi1706.cheesecakeutilities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.itachi1706.appupdater.EasterEggResMusicPrefFragment;
import com.itachi1706.appupdater.SettingsInitializer;
import com.itachi1706.cheesecakeutilities.Features.FingerprintAuth.AuthenticationActivity;
import com.itachi1706.cheesecakeutilities.Features.FingerprintAuth.BiometricCompatHelper;
import com.itachi1706.cheesecakeutilities.Features.UtilityManagement.ManageUtilityActivity;
import com.itachi1706.cheesecakeutilities.Util.CommonVariables;

import androidx.appcompat.app.AppCompatActivity;
import de.psdev.licensesdialog.LicensesDialog;


public class GeneralSettingsActivity extends AppCompatActivity {
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @SuppressWarnings("ConstantConditions")
    public static class GeneralPreferenceFragment extends EasterEggResMusicPrefFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            new SettingsInitializer(getActivity(), R.drawable.notification_icon,
                    CommonVariables.BASE_SERVER_URL, getResources().getString(R.string.link_legacy),
                    getResources().getString(R.string.link_updates), true)
                    .explodeUpdaterSettings(this);
            super.addEggMethods(true, preference -> {
                new LicensesDialog.Builder(getActivity()).setNotices(R.raw.notices)
                        .setIncludeOwnLicense(true).build().show();
                return true;
            });

            // Authentication processing
            final Preference fp_pw = findPreference("password_fp");

            sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            updatePasswordViews(fp_pw);

            findPreference("testpw").setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), AuthenticationActivity.class));
                return false;
            });

            findPreference("hide_util").setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), ManageUtilityActivity.class));
                return false;
            });

            findPreference("util_settings").setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), UtilitySettingsActivity.class));
                return false;
            });

            findPreference("app_bio_compat_enable").setOnPreferenceChangeListener((preference, newValue) -> {
                updatePasswordViews(fp_pw, (boolean) newValue);
                return true;
            });
        }

        SharedPreferences sp;

        private void updatePasswordViews(Preference fp_pw) {
            updatePasswordViews(fp_pw, BiometricCompatHelper.requireFPAuth(sp));
        }

        private void updatePasswordViews(Preference fp_pw, boolean val) {
            if (val) {
                if (BiometricCompatHelper.isBiometricFPRegistered(getActivity()))
                    fp_pw.setSummary("Protected with fingerprint");
                else
                    fp_pw.setSummary("Unprotected (No fingerprint found on device)");
            } else
                fp_pw.setSummary("Unprotected");
        }

        @Override
        public int getMusicResource() {
            return R.raw.hello;
        }

        @Override
        public String getStartEggMessage() {
            return "Hello!";
        }

        @Override
        public String getEndEggMessage() {
            return "Music Stopped";
        }

        @Override
        public String getStopEggButtonText() {
            return "SILENCE";
        }
    }
}
