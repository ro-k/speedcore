package com.roman.speedcore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.os.Handler;
import android.os.Looper;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<String> speed = new MutableLiveData<>();
    private final MutableLiveData<String> unit = new MutableLiveData<>();
    private final MutableLiveData<String> maxSpeed = new MutableLiveData<>();
    private final MutableLiveData<String> distance = new MutableLiveData<>();
    private final MutableLiveData<String> satelliteCount = new MutableLiveData<>();
    private final MutableLiveData<String> averageSpeed = new MutableLiveData<>();
    private final MutableLiveData<String> tripTime = new MutableLiveData<>();

    private float maxSpeedValue = 0f;
    private float distanceValue = 0f;
    private android.location.Location lastLocation;
    private long startTime = 0L;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (startTime > 0) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                tripTime.setValue(formatTime(elapsedTime));
                handler.postDelayed(this, 1000);
            }
        }
    };

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

    public LiveData<String> getAverageSpeed() {
        return averageSpeed;
    }

    public LiveData<String> getTripTime() {
        return tripTime;
    }

    public void onLocationUpdate(android.location.Location location, boolean isMetric) {
        if (startTime == 0L) {
            startTime = System.currentTimeMillis();
            handler.post(timerRunnable);
        }

        float speedInMetersPerSecond = location.getSpeed();
        float currentSpeed = speedInMetersPerSecond * (isMetric ? 3.6f : 2.23694f);
        speed.setValue(formatNumber(currentSpeed));

        if (currentSpeed > maxSpeedValue) {
            maxSpeedValue = currentSpeed;
            maxSpeed.setValue(formatMaxSpeed(maxSpeedValue, isMetric));
        }

        if (lastLocation != null) {
            distanceValue += location.distanceTo(lastLocation);
            distance.setValue(formatDistance(distanceValue / (isMetric ? 1000 : 1609.34), isMetric));
        }
        lastLocation = location;

        long elapsedTime = System.currentTimeMillis() - startTime;
        if (elapsedTime > 0) {
            float avgSpeedValue = (distanceValue / (elapsedTime / 1000f)) * (isMetric ? 3.6f : 2.23694f);
            averageSpeed.setValue(formatAverageSpeed(avgSpeedValue, isMetric));
        }
    }

    public void onSatelliteStatusChanged(int satelliteCountValue, int usedInFixCount) {
        satelliteCount.setValue(String.format(java.util.Locale.getDefault(), "Satellites: %d (%d used)", satelliteCountValue, usedInFixCount));
    }

    public void onSettingsChanged(boolean isMetric) {
        unit.setValue(isMetric ? "km/h" : "mph");
        maxSpeed.setValue(formatMaxSpeed(maxSpeedValue, isMetric));
        distance.setValue(formatDistance(distanceValue / (isMetric ? 1000 : 1609.34), isMetric));
    }
    
    public void resetTrip() {
        speed.setValue("0");
        maxSpeedValue = 0f;
        distanceValue = 0f;
        lastLocation = null;
        startTime = 0L;
        maxSpeed.setValue(formatMaxSpeed(0, false));
        distance.setValue(formatDistance(0, false));
        averageSpeed.setValue(formatAverageSpeed(0, false));
        tripTime.setValue(formatTime(0L));
        handler.removeCallbacks(timerRunnable);
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

    public String formatAverageSpeed(double value, boolean isMetric) {
        return "Avg: " + formatNumber(value) + (isMetric ? " km/h" : " mph");
    }

    public String formatTime(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60));
        return String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        handler.removeCallbacks(timerRunnable);
    }
}
