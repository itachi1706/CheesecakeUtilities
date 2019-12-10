package com.itachi1706.cheesecakeutilities.Modules.SystemInformation.Fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.itachi1706.cheesecakeutilities.BuildConfig;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.util.LogHelper;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;

public class GpuFragment extends Fragment {
    private static final int EVENT_TICK = 1;
    private TextView getGPUClock;
    private TextView getGPUClock1;
    MyInnerHandler mHandler;

    private static int err_gpu_clock_count = 0, err_gpu_clock1_count = 0;
    private static final int SUPRESS_WARNINGS = 10;

    private static class MyInnerHandler extends Handler {
        WeakReference<GpuFragment> mFrag;

        MyInnerHandler(GpuFragment aFragment) {
            this.mFrag = new WeakReference<>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            GpuFragment myUpdate = this.mFrag.get();
            switch (msg.what) {
                case 1:
                    myUpdate.liveUpdate();
                    sendEmptyMessageDelayed(GpuFragment.EVENT_TICK, 1000);
                default:
            }
        }
    }

    public GpuFragment() {
        this.mHandler = new MyInnerHandler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_gpuinfo, container, false);
        ((TextView) view.findViewById(R.id.gpuInfoTxt)).setText(getGpuInfo());
        ((TextView) view.findViewById(R.id.gpuExtensionsTxt)).setText(getGpuExtensions());
        TextView getMaxGPUClock = view.findViewById(R.id.getMaxGPUClock);
        if (getMaxGPUClock().equalsIgnoreCase(BuildConfig.FLAVOR)) {
            getMaxGPUClock.setVisibility(View.GONE);
        } else {
            getMaxGPUClock.setText(getString(R.string.sys_info_gpu_clock_max_adreno, getMaxGPUClock()));
        }
        TextView getMaxGPUClock1 = view.findViewById(R.id.getMaxGPUClock1);
        if (getMaxGPUClock1().equalsIgnoreCase(BuildConfig.FLAVOR)) {
            getMaxGPUClock1.setVisibility(View.GONE);
        } else {
            getMaxGPUClock1.setText(getString(R.string.sys_info_gpu_clock_max_mali, getMaxGPUClock1()));
        }
        return view;
    }

    private void liveUpdate() {
        getGPUClock.setText(getString(R.string.sys_info_gpu_clock_adreno, String.valueOf(getGPUClock())));
        getGPUClock1.setText(getString(R.string.sys_info_gpu_clock_mali, String.valueOf(getGPUClock1())));
    }

    @Override
    public void onResume() {
        super.onResume();
        getGPUClock = getActivity().findViewById(R.id.getGPUClock);
        if (getGPUClock().equalsIgnoreCase(BuildConfig.FLAVOR)) {
            getGPUClock.setVisibility(View.GONE);
        }
        getGPUClock1 = getActivity().findViewById(R.id.getGPUClock1);
        if (getGPUClock1().equalsIgnoreCase(BuildConfig.FLAVOR)) {
            getGPUClock1.setVisibility(View.GONE);
        }
        this.mHandler.sendEmptyMessageDelayed(EVENT_TICK, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mHandler.removeMessages(EVENT_TICK);
    }

    public String getGpuInfo() {
        ActivityManager am = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        SharedPreferences prefs = getActivity().getSharedPreferences("GPUinfo", 0);
        String vendor = prefs.getString("VENDOR", null);
        String renderer = prefs.getString("RENDERER", null);
        return getString(R.string.sys_info_gpu_vendor) + vendor + "\n" + getString(R.string.sys_info_gpu_vendor) + renderer + "\n" + getString(R.string.sys_info_gpu_gles_version) + String.valueOf(am.getDeviceConfigurationInfo().getGlEsVersion());
    }

    public String getGpuExtensions() {
        return getActivity().getSharedPreferences("GPUinfo", 0).getString("EXTENSIONS", "").trim().replace(" ", "\n");
    }

    private String getGPUClock() {
        String freq = null;
        try {
            freq = new RandomAccessFile("/sys/class/kgsl/kgsl-3d0/gpuclk", "r").readLine();
        } catch (IOException ex) {
            if (err_gpu_clock_count <= SUPRESS_WARNINGS)
                LogHelper.e("GPU-READ", "Error reading file: " + ex.getLocalizedMessage());
            if (err_gpu_clock_count == SUPRESS_WARNINGS)
                LogHelper.e("GPU-READ", "Supressing warnings for this exception due to repeated throwing");
            err_gpu_clock_count++;
        }
        if (freq == null) {
            return BuildConfig.FLAVOR;
        }
        return String.valueOf((long) Math.round((float) (Integer.parseInt(freq.replaceAll("[^\\d.]", BuildConfig.FLAVOR)) / 1000000))) + getString(R.string.sys_info_gpu_mhz);
    }

    private String getMaxGPUClock() {
        String freq = null;
        try {
            freq = new RandomAccessFile("/sys/class/kgsl/kgsl-3d0/max_gpuclk", "r").readLine();
        } catch (IOException ex) {
            LogHelper.e("GPU-READ", "Error reading file: " + ex.getLocalizedMessage());
        }
        if (freq == null) {
            return BuildConfig.FLAVOR;
        }
        return String.valueOf((long) Math.round((float) (Integer.parseInt(freq.replaceAll("[^\\d.]", BuildConfig.FLAVOR)) / 1000000))) + getString(R.string.sys_info_gpu_mhz);
    }

    private String getGPUClock1() {
        String freq = null;
        try {
            freq = new RandomAccessFile("/sys/devices/platform/gpusysfs/gpu_clock", "r").readLine();
        } catch (IOException ex) {
            if (err_gpu_clock1_count <= SUPRESS_WARNINGS)
                LogHelper.e("GPU-READ", "Error reading file: " + ex.getLocalizedMessage());
            if (err_gpu_clock1_count == SUPRESS_WARNINGS)
                LogHelper.e("GPU-READ", "Supressing warnings for this exception due to repeated throwing");
            err_gpu_clock1_count++;
        }
        if (freq == null) {
            return BuildConfig.FLAVOR;
        }
        return String.valueOf((long) Math.round((float) Integer.parseInt(freq.replaceAll("[^\\d.]", BuildConfig.FLAVOR)))) + getString(R.string.sys_info_gpu_mhz);
    }

    private String getMaxGPUClock1() {
        String freq = null;
        try {
            freq = new RandomAccessFile("/sys/devices/platform/gpusysfs/gpu_max_clock", "r").readLine();
        } catch (IOException ex) {
            LogHelper.e("GPU-READ", "Error reading file: " + ex.getLocalizedMessage());
        }
        if (freq == null) {
            return BuildConfig.FLAVOR;
        }
        return String.valueOf((long) Math.round((float) Integer.parseInt(freq.replaceAll("[^\\d.]", BuildConfig.FLAVOR)))) + getString(R.string.sys_info_gpu_mhz);
    }
}
