package com.itachi1706.cheesecakeutilities.Modules.CEPASReader.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit.Trip;

import androidx.fragment.app.FragmentActivity;

public class TripGMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String TRIP_EXTRA = "trip";
    private static final String TAG = TripGMapsActivity.class.getSimpleName();
    private Trip mTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTrip = getIntent().getParcelableExtra(TRIP_EXTRA);
        if (mTrip == null) {
            // Probably passing around an unparcelable trip
            Log.d(TAG, "Oops, couldn't display map, as we got a null trip!");
            finish();
            return;
        }
        setContentView(R.layout.activity_trip_gmaps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(Trip.formatStationNames(mTrip));
            if (mTrip.getAgencyName(false) == null && mTrip.getRouteName() != null)
                actionBar.setSubtitle(mTrip.getRouteName());
            else
            actionBar.setSubtitle((mTrip.getRouteName() == null) ? mTrip.getAgencyName(false)
                    : String.format("%s %s", mTrip.getAgencyName(false), mTrip.getRouteName()));
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap mMap = googleMap;

        // Get station coordinates
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        if (mTrip.getStartStation() != null && mTrip.getStartStation().getLatitude() != null && mTrip.getStartStation().getLongitude() != null) {
            LatLng startPos = new LatLng(Double.parseDouble(mTrip.getStartStation().getLatitude()), Double.parseDouble(mTrip.getStartStation().getLongitude()));
            mMap.addMarker(new MarkerOptions().position(startPos).title(mTrip.getStartStation().getStationName()).snippet(mTrip.getStartStation().getCompanyName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            b.include(startPos);
        }
        if (mTrip.getEndStation() != null && mTrip.getEndStation().getLatitude() != null && mTrip.getEndStation().getLongitude() != null) {
            LatLng endPos = new LatLng(Double.parseDouble(mTrip.getEndStation().getLatitude()), Double.parseDouble(mTrip.getEndStation().getLongitude()));
            mMap.addMarker(new MarkerOptions().position(endPos).title(mTrip.getEndStation().getStationName()).snippet(mTrip.getEndStation().getCompanyName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            b.include(endPos);
        }

        LatLngBounds boundary = b.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundary, 110));
    }
}
