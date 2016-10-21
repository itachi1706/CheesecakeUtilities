package com.itachi1706.cheesecakeutilities.Modules.SystemInformation.Fragments;

import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

public class SystemFragment extends Fragment {
    private static final int EVENT_TICK = 1;
    MyInnerHandler mHandler;
    private TextView mUptime;

    public static String getCodename(int sdkint) {
        for (Field fld : Build.VERSION_CODES.class.getFields()) {
            try {
                if (fld.getInt(null) == sdkint && fld.getName().length() > 1) {
                    return fld.getName();
                }
            } catch (IllegalAccessException e) {
                return "Unknown";
            }
        }
        return "Unknown";
    }

    static class MyInnerHandler extends Handler {
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
        TextView osInfo = (TextView) view.findViewById(R.id.osInfoTxt);
        osInfo.setText(getOsInfo());
        TextView osCodename = (TextView) view.findViewById(R.id.osCodenameTxt);
        osCodename.setText(getString(R.string.codename, getCodename(VERSION.SDK_INT)));// String.valueOf(Codenames.getCodename())));
        TextView intMem = (TextView) view.findViewById(R.id.intMemTxt);
        intMem.setText(getIntMem());
        TextView javaInfo = (TextView) view.findViewById(R.id.javaInfoTxt);
        javaInfo.setText(getJavaInfo());
        return view;
    }

    private void liveUpdate() {
        this.mUptime.setText(getString(R.string.uptime, DateUtils.formatElapsedTime(SystemClock.elapsedRealtime() / 1000)));
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mUptime = (TextView) getActivity().findViewById(R.id.uptime);
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
        return builder.toString();
    }

    @SuppressWarnings("deprecation")
    public String getIntMem() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long blockSize = (long) statFs.getBlockSize();
        double totalSize = (double) ((((long) statFs.getBlockCount()) * blockSize) / 1048576);
        double availableSize = (double) ((((long) statFs.getAvailableBlocks()) * blockSize) / 1048576);
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
