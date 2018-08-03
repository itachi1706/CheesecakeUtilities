package com.itachi1706.cheesecakeutilities.extlibs.com.scottyab.safetynet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Useful but separate utils used by the safetynet helper
 */
public class Utils {


    private static final String TAG = Utils.class.getSimpleName();

    public static String getSigningKeyFingerprint(Context ctx) {
        String result = null;
        try {
            byte[] certEncoded = getSigningKeyCertificate(ctx);
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(certEncoded);
            result = byte2HexFormatted(publicKey);
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return result;
    }

    /**
     * Gets the encoded representation of the first signing cerificated used to sign current APK
     * @param ctx Context
     * @return Signing Key Cert
     */
    private static byte[] getSigningKeyCertificate(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            String packageName = ctx.getPackageName();
            int flags = PackageManager.GET_SIGNATURES;
            @SuppressLint("PackageManagerGetSignatures") PackageInfo packageInfo = pm.getPackageInfo(packageName, flags);
            Signature[] signatures = packageInfo.signatures;

            if(signatures!=null && signatures.length>=1) {
                //takes just the first signature, TODO: handle multi signed apks
                byte[] cert = signatures[0].toByteArray();
                InputStream input = new ByteArrayInputStream(cert);
                CertificateFactory cf = CertificateFactory.getInstance("X509");
                X509Certificate c = (X509Certificate) cf.generateCertificate(input);
                return c.getEncoded();

            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return null;
    }

    private static String byte2HexFormatted(byte[] arr) {
        StringBuilder str = new StringBuilder(arr.length * 2);
        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1) h = "0" + h;
            if (l > 2) h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < (arr.length - 1)) str.append(':');
        }
        return str.toString();
    }

    @SuppressLint("PackageManagerGetSignatures")
    public static List<String> calcApkCertificateDigests(Context context, String packageName) {
        List<String> encodedSignatures = new ArrayList<>();

        // Get signatures from package manager
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return encodedSignatures;
        }
        Signature[] signatures = packageInfo.signatures;

        // Calculate b64 encoded sha256 hash of signatures
        for (Signature signature : signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(signature.toByteArray());
                byte[] digest = md.digest();
                encodedSignatures.add(Base64.encodeToString(digest, Base64.NO_WRAP));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return encodedSignatures;
    }

    public static String calcApkDigest(final Context context) {
        byte[] hashed2 = getApkFileDigest(context);
        return Base64.encodeToString(hashed2, Base64.NO_WRAP);
    }


    private static byte[] getApkFileDigest(Context context) {
        String apkPath = context.getPackageCodePath();
        try {
            return getDigest(new FileInputStream(apkPath));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    private static final int BUFFER_SIZE = 2048;

    private static byte[] getDigest(InputStream in) throws Throwable {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try {
            DigestInputStream dis = new DigestInputStream(in, md);
            byte[] buffer = new byte[BUFFER_SIZE];
            //noinspection StatementWithEmptyBody
            while (dis.read(buffer) != -1) ;
            dis.close();
        } finally {
            in.close();
        }
        return md.digest();
    }


}