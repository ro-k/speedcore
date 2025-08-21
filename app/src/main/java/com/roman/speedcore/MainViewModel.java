package com.roman.speedcore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<String> speed = new MutableLiveData<>();
    private final MutableLiveData<String> unit = new MutableLiveData<>();
    private final MutableLiveData<String> maxSpeed = new MutableLiveData<>();
    private final MutableLiveData<String> distance = new MutableLiveData<>();
    private final MutableLiveData<String> satelliteCount = new MutableLiveData<>();

    public LiveData<String> getSpeed() {
        return speed;
    }

    public LiveData<String> getUnit() {
        return unit;
    }

    public LiveData<String> getMaxSpeed() {
        return maxSpeed;
    }

    public LiveData<String> getDistance() {
        return distance;
    }

    public LiveData<String> getSatelliteCount() {
        return satelliteCount;
    }

    public void onLocationUpdate(float speedInMetersPerSecond, boolean isMetric) {
        float currentSpeed = speedInMetersPerSecond * (isMetric ? 3.6f : 2.23694f);
        speed.setValue(formatNumber(currentSpeed));
        // TODO: Update max speed and distance
    }

    public void onSatelliteStatusChanged(int satelliteCountValue, int usedInFixCount) {
        satelliteCount.setValue(String.format(java.util.Locale.getDefault(), "Satellites: %d (%d used)", satelliteCountValue, usedInFixCount));
    }

    public void onSettingsChanged(boolean isMetric) {
        unit.setValue(isMetric ? "km/h" : "mph");
        // TODO: update max speed and distance with new units
    }
    
    public void resetTrip() {
        speed.setValue("0");
        maxSpeed.setValue(formatMaxSpeed(0, false));
        distance.setValue(formatDistance(0, false));
    }

    public String formatNumber(double value) {
        if (Math.abs(value - Math.round(value)) < 1e-9) {
            return String.valueOf((long) Math.round(value));
        }
        return String.format(java.util.Locale.getDefault(), "%.1f", value);
    }

    public double parseLabeledNumber(String labeled) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(-?\\d+(?:[.,]\\d+)?+)").matcher(labeled);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1).replace(',', '.'));
            } catch (NumberFormatException ignored) { }
        }
        return 0.0;
    }
    
    public String formatMaxSpeed(double value, boolean isMetric) {
        return "Max: " + formatNumber(value) + (isMetric ? " km/h" : " mph");
    }

    public String formatDistance(double value, boolean isMetric) {
        return "Dist: " + formatNumber(value) + (isMetric ? " km" : " mi");
    }
}
