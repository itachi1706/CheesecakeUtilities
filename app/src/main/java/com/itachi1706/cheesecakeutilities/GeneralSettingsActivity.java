package com.itachi1706.cheesecakeutilities;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.itachi1706.appupdater.EasterEggResMusicPrefFragment;
import com.itachi1706.appupdater.SettingsInitializer;
import com.itachi1706.cheesecakeutilities.Features.FingerprintAuth.AuthenticationActivity;
import com.itachi1706.cheesecakeutilities.Features.FingerprintAuth.PasswordHelper;
import com.itachi1706.cheesecakeutilities.Features.UtilityManagement.ManageUtilityActivity;
import com.itachi1706.cheesecakeutilities.Util.CommonVariables;

import java.security.InvalidKeyException;


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

            new SettingsInitializer(getActivity(), R.mipmap.ic_launcher,
                    CommonVariables.BASE_SERVER_URL, getResources().getString(R.string.link_legacy),
                    getResources().getString(R.string.link_updates))
                    .explodeUpdaterSettings(this);
            super.addEggMethods();

            // Authentication processing
            final Preference pw = findPreference("password");
            final Preference fp_pw = findPreference("password_fp");

            sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            updatePasswordViews(pw, fp_pw);

            findPreference("testpw").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), AuthenticationActivity.class));
                    return false;
                }
            });

            findPreference("hide_util").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), ManageUtilityActivity.class));
                    return false;
                }
            });

            pw.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (PasswordHelper.hasPassword(sp)) {
                        final EditText currentPassword = new EditText(getActivity());
                        currentPassword.setSingleLine(true);
                        currentPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                        currentPassword.setHint("Enter Current Password");
                        final AlertDialog ad = new AlertDialog.Builder(getActivity()).setTitle("Enter Existing Password")
                                .setMessage("Enter Existing Password").setView(currentPassword)
                                .setPositiveButton(android.R.string.ok, null)
                                .setNegativeButton(android.R.string.cancel, null).setCancelable(false).create();
                        ad.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                Button b = ad.getButton(AlertDialog.BUTTON_POSITIVE);
                                b.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Validate Password
                                        String newPasswordString = currentPassword.getText().toString();
                                        if (newPasswordString.isEmpty()) {
                                            Toast.makeText(getActivity(), "Password cannot be empty!", Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        boolean result;
                                        try {
                                            result = PasswordHelper.verifyPassword(sp, newPasswordString);
                                        } catch (InvalidKeyException e) {
                                            e.printStackTrace();
                                            Log.e("PwChange", "Password Error. Invalid Key. Allowing user to change anyway");
                                            result = true;
                                        }

                                        if (result) {
                                            Log.i("PwChange", "Password Verified. Changing Password");
                                            newPassword();
                                            ad.dismiss();
                                            return;
                                        }
                                        // Invalid Password
                                        currentPassword.setError("Invalid Password");
                                    }
                                });
                            }
                        });
                        ad.show();
                        return true;
                    }
                    newPassword();
                    return true;
                }

                private boolean newPassword() {
                    final EditText newPassword = new EditText(getActivity());
                    newPassword.setSingleLine(true);
                    newPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    newPassword.setHint("Enter New Password");
                    new AlertDialog.Builder(getActivity()).setTitle("Set new Password")
                            .setMessage("Set a new app password or leave it blank to have no password").setView(newPassword)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Save Password
                                    String newPasswordString = newPassword.getText().toString();
                                    if (newPasswordString.isEmpty()) {
                                        // Delete Password
                                        if (PasswordHelper.hasPassword(sp)) {
                                            Toast.makeText(getActivity(), "Password removed!", Toast.LENGTH_LONG).show();
                                        }
                                        PasswordHelper.deletePassword(sp);
                                        updatePasswordViews(pw, fp_pw);
                                        return;
                                    }
                                    PasswordHelper.savePassword(sp, newPasswordString);
                                    updatePasswordViews(pw, fp_pw);
                                    Toast.makeText(getActivity(), "Password Updated!", Toast.LENGTH_LONG).show();
                                }
                            }).setNegativeButton(android.R.string.cancel, null).setCancelable(false).show();
                    return true;
                }
            });
        }

        SharedPreferences sp;

        private void updatePasswordViews(Preference pw, Preference fp_pw) {
            if (PasswordHelper.hasPassword(sp)) {
                FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(getActivity());
                if (fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints())
                    fp_pw.setSummary("Protected with In-app Password. You can also use your fingerprint to unlock!");
                else
                    fp_pw.setSummary("Protected with In-app Password");
                pw.setSummary("Click here to change/remove password");
                pw.setTitle("Change/Remove Password");
            } else {
                fp_pw.setSummary("Unprotected");
                pw.setSummary("No Password Set");
                pw.setTitle("Set Password");
            }
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
