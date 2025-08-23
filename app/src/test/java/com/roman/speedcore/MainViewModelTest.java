package com.roman.speedcore;

import android.location.Location;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE) // no AndroidManifest needed
public class MainViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private MainViewModel viewModel;

    @Mock
    Object unused; // if you actually need mocks, keep them; otherwise remove

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this); // since we’re not using MockitoJUnitRunner
        viewModel = new MainViewModel();    // Robolectric provides Looper.getMainLooper()
    }

    private Location createLocation(double latitude, double longitude, float speed) {
        Location location = mock(Location.class);
        when(location.getLatitude()).thenReturn(latitude);
        when(location.getLongitude()).thenReturn(longitude);
        when(location.getSpeed()).thenReturn(speed);
        when(location.distanceTo(org.mockito.ArgumentMatchers.any(Location.class))).thenReturn(0f);
        return location;
    }

    @Test
    public void resetTrip_resetsValues() {
        Location location = createLocation(10, 0, 0);
        viewModel.onLocationUpdate(location, false);
        viewModel.resetTrip();
        assertEquals("0", viewModel.getSpeed().getValue());
        assertEquals("Max: 0 mph", viewModel.getMaxSpeed().getValue());
        assertEquals("Dist: 0 mi", viewModel.getDistance().getValue());
    }

    @Test
    public void onCompassChanged_updatesCompassHeading() {
        viewModel.onCompassChanged(90f);
        assertEquals(90f, viewModel.getCompassHeading().getValue(), 0.001f);
    }
}
