package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.RecyclerAdapters;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Record;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Vehicle;
import com.itachi1706.cheesecakeutilities.R;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.List;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.Modules.ListApplications.RecyclerAdapters in Cheesecake Utilities.
 */
public class VehicleMileageRecordsAdapter extends RecyclerView.Adapter<VehicleMileageRecordsAdapter.VehicleMileageRecordsViewHolder> {
    private List<Record> recordsList;
    private DataSnapshot vehicles;

    public VehicleMileageRecordsAdapter(List<Record> recordList, DataSnapshot vehicles)
    {
        this.recordsList = recordList;
        this.vehicles = vehicles;
    }

    public void updateRecords(List<Record> records) {
        this.recordsList = records;
    }

    public void updateSnapshot(DataSnapshot vehicles) {
        this.vehicles = vehicles;
    }

    @Override
    public int getItemCount()
    {
        return recordsList.size();
    }

    @Override
    public void onBindViewHolder(VehicleMileageRecordsViewHolder recordsViewHolder, int i)
    {
        Record s = recordsList.get(i);
        recordsViewHolder.location.setText(s.getDestination());
        recordsViewHolder.purpose.setText(s.getPurpose());
        if (s.getVehicleId().isEmpty()) recordsViewHolder.vehicle.setText("Unknown Vehicle");
        else {
            Vehicle v = vehicles.child(s.getVehicleClass()).child(s.getVehicleId()).getValue(Vehicle.class);
            if (v == null) recordsViewHolder.vehicle.setText("Unknown Vehicle");
            else recordsViewHolder.vehicle.setText(v.getName());
        }
        recordsViewHolder.vehicleNumber.setText(s.getVehicleNumber());
        // Calculate distance time
        String distanceTime = DurationFormatUtils.formatDurationWords(s.getTotalTimeInMs(), true, true);
        distanceTime += " (" + s.getTotalMileage() + " km)";
        recordsViewHolder.totalTimeDistance.setText(distanceTime);
        if (s.getTrainingMileage()) recordsViewHolder.totalTimeDistance.setTextColor(Color.RED);
        else recordsViewHolder.totalTimeDistance.setTextColor(recordsViewHolder.defaultTextColor);
    }

    @Override
    public VehicleMileageRecordsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_vehicle_mileage_record, viewGroup, false);

        return new VehicleMileageRecordsViewHolder(itemView);
    }


    class VehicleMileageRecordsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView location, purpose, vehicle, vehicleNumber, totalTimeDistance;
        int defaultTextColor;

        VehicleMileageRecordsViewHolder(View v)
        {
            super(v);
            defaultTextColor = ContextCompat.getColor(v.getContext(), R.color.default_text_color_sec);
            location = (TextView) v.findViewById(R.id.tvLocation);
            purpose = (TextView) v.findViewById(R.id.tvPurpose);
            vehicle = (TextView) v.findViewById(R.id.tvVehicle);
            vehicleNumber = (TextView) v.findViewById(R.id.tvVehicleNumber);
            totalTimeDistance = (TextView) v.findViewById(R.id.tvTotalTimeDistance);
            location.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            location.setMarqueeRepeatLimit(-1);
            location.setHorizontallyScrolling(true);
            purpose.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            purpose.setMarqueeRepeatLimit(-1);
            purpose.setHorizontallyScrolling(true);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // TODO: Code Stub Show Dialog box with details of record
            // TODO: Include ability to edit/delete record
            Toast.makeText(v.getContext(), "Unimplemented", Toast.LENGTH_SHORT).show();
        }
    }
}
