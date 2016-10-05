package com.itachi1706.cheesecakeutilities;


import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.itachi1706.appupdater.SettingsInitializer;
import com.itachi1706.cheesecakeutilities.Util.CommonVariables;


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
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            //Debug Info Get
            String version = "NULL", packName = "NULL";
            int versionCode = 0;
            try {
                PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                version = pInfo.versionName;
                packName = pInfo.packageName;
                versionCode = pInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            Preference verPref = findPreference("view_app_version");
            verPref.setSummary(version + "-b" + versionCode);
            findPreference("view_app_name").setSummary(packName);
            findPreference("view_sdk_version").setSummary(android.os.Build.VERSION.RELEASE);
            findPreference("vDevInfo").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), DebugInfoActivity.class));
                    return true;
                }
            });

            new SettingsInitializer(getActivity(), R.mipmap.ic_launcher,
                    CommonVariables.BASE_SERVER_URL, getResources().getString(R.string.link_legacy),
                    getResources().getString(R.string.link_updates)).explodeSettings(this);

            //Egg stuff
            verPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (!isActive) {
                        if (count == 10) {
                            count = 0;
                            startEgg();
                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Hello!", Snackbar.LENGTH_LONG)
                                    .setAction("SILENCE", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Music Stopped", Snackbar.LENGTH_SHORT).show();
                                            endEgg();
                                        }
                                    }).show();
                            return false;
                        } else if (count > 5)
                            prompt(10 - count);
                        count++;
                    }
                    return false;
                }
            });

            // Authentication processing
            KeyguardManager keyguardManager = (KeyguardManager) getActivity().getSystemService(KEYGUARD_SERVICE);
            FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(getActivity());
            Preference pw = findPreference("password");
            Preference fp_pw = findPreference("password_fp");

            if (fingerprintManager.isHardwareDetected()) {
                fp_pw.setSummary("No fingerprints enrolled on device");
            }

            if (fingerprintManager.hasEnrolledFingerprints()) {
                fp_pw.setSummary("Use fingerprint for authentication instead of password");
                fp_pw.setEnabled(true);
            }
        }


        /**
         * Eggs are always nice :wink:
         */

        MediaPlayer mp;
        int count = 0;
        Toast toasty;
        boolean isActive = false;

        private void prompt(int left){
            if (toasty != null)
                toasty.cancel();
            if (left > 1)
                toasty = Toast.makeText(getActivity(), left + " more clicks to have fun!", Toast.LENGTH_SHORT);
            else
                toasty = Toast.makeText(getActivity(), left + " more click to have fun!", Toast.LENGTH_SHORT);
            toasty.show();
        }

        @Override
        public void onResume(){
            super.onResume();
            count = 0;
        }

        @Override
        public void onPause(){
            super.onPause();
            endEgg();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            endEgg();
        }

        private void startEgg(){
            if (!isActive) {
                mp = MediaPlayer.create(getActivity(), R.raw.hello);
                mp.start();
                isActive = true;
            }
        }

        private void endEgg(){
            count = 0;
            isActive = false;
            if (mp != null){
                if (mp.isPlaying()){
                    mp.stop();
                    mp.reset();
                }
                mp.release();
                mp = null;
            }
        }
    }
}
