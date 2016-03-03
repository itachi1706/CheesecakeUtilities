package com.itachi1706.cheesecakeutilities;


import android.app.AlertDialog;
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
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.cheesecakeutilities.Updater.AppUpdateChecker;
import com.itachi1706.cheesecakeutilities.Updater.Objects.AppUpdateObject;
import com.itachi1706.cheesecakeutilities.Updater.Util.UpdaterHelper;


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
            Preference pNamePref = findPreference("view_app_name");
            pNamePref.setSummary(packName);

            final Preference updaterPref = findPreference("launch_updater");
            updaterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AppUpdateChecker(getActivity(), sp).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

            Preference changelogPref = findPreference("android_changelog");
            changelogPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String changelog = sp.getString("version-changelog", "l");
                    if (changelog.equals("l")) {
                        //Not available
                        new AlertDialog.Builder(getActivity()).setTitle("No Changelog")
                                .setMessage("No changelog was found. Please check if you can connect to the server")
                                .setPositiveButton(android.R.string.ok, null).show();
                    } else {
                        Gson gson = new Gson();
                        AppUpdateObject updater = gson.fromJson(changelog, AppUpdateObject.class);
                        if (updater.getUpdateMessage().length == 0) {
                            new AlertDialog.Builder(getActivity()).setTitle("No Changelog")
                                    .setMessage("No changelog was found. Please check if you can connect to the server")
                                    .setPositiveButton(android.R.string.ok, null).show();
                        } else {
                            String message = "Latest Version: " + updater.getLatestVersion() + "<br /><br />";
                            message += UpdaterHelper.getChangelogStringFromArray(updater.getUpdateMessage());

                            new AlertDialog.Builder(getActivity()).setTitle("Changelog")
                                    .setMessage(Html.fromHtml(message)).setPositiveButton("Close", null).show();
                        }
                    }
                    return true;
                }
            });

            Preference oldVersionPref = findPreference("get_old_app");
            oldVersionPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(getResources().getString(R.string.link_legacy)));
                    startActivity(i);
                    return false;
                }
            });

            Preference latestVersionPref = findPreference("get_latest_app");
            latestVersionPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(getResources().getString(R.string.link_updates)));
                    startActivity(i);
                    return false;
                }
            });

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
                        } else {
                            switch (count) {
                                case 5:
                                    prompt(5);
                                    break;
                                case 6:
                                    prompt(4);
                                    break;
                                case 7:
                                    prompt(3);
                                    break;
                                case 8:
                                    prompt(2);
                                    break;
                                case 9:
                                    prompt(1);
                                    break;
                            }
                        }
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
            if (toasty != null){
                toasty.cancel();
            }
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
