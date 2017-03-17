package com.itachi1706.cheesecakeutilities.Modules.SystemInformation.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.BuildConfig;
import com.itachi1706.cheesecakeutilities.R;

import java.lang.ref.WeakReference;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public class NetworkFragment extends Fragment {
    private static final int EVENT_TICK = 1;
    private TextView dataIpAddress;
    private TextView getNetSsid;
    MyInnerHandler mHandler;
    private TextView macAddressInfo;

    static class MyInnerHandler extends Handler {
        WeakReference<NetworkFragment> mFrag;

        MyInnerHandler(NetworkFragment aFragment) {
            this.mFrag = new WeakReference<>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            NetworkFragment myUpdate = this.mFrag.get();
            switch (msg.what) {
                case 1:
                    myUpdate.liveUpdate();
                    sendEmptyMessageDelayed(NetworkFragment.EVENT_TICK, 1000);
                default:
            }
        }
    }

    public NetworkFragment() {
        this.mHandler = new MyInnerHandler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_network, container, false);
    }

    private void liveUpdate() {
        this.dataIpAddress.setText(ipAddressLookup());
        this.getNetSsid.setText(getSsid());
        this.macAddressInfo.setText(getMacAddress());
    }

    @Override
    public void onResume() {
        super.onResume();
        this.dataIpAddress = (TextView) getActivity().findViewById(R.id.ipAddressTxt);
        this.getNetSsid = (TextView) getActivity().findViewById(R.id.ssidTxt);
        this.macAddressInfo = (TextView) getActivity().findViewById(R.id.macAddressTxt);
        this.mHandler.sendEmptyMessageDelayed(EVENT_TICK, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mHandler.removeMessages(EVENT_TICK);
    }

    public String getSsid() {
        String netSsid = ((WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getSSID();
        if (netSsid == null) {
            return "Not available (Wi-Fi).";
        }
        return netSsid;
    }

    public String getMacAddress() {
        if (VERSION.SDK_INT >= 23) {
            try {
                String interfaceName = "wlan0";
                for (NetworkInterface intf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                    if (intf.getName().equalsIgnoreCase(interfaceName)) {
                        byte[] mac = intf.getHardwareAddress();
                        if (mac == null) {
                            return BuildConfig.FLAVOR;
                        }
                        StringBuilder buf = new StringBuilder();
                        for (byte aMac : mac) {
                            buf.append(String.format("%02X:", aMac));
                        }
                        if (buf.length() > 0) {
                            buf.deleteCharAt(buf.length() - 1);
                        }
                        return buf.toString();
                    }
                }
            } catch (Exception ignored) {
            }
            return "Not available (Wi-Fi).";
        }
        @SuppressLint("HardwareIds")
        String macAddress = ((WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .getConnectionInfo().getMacAddress();
        if (macAddress == null) {
            return "Not available (Wi-Fi).";
        }
        return macAddress;
    }

    public String ipAddressLookup() {
        String ipAddress = null;
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                Enumeration<InetAddress> enumIpAddr = (en.nextElement()).getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (ipAddress == null) {
                        ipAddress = "Not available.";
                    } else if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        ipAddress = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {
        }
        return ipAddress;
    }
}
