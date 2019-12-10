package com.itachi1706.cheesecakeutilities.modules.SystemInformation.Fragments;

import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemClock;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itachi1706.appupdater.Util.DeprecationHelper;
import com.itachi1706.cheesecakeutilities.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemFragment extends Fragment {
    private static final int EVENT_TICK = 1;
    MyInnerHandler mHandler;
    private TextView mUptime;

    public static String getCodename(int sdkint) {
        for (Field fld : Build.VERSION_CODES.class.getFields()) {
            try {
                if (fld.getInt(null) == sdkint && fld.getName().length() >= 1) {
                    return fld.getName();
                }
            } catch (IllegalAccessException e) {
                return "Unknown (Exception Occurred: " + e.getLocalizedMessage() + ")";
            }
        }
        return "Unknown";
    }

    private static class MyInnerHandler extends Handler {
        WeakReference<SystemFragment> mFrag;

        MyInnerHandler(SystemFragment aFragment) {
            this.mFrag = new WeakReference<>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            SystemFragment myUpdate = this.mFrag.get();
            switch (msg.what) {
                case 1:
                    myUpdate.liveUpdate();
                    sendEmptyMessageDelayed(SystemFragment.EVENT_TICK, 1000);
                default:
            }
        }
    }

    public SystemFragment() {
        this.mHandler = new MyInnerHandler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_system, container, false);
        TextView osInfo = view.findViewById(R.id.osInfoTxt);
        osInfo.setText(getOsInfo());
        TextView osCodename = view.findViewById(R.id.osCodenameTxt);
        osCodename.setText(getString(R.string.sys_info_codename, getCodename(VERSION.SDK_INT)));// String.valueOf(Codenames.getCodename())));
        TextView intMem = view.findViewById(R.id.intMemTxt);
        intMem.setText(getIntMem());
        TextView javaInfo = view.findViewById(R.id.javaInfoTxt);
        javaInfo.setText(getJavaInfo());
        ((TextView) view.findViewById(R.id.adv_android_text)).setText(getAdvancedAndroidInfo());
        return view;
    }

    private void liveUpdate() {
        this.mUptime.setText(getString(R.string.sys_info_uptime, DateUtils.formatElapsedTime(SystemClock.elapsedRealtime() / 1000)));
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mUptime = getActivity().findViewById(R.id.uptime);
        this.mHandler.sendEmptyMessageDelayed(EVENT_TICK, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mHandler.removeMessages(EVENT_TICK);
    }

    private static String getOsInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("Android Version: ").append(VERSION.RELEASE).append("\nAPI Level: ")
                .append(String.valueOf(VERSION.SDK_INT));
        if (VERSION.SDK_INT >= Build.VERSION_CODES.M)
            builder.append("\nSecurity Patch Level: ").append(VERSION.SECURITY_PATCH);
        builder.append("\nRelease: ")
                .append(VERSION.CODENAME).append("\nIncremental: ").append(VERSION.INCREMENTAL)
                .append("\nKernel Type: ").append(System.getProperty("os.name")).append("\nKernel Version: ")
                .append(System.getProperty("os.version"));
        if (System.getProperty("java.vm.version").equals("2.1.0") && System.getProperty("java.vm.name").equals("Dalvik")) {
            builder.append("\nRuntime: ").append("ART (2.1.0)"); // ART
        } else {
            builder.append("\nRuntime: ").append(System.getProperty("java.vm.name")).append(" (")
                    .append(System.getProperty("java.vm.version")).append(")");
        }
        builder.append("\nRoot Status: ").append((isRooted()) ? "Rooted" : "Not Rooted").append("\nSELinux Status: ")
                .append((isSELinuxEnforcing()) ? "Enforcing" : "Permissive");
        return builder.toString();
    }

    public static boolean isSELinuxEnforcing() {
        try {
            Class<?> c = Class.forName("android.os.SELinux");
            boolean cs = (boolean) c.getMethod("isSELinuxEnabled").invoke(c);
            if (cs)
                cs = (boolean) c.getMethod("isSELinuxEnforced").invoke(c);
            return cs;
        } catch (Exception ignored) {
        }

        return VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    private static boolean findBinary(String binaryName) {
        String[] places = { "/sbin/", "/system/bin/", "/system/xbin/",
                "/data/local/xbin/", "/data/local/bin/",
                "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/" };
        for (String where : places) {
            if (new File(where + binaryName).exists()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRooted() {
        return findBinary("su");
    }

    public String getIntMem() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long blockSize = DeprecationHelper.StatFs.getBlockSize(statFs);
        double totalSize = (double) ((DeprecationHelper.StatFs.getBlockCount(statFs) * blockSize) / 1048576);
        double availableSize = (double) ((DeprecationHelper.StatFs.getAvailableBlocks(statFs) * blockSize) / 1048576);
        double usedSize = totalSize - availableSize;
        String unitTotal = " MB";
        String unitAvail = " MB";
        String unitUsed = " MB";
        String path = "Path: " + Environment.getDataDirectory().getAbsolutePath();
        if (totalSize >= 1024.0d) {
            totalSize = ((double) Math.round((100.0d * totalSize) / 1024.0d)) / 100.0d;
            unitTotal = " GB";
        }
        if (availableSize >= 1024.0d) {
            availableSize = ((double) Math.round((100.0d * availableSize) / 1024.0d)) / 100.0d;
            unitAvail = " GB";
        }
        if (usedSize >= 1024.0d) {
            usedSize = ((double) Math.round((100.0d * usedSize) / 1024.0d)) / 100.0d;
            unitUsed = " GB";
        }
        return path + "\nTotal Space: " + totalSize + unitTotal + "\nAvailable: " + availableSize +
                unitAvail + "\nUsed: " + usedSize + unitUsed;
    }

    public static String getAdvancedAndroidInfo() {
        return "Treble Enabled: " + getBuildPropProperty("ro.treble.enabled", "Enabled")
                + "\nSeamless Update (A/B Partitions): " + getBuildPropProperty("ro.build.ab_update", null)
                + "\nOEM Unlock: " + getBuildPropProperty("ro.oem_unlock_supported", null)
                + "\nMove2SD Enabled: " + getBuildPropProperty("ro.move2dcard.enable", null)
                + "\nVerified Boot State: " + getBuildPropProperty("ro.boot.verifiedbootstate", null)
                + "\nDM-Verity Status: " + getBuildPropProperty("ro.boot.veritymode", null)
                + "\nSecure Boot Enabled: " + getBuildPropProperty("ro.boot.secureboot", null)
                + "\nWaterproofed Device: " + getBuildPropProperty("ro.support.waterproof", null)
                + "\nDolby Audio Support: " + getBuildPropProperty("audio.dolby.ds2.enabled", null);
    }

    private static String getBuildPropProperty(String key, @Nullable String trueString) {
        String line;
        Pattern pattern = Pattern.compile("\\[(.+)\\]: \\[(.+)\\]");
        Matcher m;
        try {
            Process p = Runtime.getRuntime().exec("getprop");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                m = pattern.matcher(line);
                if (m.find()) {
                    MatchResult result = m.toMatchResult();
                    if(result.group(1).equals(key)) {
                        if (result.group(2).equals("1") || result.group(2).equals("true"))
                            if (trueString == null) return "Supported";
                            else return trueString;
                        else if (result.group(2).equals("0") || result.group(2).equals("false"))
                            return "Not Supported";
                        return result.group(2);
                    }
                }
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return "Not Supported";
    }

    private static String getJavaInfo() {
        return "VM Lib Vendor: " + System.getProperty("java.specification.vendor") + "\nVM Lib Name: "
                + System.getProperty("java.specification.name") + "\nVM Lib Ver: " +
                System.getProperty("java.specification.version") + "\nVM Imp Name: "
                + System.getProperty("java.vm.name") + "\nVM Imp Ver: " + System.getProperty("java.vm.version")
                + "\nVM Spec Name: " +
                System.getProperty("java.vm.specification.name") + "\nVM Spec Ver: "
                + System.getProperty("java.vm.specification.version") + "\nJNI Library Path: "
                + System.getProperty("java.library.path");
    }
}
