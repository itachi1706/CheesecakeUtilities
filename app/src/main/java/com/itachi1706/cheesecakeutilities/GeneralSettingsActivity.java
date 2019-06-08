package com.itachi1706.cheesecakeutilities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;

import androidx.appcompat.app.AppCompatActivity;

import com.itachi1706.appupdater.EasterEggResMusicPrefFragment;
import com.itachi1706.appupdater.SettingsInitializer;
import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.Features.FingerprintAuth.AuthenticationActivity;
import com.itachi1706.cheesecakeutilities.Features.FingerprintAuth.BiometricCompatHelper;
import com.itachi1706.cheesecakeutilities.Features.UtilityManagement.ManageUtilityActivity;
import com.itachi1706.cheesecakeutilities.Util.CommonVariables;

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
    public static class GeneralPreferenceFragment extends EasterEggResMusicPrefFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            new SettingsInitializer().setFullscreen(true).explodeUpdaterSettings(getActivity(), R.drawable.notification_icon, CommonVariables.BASE_SERVER_URL,
                    getResources().getString(R.string.link_legacy), getResources().getString(R.string.link_updates), this);
            super.addEggMethods(true, preference -> {
                new LicensesDialog.Builder(getActivity()).setNotices(R.raw.notices)
                        .setIncludeOwnLicense(true).build().show();
                return true;
            });

            // Authentication processing
            final Preference fp_pw = findPreference("password_fp");

            sp = PrefHelper.getDefaultSharedPreferences(getActivity());
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

            findPreference(BiometricCompatHelper.APP_BIOMETRIC_COMPAT_ENABLED).setOnPreferenceChangeListener((preference, newValue) -> {
                updatePasswordViews(fp_pw, (boolean) newValue, 0);
                return true;
            });

            findPreference(BiometricCompatHelper.SCREEN_LOCK_ENABLED).setOnPreferenceChangeListener((preference, newValue) -> {
                updatePasswordViews(fp_pw, (boolean) newValue, 1);
                return true;
            });

            findPreference("firebase_signin").setOnPreferenceClickListener(preference -> {
                Intent i = new Intent(getActivity(), FirebaseLoginActivity.class);
                i.putExtra("persist", true);
                startActivity(i);
                return false;
            });
        }

        @Override
        public void onResume() {
            super.onResume();

            updatePasswordViews(findPreference("password_fp"));
            boolean hasSL = BiometricCompatHelper.Companion.isScreenLockEnabled(getActivity());
            findPreference(BiometricCompatHelper.SCREEN_LOCK_ENABLED).setEnabled(hasSL);
            findPreference(BiometricCompatHelper.APP_BIOMETRIC_COMPAT_ENABLED).setEnabled(hasSL);
        }

        SharedPreferences sp;

        private void updatePasswordViews(Preference fp_pw) {
            updatePasswordViews(fp_pw, BiometricCompatHelper.Companion.requireFPAuth(sp), -1);
        }

        private void updatePasswordViews(Preference fp_pw, boolean val, int type) {
            boolean isScreenLock = BiometricCompatHelper.Companion.isScreenLockProtectionEnabled(getActivity()), isFP = BiometricCompatHelper.Companion.requireFPAuth(sp);
            switch (type) {
                case 0: isFP = val; break;
                case 1: isScreenLock = val; break;
            }
            String summary = "Unprotected";
            if (isScreenLock) {
                if (BiometricCompatHelper.Companion.isScreenLockEnabled(getActivity())) {
                    summary = "Protected with device screen lock";
                    if (isFP) {
                        if (BiometricCompatHelper.Companion.isBiometricFPRegistered(getActivity())) summary = "Protected with fingerprint + screen lock";
                        else summary += " (No fingerprint found on device)";
                    }
                }
                else summary = "Unprotected (No screen lock found)";
            } else if (isFP) {
                if (!BiometricCompatHelper.Companion.isScreenLockEnabled(getActivity())) summary = "Unprotected (No screen lock found)"; // No FP without a screen lock
                else if (BiometricCompatHelper.Companion.isBiometricFPRegistered(getActivity())) summary = "Protected with fingerprint";
                else summary = "Unprotected (No fingerprint found on device)";
            }

            fp_pw.setSummary(summary);
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
