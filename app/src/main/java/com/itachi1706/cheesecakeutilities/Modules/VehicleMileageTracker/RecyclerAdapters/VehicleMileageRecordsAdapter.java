package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.RecyclerAdapters;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Record;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Vehicle;
import com.itachi1706.cheesecakeutilities.R;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.Modules.ListApplications.RecyclerAdapters in Cheesecake Utilities.
 */
public class VehicleMileageRecordsAdapter extends RecyclerView.Adapter<VehicleMileageRecordsAdapter.VehicleMileageRecordsViewHolder> {
    private List<Record> recordsList, hidden;
    private List<String> tags, hiddenTags;
    private DataSnapshot vehicles;
    private boolean hideTraining = false;

    public VehicleMileageRecordsAdapter(List<Record> recordList, List<String> tags, DataSnapshot vehicles)
    {
        this.recordsList = recordList;
        this.tags = tags;
        this.vehicles = vehicles;
    }

    public void updateRecords(List<Record> records, List<String> tags) {
        this.recordsList = records;
        this.tags = tags;
    }

    public void updateSnapshot(DataSnapshot vehicles) {
        this.vehicles = vehicles;
    }

    public void setHideTraining(boolean hide) {
        this.hideTraining = hide;
        if (this.hideTraining) {
            if (hidden == null) hidden = new ArrayList<>();
            hidden.clear();
            if (hiddenTags == null) hiddenTags = new ArrayList<>();
            hiddenTags.clear();
            for (int i = 0; i < recordsList.size(); i++) {
                Record r = recordsList.get(i);
                if (r.getTrainingMileage()) continue;
                hidden.add(r);
                hiddenTags.add(tags.get(i));
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return (this.hideTraining) ? hidden.size() : recordsList.size();
    }

    @Override
    public void onBindViewHolder(VehicleMileageRecordsViewHolder recordsViewHolder, int i)
    {
        Record s;
        if (this.hideTraining) s = hidden.get(i);
        else s = recordsList.get(i);
        recordsViewHolder.tag = (this.hideTraining) ? hiddenTags.get(i) : tags.get(i);
        recordsViewHolder.r = s;
        recordsViewHolder.location.setText(s.getDestination());
        recordsViewHolder.purpose.setText(s.getPurpose());
        if (s.getVehicleId().isEmpty()) {
            recordsViewHolder.vehicle.setText("Unknown Vehicle");
            recordsViewHolder.fullVehicleName = "Unknown Vehicle";
        }
        else {
            Vehicle v = vehicles.child(s.getVehicleClass()).child(s.getVehicleId()).getValue(Vehicle.class);
            if (v == null) recordsViewHolder.vehicle.setText("Unknown Vehicle");
            else recordsViewHolder.vehicle.setText(v.getShortname());
            recordsViewHolder.fullVehicleName = (v == null) ? "Unknown Vehicle" : v.getName();
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
        String tag, fullVehicleName;
        Record r;

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
            // TODO: Include ability to edit record
            String message = "";
            message += "Location: " + r.getDestination() + "\n";
            message += "Purpose: " + r.getPurpose() + "\n";
            message += "Vehicle: " + vehicle.getText().toString() + "\n";
            message += "Vehicle Full Name: " + fullVehicleName + "\n";
            message += "Vehicle License Plate: " + r.getVehicleNumber() + "\n";
            message += "From: " + FirebaseUtils.formatTime(r.getDatetimeFrom()) + " hrs\n";
            message += "To: " + FirebaseUtils.formatTime(r.getDateTimeTo()) + " hrs\n";
            message += "Time Taken: " + DurationFormatUtils.formatDurationWords(r.getTotalTimeInMs(), true, true) + "\n";
            message += "Mileage From: " + r.getMileageFrom() + " km\n";
            message += "Mileage To: " + r.getMileageTo() + " km\n";
            message += "Total Mileage: " + r.getTotalMileage() + " km\n";
            message += "Training Mileage: " + ((r.getTrainingMileage()) ? "true" : "false") + "\n";
            final View v1 = v;
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Mileage Record")
                    .setMessage(message).setPositiveButton(R.string.dialog_action_positive_close, null)
                    .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new AlertDialog.Builder(v1.getContext()).setTitle("Deleting Mileage Record")
                                    .setMessage("Are you sure you want to delete this record? This cannot be reversed!" +
                                            "\nID: " + tag)
                            .setPositiveButton("Delete Anyway", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(v1.getContext());
                                    FirebaseUtils.getFirebaseDatabase().getReference().child("users")
                                            .child(sp.getString("firebase_uid", "nien")).child("records")
                                            .child(tag).removeValue();
                                    Toast.makeText(v1.getContext(), "Deleted record", Toast.LENGTH_SHORT).show();

                                }
                            }).setNegativeButton("No", null).show();
                        }
                    }).show();
        }
    }
}
