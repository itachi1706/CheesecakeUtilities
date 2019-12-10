package com.itachi1706.cheesecakeutilities.modules.systemInformation.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.view.PointerIconCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.fragment.app.Fragment;

import com.itachi1706.cheesecakeutilities.BuildConfig;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.util.LogHelper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.regex.Pattern;

public class DeviceFragment extends Fragment {
    private static final int EVENT_TICK = 1;
    private TextView getCpuFreq;
    private TextView mFreeRam;
    MyInnerHandler mHandler;

    private static int err_cpu_min_count = 0, err_cpu_freq_count = 0;
    private static final int SUPRESS_WARNINGS = 10;

    private static class MyInnerHandler extends Handler {
        WeakReference<DeviceFragment> mFrag;

        MyInnerHandler(DeviceFragment aFragment) {
            this.mFrag = new WeakReference<>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            DeviceFragment myUpdate = this.mFrag.get();
            switch (msg.what) {
                case 1:
                    myUpdate.liveUpdate();
                    sendEmptyMessageDelayed(DeviceFragment.EVENT_TICK, 1000);
                default:
            }
        }
    }

    public DeviceFragment() {
        this.mHandler = new MyInnerHandler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_device, container, false);
        TextView cpuMax = view.findViewById(R.id.cpuMaxTxt);
        cpuMax.setText(getCpuMax());
        TextView cpuMin = view.findViewById(R.id.cpuMinTxt);
        cpuMin.setText(getCpuMin());
        TextView cpuType = view.findViewById(R.id.cpuTypeTxt);
        cpuType.setText(getCpuType());
        TextView cpuInstruct = view.findViewById(R.id.cpuInstructTxt);
        cpuInstruct.setText(getCpuInstructions());
        TextView cpuCoreCount = view.findViewById(R.id.cpuCoreCountTxt);
        cpuCoreCount.setText(getString(R.string.sys_info_cpucores, String.valueOf(getCoreCount())));
        TextView ramTotal = view.findViewById(R.id.ramTotal);
        ramTotal.setText(getTotalRAM());
        TextView deviceInfo = view.findViewById(R.id.deviceInfoTxt);
        deviceInfo.setText(getDeviceInfo());
        TextView displayInfo = view.findViewById(R.id.displayInfoTxt);
        TextView displaySize = view.findViewById(R.id.displaySizeTxt);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            displayInfo.setText(getDisplayInfo());
            displaySize.setText(getDisplaySize());
        }
        return view;
    }

    private void liveUpdate() {
        this.mFreeRam.setText(getFreeRAM());
        this.getCpuFreq.setText(getString(R.string.sys_info_cpufreq, getCpuFrequency()));
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mFreeRam = getActivity().findViewById(R.id.ramFree);
        this.getCpuFreq = getActivity().findViewById(R.id.cpuFreq);
        if (getCpuFrequency().equalsIgnoreCase("GONE")) {
            this.getCpuFreq.setVisibility(View.GONE);
        }
        this.mHandler.sendEmptyMessageDelayed(EVENT_TICK, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mHandler.removeMessages(EVENT_TICK);
    }

    private static String getCpuType() {
        String cpu = null;
        String cpu1 = null;
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/cpuinfo", "r");
            cpu = reader.readLine();
            cpu1 = reader.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (cpu == null) {
            return "CPU Type: unknown";
        }
        if (cpu1 == null) {
            return "CPU Type: unknown";
        }
        if (cpu.replaceAll("[^\\d.]", BuildConfig.FLAVOR).equals("0")) {
            return String.valueOf(cpu1);
        }
        return String.valueOf(cpu);
    }

    private static String getCpuMax() {
        String load = null;
        try {
            load = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq", "r").readLine();
        } catch (IOException ex) {
            LogHelper.e("CPU-READ", "Error reading file: " + ex.getLocalizedMessage());
        }
        if (load == null) {
            return "Minimum Frequency: unknown";
        }
        return "Minimum Frequency: " + String.valueOf(Integer.parseInt(load.replace("\n", BuildConfig.FLAVOR)) /
                PointerIconCompat.TYPE_DEFAULT) + " MHz";
    }

    private static String getCpuMin() {
        String load = null;
        try {
            load = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r").readLine();
        } catch (IOException ex) {
            if (err_cpu_min_count <= SUPRESS_WARNINGS)
                LogHelper.e("CPU-READ", "Error reading file: " + ex.getLocalizedMessage());
            if (err_cpu_min_count == SUPRESS_WARNINGS)
                LogHelper.e("CPU-READ", "Supressing warnings for this exception due to repeated throwing");
            err_cpu_min_count++;
        }
        if (load == null) {
            return "Maximum Frequency: unknown";
        }
        return "Maximum Frequency: " + String.valueOf(Integer.parseInt(load.replace("\n", BuildConfig.FLAVOR)) /
                PointerIconCompat.TYPE_DEFAULT) + " MHz";
    }

    private static String getCpuInstructions() {
        StringBuilder builder = new StringBuilder();
        builder.append("Instruction Sets: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (String s : Build.SUPPORTED_ABIS) {
                builder.append(s).append(", ");
            }
        } else {
            //noinspection deprecation
            builder.append(Build.CPU_ABI).append(", ").append(Build.CPU_ABI2);
        }
        return builder.toString();
    }

    private int getCoreCount() {
        try {
            return new File("/sys/devices/system/cpu/").listFiles(pathname -> Pattern.matches("cpu[0-9]+", pathname.getName())).length;
        } catch (Exception e) {
            return EVENT_TICK;
        }
    }

    private String getCpuFrequency() {
        String freq = null;
        try {
            freq = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq", "r").readLine();
        } catch (IOException ex) {
            if (err_cpu_freq_count <= SUPRESS_WARNINGS)
                LogHelper.e("CPU-READ", "Error reading file: " + ex.getLocalizedMessage());
            if (err_cpu_freq_count == SUPRESS_WARNINGS)
                LogHelper.e("CPU-READ", "Supressing warnings for this exception due to repeated throwing");
            err_cpu_freq_count++;
        }
        if (freq == null) {
            return "GONE";
        }
        return String.valueOf((long) Math.round((float) (Integer.parseInt(freq.replaceAll("[^\\d.]", BuildConfig.FLAVOR)) /
                AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT))) + " MHz";
    }

    public static String getFreeRAM() {
        String load1 = null;
        String load2 = null;
        String load3 = null;
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile("/proc/meminfo", "r");
            randomAccessFile.readLine();
            load1 = randomAccessFile.readLine();
            load2 = randomAccessFile.readLine();
            load3 = randomAccessFile.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (load1 == null) {
            return "Free RAM: unknown";
        }
        if (load2 == null) {
            return "Free RAM: unknown";
        }
        if (load3 == null) {
            return "Free RAM: unknown";
        }
        long megabytes1 = (long) Math.round((float) (Integer.parseInt(load1.replaceAll("[^\\d.]", BuildConfig.FLAVOR)) /
                AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT));
        long megabytes2 = (long) Math.round((float) (Integer.parseInt(load2.replaceAll("[^\\d.]", BuildConfig.FLAVOR)) /
                AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT));
        long j = megabytes1 + megabytes2;
        long totalfree = j + ((long) Math.round((float) (Integer.parseInt(load3.replaceAll("[^\\d.]", BuildConfig.FLAVOR)) /
                AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT)));
        return "Free RAM: " + String.valueOf(totalfree) + " MB";
    }

    public static String getTotalRAM() {
        String load = null;
        try {
            load = new RandomAccessFile("/proc/meminfo", "r").readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (load == null) {
            return "Total RAM: unknown";
        }
        return "Total RAM: " + String.valueOf((long) Math.round((float) (Integer.parseInt(load.replaceAll("[^\\d.]",
                BuildConfig.FLAVOR)) / AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT))) + " MB";
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private String getDisplayInfo() {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) getActivity().getApplicationContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRealMetrics(dm);
        return "Resolution: " + String.valueOf(dm.widthPixels) + " x " + String.valueOf(dm.heightPixels);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private String getDisplaySize() {
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        return "Screen Size: " + String.format(Locale.US, "%.2f",
                Math.sqrt(Math.pow((double) (((float) dm.widthPixels) / dm.xdpi), 2.0d) +
                        Math.pow((double) (((float) dm.heightPixels) / dm.ydpi), 2.0d))) + " inches";
    }

    private static String getDeviceInfo() {
        return "Brand: " + Build.BRAND + "\nBoard: " + Build.BOARD + "\nDevice: " + Build.DEVICE + "\nHardware: " +
                Build.HARDWARE + "\nManufacturer: " + Build.MANUFACTURER + "\nModel: " + Build.MODEL + "\nProduct: " +
                Build.PRODUCT + "\nBootloader: " + Build.BOOTLOADER;
    }
}
