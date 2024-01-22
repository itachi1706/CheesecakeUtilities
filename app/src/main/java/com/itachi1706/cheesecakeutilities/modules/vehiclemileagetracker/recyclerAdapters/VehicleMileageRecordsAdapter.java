package com.itachi1706.cheesecakeutilities.modules.vehiclemileagetracker.recyclerAdapters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.itachi1706.helperlib.helpers.PrefHelper;
import com.itachi1706.cheesecakeutilities.modules.vehiclemileagetracker.AddNewMileageRecordActivity;
import com.itachi1706.cheesecakeutilities.modules.vehiclemileagetracker.objects.Record;
import com.itachi1706.cheesecakeutilities.modules.vehiclemileagetracker.objects.Vehicle;
import com.itachi1706.cheesecakeutilities.modules.vehiclemileagetracker.VehMileageFirebaseUtils;
import com.itachi1706.cheesecakeutilities.R;
import com.turingtechnologies.materialscrollbar.IDateableAdapter;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.itachi1706.cheesecakeutilities.modules.vehiclemileagetracker.VehMileageFirebaseUtils.FB_REC_RECORDS;
import static com.itachi1706.cheesecakeutilities.modules.vehiclemileagetracker.VehMileageFirebaseUtils.FB_REC_USER;
import static com.itachi1706.cheesecakeutilities.util.FirebaseUtils.Companion;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.modules.ListApplications.RecyclerAdapters in Cheesecake Utilities.
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
    public void onBindViewHolder(@NonNull VehicleMileageRecordsViewHolder recordsViewHolder, int i) {
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
        distanceTime += " (" + Companion.parseData(s.getTotalMileage(), decimal) + " km)";
        recordsViewHolder.totalTimeDistance.setText(distanceTime);
        if (s.getTrainingMileage()) recordsViewHolder.totalTimeDistance.setTextColor(Color.RED);
        else recordsViewHolder.totalTimeDistance.setTextColor(recordsViewHolder.defaultTextColor);
        recordsViewHolder.datetime.setText(VehMileageFirebaseUtils.Companion.formatTimeDuration(s.getDatetimeFrom(), s.getDateTimeTo()));
        recordsViewHolder.datetime.setSelected(true);
    }

    @Override
    @NonNull
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

    public String getItemTag(int position) {
        return (this.hideTraining) ? hiddenTags.get(position) : tags.get(position);
    }

    public Record getRecord(int position) {
        return (this.hideTraining) ? hidden.get(position) : recordsList.get(position);
    }


    class VehicleMileageRecordsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView location, purpose, vehicle, vehicleNumber, totalTimeDistance, datetime;
        int defaultTextColor;
        String tag, fullVehicleName;
        Record r;

        VehicleMileageRecordsViewHolder(View v) {
            super(v);
            defaultTextColor = ContextCompat.getColor(v.getContext(), (PrefHelper.isNightModeEnabled(v.getContext())) ? R.color.default_text_color_sec_dark : R.color.default_text_color_sec_light);
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
            message += "From: " + VehMileageFirebaseUtils.Companion.formatTime(r.getDatetimeFrom()) + " hrs\n";
            message += "To: " + VehMileageFirebaseUtils.Companion.formatTime(r.getDateTimeTo()) + " hrs\n";
            message += "Time Taken: " + DurationFormatUtils.formatDurationWords(r.getTotalTimeInMs(), true, true) + "\n";
            message += "Mileage From: " + Companion.parseData(r.getMileageFrom(), decimal) + " km\n";
            message += "Mileage To: " + Companion.parseData(r.getMileageTo(), decimal) + " km\n";
            message += "Total Mileage: " + Companion.parseData(r.getTotalMileage(), decimal) + " km\n";
            message += "Training Mileage: " + ((r.getTrainingMileage()) ? "true" : "false") + "\n";
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Mileage Record")
                    .setMessage(message).setPositiveButton(com.itachi1706.appupdater.R.string.dialog_action_positive_close, null)
                    .setNeutralButton("Delete", (dialog, which) -> {
                                SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(v.getContext());
                                DatabaseReference ref = VehMileageFirebaseUtils.getVehicleMileageDatabase().child(FB_REC_USER)
                                        .child(VehMileageFirebaseUtils.getFirebaseUIDFromSharedPref(sp)).child(FB_REC_RECORDS).child(tag);
                                ref.removeValue();
                                Snackbar.make(v, "Record Deleted", Snackbar.LENGTH_LONG).setAction("Undo", v2 -> {
                                    ref.setValue(r);
                                    Snackbar.make(v2, "Delete Undone", Snackbar.LENGTH_SHORT).show();
                                }).show();
                            }).setNegativeButton("Edit", (dialogInterface, i) -> {
                        SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(v.getContext());
                        String uid = sp.getString(VehMileageFirebaseUtils.FB_UID, "");
                        Intent intent = new Intent(v.getContext(), AddNewMileageRecordActivity.class);
                        intent.putExtra("edit", tag);
                        if (!uid.isEmpty()) intent.putExtra("uid", uid);
                        v.getContext().startActivity(intent);
                    }).show();
        }
    }
}
