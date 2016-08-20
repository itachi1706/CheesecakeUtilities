package com.itachi1706.cheesecakeutilities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.itachi1706.appupdater.AppUpdateChecker;
import com.itachi1706.appupdater.Util.UpdaterHelper;
import com.itachi1706.appupdater.Util.ValidationHelper;
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

            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

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

            findPreference("launch_updater").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AppUpdateChecker(getActivity(), sp, R.mipmap.ic_launcher, CommonVariables.BASE_SERVER_URL).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    return false;
                }
            });

            findPreference("view_sdk_version").setSummary(android.os.Build.VERSION.RELEASE);
            findPreference("vDevInfo").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), DebugInfoActivity.class));
                    return true;
                }
            });

            findPreference("android_changelog").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    UpdaterHelper.settingGenerateChangelog(sp, getActivity());
                    return true;
                }
            });

            findPreference("get_old_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(getResources().getString(R.string.link_legacy)));
                    startActivity(i);
                    return false;
                }
            });

            findPreference("get_latest_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(getResources().getString(R.string.link_updates)));
                    startActivity(i);
                    return false;
                }
            });

            String installLocation;
            String location = ValidationHelper.getInstallLocation(getActivity());
            switch (ValidationHelper.checkInstallLocation(getActivity())) {
                case ValidationHelper.GOOGLE_PLAY: installLocation = "Google Play (" + location + ")"; break;
                case ValidationHelper.AMAZON: installLocation = "Amazon App Store (" + location + ")"; break;
                case ValidationHelper.SIDELOAD:
                default: installLocation = "Sideloaded";
            }
            findPreference("installer_from").setSummary(installLocation);

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
