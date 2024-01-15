package com.itachi1706.cheesecakeutilities.modules.systemInformation.fragments;

import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.itachi1706.cheesecakeutilities.BuildConfig;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.helperlib.helpers.LogHelper;
import com.itachi1706.helperlib.deprecation.StatFsDep;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class StorageFragment extends Fragment {

    List<Mounts> mountPoints;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_storage, container, false);
        getMountPoints();
        TextView intMem = view.findViewById(R.id.intMemTxt);
        intMem.setText(getIntMem());
        TextView extMem = view.findViewById(R.id.extMemTxt);
        extMem.setText(getExtMem());
        TextView removableMem = view.findViewById(R.id.remMemTxt);
        removableMem.setText(getRemovableMem());
        return view;
    }

    private class Mounts {
        private String mountDevice, mountPoint, filesystem, options;

        Mounts(String mountDevice, String mountPoint, String filesystem, String options) {
            this.mountDevice = mountDevice;
            this.mountPoint = mountPoint;
            this.filesystem = filesystem;
            this.options = options;
        }

        String getMountDevice() {
            return mountDevice;
        }

        String getMountPoint() {
            return mountPoint;
        }

        String getFilesystem() {
            return filesystem;
        }

        String getOptions() {
            return options;
        }
    }

    @Nullable
    public List<Mounts> getMountPoints() {
        List<String> mountsTmp = new ArrayList<>();
        try {
            RandomAccessFile file = new RandomAccessFile("/proc/mounts", "r");
            String line;
            while (true) {
                line = file.readLine();
                if (line != null && !line.isEmpty()) {
                    mountsTmp.add(line);
                } else {
                    LogHelper.i("Read Mount file", "Finished reading mount file");
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        mountPoints = new ArrayList<>();
        for (String m : mountsTmp) {
            String[] ms = m.split(" ");
            if (ms.length >= 4) {
                mountPoints.add(new Mounts(ms[0], ms[1], ms[2], ms[3]));
            }
        }
        return mountPoints;
    }

    @Nullable
    private Mounts getMountPoint(String path) {
        for (Mounts m : mountPoints) {
            if (m.getMountPoint().equals(path))
                return m;
        }

        Mounts bestMount = null; // Doing a more advanced search
        int bestScore = 999999999;
        for (Mounts m : mountPoints) {
            int levenshteinDistance = StringUtils.getLevenshteinDistance(path, m.getMountPoint(), bestScore);
            if (bestScore > levenshteinDistance && levenshteinDistance > -1) {
                bestScore = levenshteinDistance;
                bestMount = m;
            }
        }
        return bestMount;
    }

    public String getIntMem() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long blockSize = StatFsDep.getBlockSize(statFs);
        double totalSize = (double) ((StatFsDep.getBlockCount(statFs) * blockSize) / 1048576);
        double availableSize = (double) ((StatFsDep.getAvailableBlocks(statFs) * blockSize) / 1048576);
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
        Mounts mem = getMountPoint(Environment.getDataDirectory().getAbsolutePath());
        String additionalData = "";
        if (mem != null) {
            additionalData += "Blockdevice: " + mem.getMountDevice()
                    + (mem.getMountDevice().equals("/dev/fuse") ? " (Emulated)" : "")
                    + "\nFilesystem: " + mem.getFilesystem().toUpperCase()
                    + "\nMountpoint: " + mem.getMountPoint() + "\nOptions: " + mem.getOptions();
        }
        return path + "\nTotal Space: " + totalSize + unitTotal
                + "\nAvailable: " + availableSize + unitAvail
                + "\nUsed: " + usedSize + unitUsed + ((additionalData.isEmpty()) ? "" : "\n" + additionalData);
    }

    public String getExtMem() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        double totalSize;
        double availableSize;
        long blockSize = StatFsDep.getBlockSize(statFs);
        totalSize = (double) ((StatFsDep.getBlockCount(statFs) * blockSize) / 1048576);
        availableSize = (double) ((StatFsDep.getAvailableBlocks(statFs) * blockSize) / 1048576);
        double usedSize = totalSize - availableSize;
        String unitTotal = " MB";
        String unitAvail = " MB";
        String unitUsed = " MB";
        String sdkDetect = "Emulated SD Card (Android 4.0+)";
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
        Mounts mem = getMountPoint(Environment.getExternalStorageDirectory().getAbsolutePath());
        String additionalData = "";
        if (mem != null) {
            additionalData += "Blockdevice: " + mem.getMountDevice()
                    + (mem.getMountDevice().equals("/dev/fuse") ? " (Emulated)" : "")
                    + "\nFilesystem: " + mem.getFilesystem().toUpperCase()
                    + "\nMountpoint: " + mem.getMountPoint() + "\nOptions: " + mem.getOptions();
        }
        return "Type: " + sdkDetect + "\n" + path + "\nTotal Space: "
                + totalSize + unitTotal + "\nAvailable: " + availableSize + unitAvail
                + "\nUsed: " + usedSize + unitUsed + ((additionalData.isEmpty()) ? "" : "\n" + additionalData);
    }

    private static String extRemovablePath() {
        String sdcardPath = "";
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

    public String getRemovableMem() {
        if (String.valueOf(extRemovablePath()).equals("on")) {
            return null;
        }
        if (extRemovablePath().isEmpty()) {
            return null;
        }
        if (VERSION.SDK_INT >= 19) {
            return null;
        }
        StatFs statFs = new StatFs(extRemovablePath());
        long blockSize = StatFsDep.getBlockSize(statFs);
        double totalSize = (double) ((StatFsDep.getBlockCount(statFs) * blockSize) / 1048576);
        double availableSize = (double) ((StatFsDep.getAvailableBlocks(statFs) * blockSize) / 1048576);
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
        Mounts mem = getMountPoint(Environment.getExternalStorageDirectory().getAbsolutePath());
        String additionalData = "";
        if (mem != null) {
            additionalData += "Blockdevice: " + mem.getMountDevice()
                    + (mem.getMountDevice().equals("/dev/fuse") ? " (Emulated)" : "")
                    + "\nFilesystem: " + mem.getFilesystem().toUpperCase()
                    + "\nMountpoint: " + mem.getMountPoint() + "\nOptions: " + mem.getOptions();
        }
        return "Type: " + sdkDetect + "\n" + path
                + "\nTotal Space: " + totalSize + unitTotal
                + "\nAvailable: " + availableSize + unitAvail
                + "\nUsed: " + usedSize + unitUsed + ((additionalData.isEmpty()) ? "" : "\n" + additionalData);
    }
}
