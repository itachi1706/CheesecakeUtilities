package com.itachi1706.cheesecakeutilities.Modules.SystemInformation.Fragments;

import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.BuildConfig;
import com.itachi1706.cheesecakeutilities.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This is usused but will be kept just in case something will use it
 */
public class StorageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_storage, container, false);
        TextView intMem = (TextView) view.findViewById(R.id.intMemTxt);
        intMem.setText(getIntMem());
        TextView extMem = (TextView) view.findViewById(R.id.extMemTxt);
        extMem.setText(getExtMem());
        TextView removableMem = (TextView) view.findViewById(R.id.remMemTxt);
        removableMem.setText(getRemovableMem());
        return view;
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
        return path + "\nTotal Space: " + totalSize + unitTotal + "\nAvailable: " + availableSize + unitAvail + "\nUsed: " + usedSize + unitUsed;
    }

    @SuppressWarnings("deprecation")
    public String getExtMem() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        double totalSize;
        double availableSize;
        double usedSize;
        String unitTotal;
        String unitAvail;
        String unitUsed;
        String sdkDetect;
        if (VERSION.SDK_INT >= 18) {
            long blockSize = statFs.getBlockSizeLong();
            totalSize = (double) ((statFs.getBlockCountLong() * blockSize) / 1048576);
            availableSize = (double) ((statFs.getAvailableBlocksLong() * blockSize) / 1048576);
            usedSize = totalSize - availableSize;
            unitTotal = " MB";
            unitAvail = " MB";
            unitUsed = " MB";
            sdkDetect = "Removable SD Card";
            String path = "Path: " + Environment.getExternalStorageDirectory().getAbsolutePath();
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
            if (VERSION.SDK_INT >= 14) {
                sdkDetect = "Emulated SD Card (Android 4.0+)";
            }
            return "Type: " + sdkDetect + "\n" + path + "\nTotal Space: " + totalSize + unitTotal + "\nAvailable: " + availableSize + unitAvail + "\nUsed: " + usedSize + unitUsed;
        }
        long blockSize = (long) statFs.getBlockSize();
        totalSize = (double) ((((long) statFs.getBlockCount()) * blockSize) / 1048576);
        availableSize = (double) ((((long) statFs.getAvailableBlocks()) * blockSize) / 1048576);
        usedSize = totalSize - availableSize;
        unitTotal = " MB";
        unitAvail = " MB";
        unitUsed = " MB";
        sdkDetect = "Removable SD Card";
        String path = "Path: " + Environment.getExternalStorageDirectory().getAbsolutePath();
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
        if (VERSION.SDK_INT >= 14) {
            sdkDetect = "Emulated SD Card (Android 4.0+)";
        }
        return "Type: " + sdkDetect + "\n" + path + "\nTotal Space: " + totalSize + unitTotal + "\nAvailable: " + availableSize + unitAvail + "\nUsed: " + usedSize + unitUsed;
    }

    private static String extRemovablePath() {
        String sdcardPath = BuildConfig.FLAVOR;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("mount").getInputStream()));
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                } else if (!(line.contains("secure") || line.contains("asec"))) {
                    String[] columns;
                    if (line.contains("fat")) {
                        columns = line.split(" ");
                        if (columns.length > 1) {
                            sdcardPath = columns[1];
                        }
                    } else if (line.contains("fuse")) {
                        columns = line.split(" ");
                        if (columns.length > 1) {
                            sdcardPath = columns[1];
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sdcardPath;
    }

    @SuppressWarnings("deprecation")
    public String getRemovableMem() {
        if (String.valueOf(extRemovablePath()).equals("on")) {
            return null;
        }
        if (VERSION.SDK_INT <= 13) {
            return null;
        }
        if (extRemovablePath().isEmpty()) {
            return null;
        }
        if (VERSION.SDK_INT >= 19) {
            return null;
        }
        StatFs statFs = new StatFs(extRemovablePath());
        long blockSize = (long) statFs.getBlockSize();
        double totalSize = (double) ((((long) statFs.getBlockCount()) * blockSize) / 1048576);
        double availableSize = (double) ((((long) statFs.getAvailableBlocks()) * blockSize) / 1048576);
        double usedSize = totalSize - availableSize;
        String unitTotal = " MB";
        String unitAvail = " MB";
        String unitUsed = " MB";
        String sdkDetect = "Removable SD Card";
        String path = "Path: " + extRemovablePath();
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
        return "Type: " + sdkDetect + "\n" + path + "\nTotal Space: " + totalSize + unitTotal + "\nAvailable: " + availableSize + unitAvail + "\nUsed: " + usedSize + unitUsed;
    }
}
