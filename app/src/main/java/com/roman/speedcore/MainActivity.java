package com.roman.speedcore;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.Locale;
import android.location.GnssStatus;
import android.os.Build;
import androidx.annotation.RequiresApi;

public class MainActivity extends AppCompatActivity implements SettingsDialogFragment.SettingsDialogListener {

    private MainViewModel viewModel;

    private TextView speedText;
    private TextView unitText;
    private TextView maxSpeedText;
    private TextView distanceText;
    private TextView satelliteCountText;
    private TextView avgSpeedText;
    private TextView tripTimeText;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    private LocationCallback locationCallback;
    @RequiresApi(Build.VERSION_CODES.N)
    private GnssStatus.Callback gnssStatusCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        speedText = findViewById(R.id.speed_text_view);
        unitText = findViewById(R.id.speed_unit_text_view);
        maxSpeedText = findViewById(R.id.max_speed_text_view);
        distanceText = findViewById(R.id.distance_text_view);
        satelliteCountText = findViewById(R.id.satellite_count_text_view);
        avgSpeedText = findViewById(R.id.avg_speed_text_view);
        tripTimeText = findViewById(R.id.trip_time_text_view);

        findViewById(R.id.reset_button).setOnClickListener(v -> viewModel.resetTrip());
        findViewById(R.id.switch_units_button).setVisibility(View.GONE);

        MaterialToolbar toolbar = findViewById(R.id.top_app_bar);
        setSupportActionBar(toolbar);

        setupObservers();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        createLocationCallback();
        createGnssStatusCallback();

        checkLocationPermission();
        loadAndApplySettings();
    }

    private void setupObservers() {
        viewModel.getSpeed().observe(this, speedText::setText);
        viewModel.getUnit().observe(this, unitText::setText);
        viewModel.getMaxSpeed().observe(this, maxSpeedText::setText);
        viewModel.getDistance().observe(this, distanceText::setText);
        viewModel.getSatelliteCount().observe(this, satelliteCountText::setText);
        viewModel.getAverageSpeed().observe(this, avgSpeedText::setText);
        viewModel.getTripTime().observe(this, tripTimeText::setText);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location.hasSpeed()) {
                        SharedPreferences sharedPreferences = getSharedPreferences(SettingsDialogFragment.PREFS_NAME, Context.MODE_PRIVATE);
                        boolean isMetric = sharedPreferences.getBoolean(SettingsDialogFragment.KEY_IS_METRIC, false);
                        viewModel.onLocationUpdate(location, isMetric);
                    }
                }
            }
        };
    }

    private void createGnssStatusCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            gnssStatusCallback = new GnssStatus.Callback() {
                @Override
                public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                    int satelliteCount = status.getSatelliteCount();
                    int usedInFixCount = 0;
                    for (int i = 0; i < satelliteCount; i++) {
                        if (status.usedInFix(i)) {
                            usedInFixCount++;
                        }
                    }
                    viewModel.onSatelliteStatusChanged(satelliteCount, usedInFixCount);
                }
            };
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLocationPermission();
        loadAndApplySettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            new SettingsDialogFragment().show(getSupportFragmentManager(), SettingsDialogFragment.TAG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSettingsChanged() {
        loadAndApplySettings();
    }

    private void loadAndApplySettings() {
        SharedPreferences sharedPreferences = getSharedPreferences(SettingsDialogFragment.PREFS_NAME, Context.MODE_PRIVATE);
        boolean isMetric = sharedPreferences.getBoolean(SettingsDialogFragment.KEY_IS_METRIC, false);
        boolean keepScreenOn = sharedPreferences.getBoolean(SettingsDialogFragment.KEY_KEEP_SCREEN_ON, false);
        boolean showSatellites = sharedPreferences.getBoolean(SettingsDialogFragment.KEY_SHOW_SATELLITES, true);

        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        satelliteCountText.setVisibility(showSatellites ? View.VISIBLE : View.GONE);
        viewModel.onSettingsChanged(isMetric);
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private void checkLocationPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied. Speedometer cannot function.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
            registerGnssStatusCallback();
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        unregisterGnssStatusCallback();
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private void registerGnssStatusCallback() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.registerGnssStatusCallback(ContextCompat.getMainExecutor(this), gnssStatusCallback);
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private void unregisterGnssStatusCallback() {
        locationManager.unregisterGnssStatusCallback(gnssStatusCallback);
    }
}
