package com.itachi1706.cheesecakeutilities.Features.FingerprintAuth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.UUID;

/**
 * Created by Kenneth on 5/10/2016.
 * for com.itachi1706.cheesecakeutilities.Features.FingerprintAuth in CheesecakeUtilities
 */

/**
 * Flow for password create/update
 * 1) Check for existing password {@link PasswordHelper#hasPassword(SharedPreferences)}
 * 1.1) If no existing password, let user create password {@link PasswordHelper#savePassword(SharedPreferences, String)}
 * 2) Prompt user for existing password and verify its correct {@link PasswordHelper#verifyPassword(SharedPreferences, String)}
 * 3) Let user update password {@link PasswordHelper#savePassword(SharedPreferences, String)} or delete it {@link PasswordHelper#deletePassword(SharedPreferences)}
 */
public class PasswordHelper {

    public static SharedPreferences retrieveSP(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean hasPassword(Context context) {
        return hasPassword(retrieveSP(context));
    }

    private static final String APP_PASSWORD = "app_pw_unlock_enc", APP_KEY = "app_pw_unlock_key";

    public static boolean hasPassword(SharedPreferences sp) {
        return sp.contains(APP_PASSWORD);
    }

    public static String getPassword(SharedPreferences sp) {
        if (hasPassword(sp)) {
            return "";
        }
        return sp.getString(APP_PASSWORD, "");
    }

    private static void generateKeyIfNotExists(SharedPreferences sp) throws GeneralSecurityException {
        if (!sp.contains(APP_KEY)) {
            AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.generateKeyFromPassword(UUID.randomUUID().toString(), AesCbcWithIntegrity.generateSalt());
            sp.edit().putString(APP_KEY, keys.toString()).apply();
        }
    }

    private static AesCbcWithIntegrity.SecretKeys retrieveKey(SharedPreferences sp) throws InvalidKeyException {
        try {
            generateKeyIfNotExists(sp);
        } catch (GeneralSecurityException e) {
            Log.e("PasswordHelper", "GeneralSecurityException met when trying to generate key");
            return null;
        }
        String s = sp.getString(APP_KEY, "-1");
        if (s.equals("-1")) return null;

        return AesCbcWithIntegrity.keys(s);
    }

    public static boolean verifyPassword(SharedPreferences sp, String passwordEntered) throws InvalidKeyException {
        AesCbcWithIntegrity.SecretKeys secretKeys = retrieveKey(sp);
        if (secretKeys == null) {
            return true;   // Error occurred. Letting them in
        }

        Log.i("Authentication", "Verifying Password...");
        AesCbcWithIntegrity.CipherTextIvMac res;
        try {
             res = AesCbcWithIntegrity.encrypt(passwordEntered, secretKeys);
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            e.printStackTrace();
            Log.e("PasswordHelper", "Unsupported device, presuming go ahead");
            return true;
        }

        return res.toString().equals(getPassword(sp));
    }

    public static boolean savePassword(SharedPreferences sp, String newPassword) {
        AesCbcWithIntegrity.SecretKeys secretKeys;
        try {
            secretKeys = retrieveKey(sp);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return false;   // Error occurred
        }

        Log.i("Authentication", "Generating encrypted password");
        AesCbcWithIntegrity.CipherTextIvMac res;
        try {
            res = AesCbcWithIntegrity.encrypt(newPassword, secretKeys);
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            e.printStackTrace();
            Log.e("PasswordHelper", "Unsupported device, erroring out");
            return false;
        }

        sp.edit().putString(APP_PASSWORD, res.toString()).apply();
        return true;
    }

    public static boolean deletePassword(SharedPreferences sp) {
        sp.edit().remove(APP_PASSWORD).apply();
        return true;
    }
}
