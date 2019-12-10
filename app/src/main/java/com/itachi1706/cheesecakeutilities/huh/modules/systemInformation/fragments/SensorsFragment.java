package com.itachi1706.cheesecakeutilities.huh.modules.systemInformation.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.itachi1706.cheesecakeutilities.BuildConfig;
import com.itachi1706.cheesecakeutilities.R;

import java.util.ArrayList;
import java.util.List;

public class SensorsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_sensors, container, false);
        SensorManager mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = mSensorManager.getSensorList(-1);
        ListView list = view.findViewById(R.id.sensorList1);
        ArrayList<String> items = new ArrayList<>();
        for (Sensor s : sensorList) {
            String item = s.getName() + ": " + s.getVendor();
            if (!item.equals(BuildConfig.FLAVOR)) {
                items.add(item);
            }
        }
        list.setAdapter(new ArrayAdapter<>(getActivity().getBaseContext(), android.R.layout.simple_list_item_1, items));
        return view;
    }
}
