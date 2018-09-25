package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.RecyclerAdapters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.AddNewMileageRecordActivity;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Record;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Vehicle;
import com.itachi1706.cheesecakeutilities.R;
import com.turingtechnologies.materialscrollbar.IDateableAdapter;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils.FB_REC_RECORDS;
import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils.FB_REC_USER;
import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils.parseData;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.Modules.ListApplications.RecyclerAdapters in Cheesecake Utilities.
 */
public class VehicleMileageRecordsAdapter extends RecyclerView.Adapter<VehicleMileageRecordsAdapter.VehicleMileageRecordsViewHolder> implements IDateableAdapter {
    private List<Record> recordsList, hidden;
    private List<String> tags, hiddenTags;
    private DataSnapshot vehicles;
    private boolean hideTraining = false;
    private boolean decimal;

    public VehicleMileageRecordsAdapter(List<Record> recordList, List<String> tags, DataSnapshot vehicles, boolean decimal) {
        this.recordsList = recordList;
        this.tags = tags;
        this.vehicles = vehicles;
        this.decimal = decimal;
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
    public int getItemCount() {
        return (this.hideTraining) ? hidden.size() : recordsList.size();
    }

    @Override
    public void onBindViewHolder(VehicleMileageRecordsViewHolder recordsViewHolder, int i) {
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
        } else {
            Vehicle v = vehicles.child(s.getVehicleClass()).child(s.getVehicleId()).getValue(Vehicle.class);
            if (v == null) recordsViewHolder.vehicle.setText("Unknown Vehicle");
            else recordsViewHolder.vehicle.setText(v.getShortname());
            recordsViewHolder.fullVehicleName = (v == null) ? "Unknown Vehicle" : v.getName();
        }
        recordsViewHolder.vehicleNumber.setText(s.getVehicleNumber());
        // Calculate distance time
        String distanceTime = DurationFormatUtils.formatDurationWords(s.getTotalTimeInMs(), true, true);
        distanceTime += " (" + parseData(s.getTotalMileage(), decimal) + " km)";
        recordsViewHolder.totalTimeDistance.setText(distanceTime);
        if (s.getTrainingMileage()) recordsViewHolder.totalTimeDistance.setTextColor(Color.RED);
        else recordsViewHolder.totalTimeDistance.setTextColor(recordsViewHolder.defaultTextColor);
        recordsViewHolder.datetime.setText(FirebaseUtils.formatTimeDuration(s.getDatetimeFrom(), s.getDateTimeTo()));
        recordsViewHolder.datetime.setSelected(true);
    }

    @Override
    public VehicleMileageRecordsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_vehicle_mileage_record, viewGroup, false);

        return new VehicleMileageRecordsViewHolder(itemView);
    }

    @Override
    public Date getDateForElement(int element) {
        return new Date(recordsList.get(element).getDatetimeFrom());
    }


    class VehicleMileageRecordsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView location, purpose, vehicle, vehicleNumber, totalTimeDistance, datetime;
        int defaultTextColor;
        String tag, fullVehicleName;
        Record r;

        VehicleMileageRecordsViewHolder(View v) {
            super(v);
            defaultTextColor = ContextCompat.getColor(v.getContext(), R.color.default_text_color_sec);
            location = v.findViewById(R.id.tvLocation);
            purpose = v.findViewById(R.id.tvPurpose);
            vehicle = v.findViewById(R.id.tvVehicle);
            datetime = v.findViewById(R.id.tvDateTime);
            vehicleNumber = v.findViewById(R.id.tvVehicleNumber);
            totalTimeDistance = v.findViewById(R.id.tvTotalTimeDistance);
            datetime.setHorizontallyScrolling(true);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String message = "";
            message += "Location: " + r.getDestination() + "\n";
            message += "Purpose: " + r.getPurpose() + "\n";
            message += "Vehicle: " + vehicle.getText().toString() + "\n";
            message += "Vehicle Full Name: " + fullVehicleName + "\n";
            message += "Vehicle License Plate: " + r.getVehicleNumber() + "\n";
            message += "From: " + FirebaseUtils.formatTime(r.getDatetimeFrom()) + " hrs\n";
            message += "To: " + FirebaseUtils.formatTime(r.getDateTimeTo()) + " hrs\n";
            message += "Time Taken: " + DurationFormatUtils.formatDurationWords(r.getTotalTimeInMs(), true, true) + "\n";
            message += "Mileage From: " + parseData(r.getMileageFrom(), decimal) + " km\n";
            message += "Mileage To: " + parseData(r.getMileageTo(), decimal) + " km\n";
            message += "Total Mileage: " + parseData(r.getTotalMileage(), decimal) + " km\n";
            message += "Training Mileage: " + ((r.getTrainingMileage()) ? "true" : "false") + "\n";
            final View v1 = v;
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Mileage Record")
                    .setMessage(message).setPositiveButton(R.string.dialog_action_positive_close, null)
                    .setNeutralButton("Delete", (dialog, which) -> new AlertDialog.Builder(v1.getContext()).setTitle("Deleting Mileage Record")
                            .setMessage("Are you sure you want to delete this record? This cannot be reversed!" +
                                    "\nID: " + tag)
                            .setPositiveButton("Delete Anyway", (dialog1, which1) -> {
                                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(v1.getContext());
                                FirebaseUtils.getFirebaseDatabase().getReference().child(FB_REC_USER)
                                        .child(sp.getString("firebase_uid", "nien")).child(FB_REC_RECORDS)
                                        .child(tag).removeValue();
                                Toast.makeText(v1.getContext(), "Deleted record", Toast.LENGTH_SHORT).show();

                            }).setNegativeButton("No", null).show()).setNegativeButton("Edit", (dialogInterface, i) -> {
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(v1.getContext());
                        String uid = sp.getString("firebase_uid", "");
                        Intent intent = new Intent(v1.getContext(), AddNewMileageRecordActivity.class);
                        intent.putExtra("edit", tag);
                        if (!uid.isEmpty()) intent.putExtra("uid", uid);
                        v1.getContext().startActivity(intent);
                    }).show();
        }
    }
}
