package com.roman.speedcore;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class MainViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private MainViewModel viewModel;

    @Before
    public void setup() {
        viewModel = new MainViewModel();
    }

    @Test
    public void formatNumber_withInteger_returnsIntegerString() {
        assertEquals("10", viewModel.formatNumber(10.0));
    }

    @Test
    public void formatNumber_withDecimal_returnsDecimalString() {
        assertEquals("10.5", viewModel.formatNumber(10.5));
    }

    @Test
    public void formatNumber_withZero_returnsZeroString() {
        assertEquals("0", viewModel.formatNumber(0.0));
    }

    @Test
    public void formatNumber_withNegativeInteger_returnsNegativeIntegerString() {
        assertEquals("-10", viewModel.formatNumber(-10.0));
    }

    @Test
    public void formatNumber_withNegativeDecimal_returnsNegativeDecimalString() {
        assertEquals("-10.5", viewModel.formatNumber(-10.5));
    }

    @Test
    public void formatNumber_withRounding_returnsRoundedString() {
        assertEquals("10.1", viewModel.formatNumber(10.09));
        assertEquals("10.9", viewModel.formatNumber(10.94));
    }

    @Test
    public void parseLabeledNumber_withValidInput_returnsNumber() {
        assertEquals(12.3, viewModel.parseLabeledNumber("Max: 12.3 mph"), 0.001);
        assertEquals(-45.6, viewModel.parseLabeledNumber("Dist: -45,6 km"), 0.001);
        assertEquals(7, viewModel.parseLabeledNumber("Speed: 7"), 0.001);
    }

    @Test
    public void parseLabeledNumber_withInvalidInput_returnsZero() {
        assertEquals(0.0, viewModel.parseLabeledNumber("Max: -- mph"), 0.001);
        assertEquals(0.0, viewModel.parseLabeledNumber("Dist: abc km"), 0.001);
        assertEquals(0.0, viewModel.parseLabeledNumber(""), 0.001);
    }

    @Test
    public void onLocationUpdate_withMetric_updatesSpeed() {
        viewModel.onLocationUpdate(10, true); // 10 m/s = 36 km/h
        assertEquals("36", viewModel.getSpeed().getValue());
    }

    @Test
    public void onLocationUpdate_withImperial_updatesSpeed() {
        viewModel.onLocationUpdate(10, false); // 10 m/s = 22.4 mph
        assertEquals("22.4", viewModel.getSpeed().getValue());
    }

    @Test
    public void onSatelliteStatusChanged_updatesSatelliteCount() {
        viewModel.onSatelliteStatusChanged(10, 5);
        assertEquals("Satellites: 10 (5 used)", viewModel.getSatelliteCount().getValue());
    }

    @Test
    public void onSettingsChanged_toMetric_updatesUnit() {
        viewModel.onSettingsChanged(true);
        assertEquals("km/h", viewModel.getUnit().getValue());
    }

    @Test
    public void onSettingsChanged_toImperial_updatesUnit() {
        viewModel.onSettingsChanged(false);
        assertEquals("mph", viewModel.getUnit().getValue());
    }

    @Test
    public void resetTrip_resetsValues() {
        viewModel.resetTrip();
        assertEquals("0", viewModel.getSpeed().getValue());
        assertEquals("Max: 0 mph", viewModel.getMaxSpeed().getValue());
        assertEquals("Dist: 0 mi", viewModel.getDistance().getValue());
    }
}
