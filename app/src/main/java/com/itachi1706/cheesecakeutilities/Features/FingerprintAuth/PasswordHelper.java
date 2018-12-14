package com.itachi1706.cheesecakeutilities.Features.FingerprintAuth;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.UUID;

/**
 * Created by Kenneth on 5/10/2016.
 * for com.itachi1706.cheesecakeutilities.Features.FingerprintAuth in CheesecakeUtilities
 */
public class PasswordHelper {
    /**
     * Flow for password create/update
     * 1) Check for existing password {@link PasswordHelper#hasPassword(SharedPreferences)}
     * 1.1) If no existing password, let user create password {@link PasswordHelper#savePassword(SharedPreferences, String)}
     * 2) Prompt user for existing password and verify its correct {@link PasswordHelper#verifyPassword(SharedPreferences, String)}
     * 3) Let user update password {@link PasswordHelper#savePassword(SharedPreferences, String)} or delete it {@link PasswordHelper#deletePassword(SharedPreferences)}
     */
    @Deprecated private static final String APP_PASSWORD = "app_pw_unlock_enc", APP_KEY = "app_pw_unlock_key";

    @Deprecated
    public static boolean hasPassword(SharedPreferences sp) {
        return sp.contains(APP_PASSWORD);
    }

    @Deprecated
    private static String getPassword(SharedPreferences sp) {
        if (!hasPassword(sp)) {
            return "";
        }
        return sp.getString(APP_PASSWORD, "");
    }

    public static void migrateToBiometric(SharedPreferences sp) {
        if (hasPassword(sp)) {
            sp.edit().putBoolean(BiometricCompatHelper.APP_BIOMETRIC_COMPAT_ENABLED, true).apply();
            sp.edit().remove(APP_PASSWORD).apply();
            sp.edit().remove(APP_KEY).apply();
        }
    }

    @Deprecated
    private static void generateKeyIfNotExists(SharedPreferences sp) throws GeneralSecurityException {
        if (!sp.contains(APP_KEY)) {
            AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.generateKeyFromPassword(UUID.randomUUID().toString(), AesCbcWithIntegrity.generateSalt());
            sp.edit().putString(APP_KEY, keys.toString()).apply();
        }
    }

    @Deprecated
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

    @Deprecated
    public static boolean verifyPassword(SharedPreferences sp, String passwordEntered) throws InvalidKeyException {
        AesCbcWithIntegrity.SecretKeys secretKeys = retrieveKey(sp);
        if (secretKeys == null) {
            return true;   // Error occurred. Letting them in
        }

        Log.i("Authentication", "Verifying Password...");
        AesCbcWithIntegrity.CipherTextIvMac existingPw = new AesCbcWithIntegrity.CipherTextIvMac(getPassword(sp));
        try {
            return passwordEntered.equals(AesCbcWithIntegrity.decryptString(existingPw, secretKeys));
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("PasswordHelper", "Error occurred evaluating, denying access through exception");
            throw new InvalidKeyException();
        }
    }

    @Deprecated
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

    @Deprecated
    public static boolean deletePassword(SharedPreferences sp) {
        if (hasPassword(sp)) {
            sp.edit().remove(APP_PASSWORD).apply();
        }
        return true;
    }
}
