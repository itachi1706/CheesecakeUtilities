package com.itachi1706.cheesecakeutilities.Features.FingerprintAuth;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.concurrent.Executor;

import androidx.biometrics.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

/**
 * Created by Kenneth on 14/12/2018.
 * for com.itachi1706.appupdater.extlib.fingerprint in CheesecakeUtilities
 */
public class BiometricCompatHelper {

    public static final String APP_BIOMETRIC_COMPAT_ENABLED = "app_bio_compat_enable";

    /**
     * Check if we need to authenticate with Fingerprint through the BiometricPromptCompat API
     * @param sp Shared Preference Object
     * @return true if require fingerprint, false otherwise
     */
    public static boolean requireFPAuth(SharedPreferences sp) {
        return sp.getBoolean(APP_BIOMETRIC_COMPAT_ENABLED, false);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isBiometricFPRegistered(Context context) throws NullPointerException {
        if (!isBiometricAuthFPAvailable(context)) return false;
        //noinspection ResourceType

        KeyguardManager km = context.getSystemService(KeyguardManager.class);
        if (km == null) {
            Log.e("BioCompat", "Keyguard died!");
            return false;
        }
        FingerprintManagerCompat fpCompat = FingerprintManagerCompat.from(context);
        return km.isKeyguardSecure() && fpCompat.hasEnrolledFingerprints();
    }

    public static Executor getBiometricExecutor() {
        return new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };
    }

    public static boolean isBiometricAuthFPAvailable(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false;
        int granted = ContextCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            int granted2 = ContextCompat.checkSelfPermission(context, Manifest.permission.USE_BIOMETRIC);
            if (granted2 != PackageManager.PERMISSION_GRANTED) return false;
        }
        if (granted != PackageManager.PERMISSION_GRANTED) return false;

        FingerprintManagerCompat fpCompat = FingerprintManagerCompat.from(context);
        return fpCompat.isHardwareDetected() && fpCompat.hasEnrolledFingerprints();
    }

    public static BiometricPrompt.PromptInfo createPromptObject() {
        return createPromptObject("Sign In", null, "Confirm fingerprint to continue");
    }

    public static BiometricPrompt.PromptInfo createPromptObject(String title, String subtitle, String description) {
        return createPromptObject(title, subtitle, description, "Cancel");
    }

    public static BiometricPrompt.PromptInfo createPromptObject(String title, String subtitle, String description, String negativeBtn) {
        return new BiometricPrompt.PromptInfo.Builder().setTitle(title).setSubtitle(subtitle).setDescription(description).setNegativeButtonText(negativeBtn).build();
    }
}
