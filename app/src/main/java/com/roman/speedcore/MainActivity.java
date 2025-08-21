package com.roman.speedcore;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.content.Context;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import android.location.GnssStatus;
import java.util.Locale;

import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    private boolean isMetric = false; // false = mph/mi, true = km/h/km

    private TextView speedText;
    private TextView unitText;
    private TextView maxSpeedText;
    private TextView distanceText;
    private TextView satelliteCountText;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    private LocationCallback locationCallback;
    private GnssStatus.Callback gnssStatusCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speedText = findViewById(R.id.speed_text_view);
        unitText = findViewById(R.id.speed_unit_text_view);
        maxSpeedText = findViewById(R.id.max_speed_text_view);
        distanceText = findViewById(R.id.distance_text_view);
        satelliteCountText = findViewById(R.id.satellite_count_text_view);

        findViewById(R.id.reset_button).setOnClickListener(v -> resetTrip());
        findViewById(R.id.switch_units_button).setOnClickListener(v -> toggleUnits());

        MaterialToolbar toolbar = findViewById(R.id.top_app_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_reset) {
                    resetTrip();
                    return true;
                } else if (id == R.id.action_units) {
                    toggleUnits();
                    return true;
                } else if (id == R.id.action_show_satellites) {
                    Toast.makeText(this, "Show Satellites clicked (TODO: Implement)", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    if (location.hasSpeed()) {
                        float currentSpeed = location.getSpeed() * (isMetric ? 3.6f : 2.23694f); // m/s to km/h or mph
                        speedText.setText(formatNumber(currentSpeed));
                        // TODO: Update max speed and distance
                    }
                }
            }
        };

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
                satelliteCountText.setText(String.format(Locale.getDefault(), "Satellites: %d (%d used)", satelliteCount, usedInFixCount));
            }
        };
        checkLocationPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.unregisterGnssStatusCallback(gnssStatusCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLocationPermission(); // This will start location updates if permission is granted
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.registerGnssStatusCallback(getMainExecutor(), gnssStatusCallback);
        }
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private void checkLocationPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, proceed with location updates
            startLocationUpdates();
        } else {
            // Permission not granted, request it
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with location updates
                startLocationUpdates();
            } else {
                // Permission denied, inform the user
                Toast.makeText(this, "Location permission denied. Speedometer cannot function.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // 1 second interval
        locationRequest.setFastestInterval(500); // 0.5 second fastest interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
            Toast.makeText(this, "Location updates started.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location permission not granted. Cannot start updates.", Toast.LENGTH_LONG).show();
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Toast.makeText(this, "Location updates stopped.", Toast.LENGTH_SHORT).show();
    }

    private void resetTrip() {
        // Reset displayed values; hook into your real logic as needed.
        speedText.setText("0");
        double zero = 0.0;
        maxSpeedText.setText(formatMaxSpeed(zero));
        distanceText.setText(formatDistance(zero));
        Toast.makeText(this, "Trip reset", Toast.LENGTH_SHORT).show();
    }

    private void toggleUnits() {
        isMetric = !isMetric;
        unitText.setText(isMetric ? "km/h" : "mph");

        // Convert currently displayed values for continuity.
        try {
            double currentSpeed = Double.parseDouble(speedText.getText().toString());
            speedText.setText(formatNumber(convertSpeed(currentSpeed, !isMetric)));
        } catch (NumberFormatException ignored) { }

        double currentMax = parseLabeledNumber(maxSpeedText.getText().toString());
        maxSpeedText.setText(formatMaxSpeed(convertSpeed(currentMax, !isMetric)));

        double currentDist = parseLabeledNumber(distanceText.getText().toString());
        distanceText.setText(formatDistance(convertDistance(currentDist, !isMetric)));

        Toast.makeText(this, isMetric ? "Units: km/h" : "Units: mph", Toast.LENGTH_SHORT).show();
    }

    private String formatMaxSpeed(double value) {
        return "Max: " + formatNumber(value) + (isMetric ? " km/h" : " mph");
    }

    private String formatDistance(double value) {
        return "Dist: " + formatNumber(value) + (isMetric ? " km" : " mi");
    }

    private String formatNumber(double value) {
        // Keep it simple; no trailing .0 for integers
        if (Math.abs(value - Math.round(value)) < 1e-9) {
            return String.valueOf((long) Math.round(value));
        }
        return String.format(java.util.Locale.getDefault(), "%.1f", value);
    }

    private double parseLabeledNumber(String labeled) {
        // Extract first number in string like "Max: 12.3 mph"
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(-?\\d+(?:[.,]\\d+)?)").matcher(labeled);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1).replace(',', '.'));
            } catch (NumberFormatException ignored) { }
        }
        return 0.0;
    }

    private double convertSpeed(double value, boolean toMetric) {
        // mph <-> km/h (1 mph = 1.60934 km/h)
        return toMetric ? value * 1.60934 : value / 1.60934;
    }

    private double convertDistance(double value, boolean toMetric) {
        // miles <-> km (1 mi = 1.60934 km)
        return convertSpeed(value, toMetric);
    }
}
