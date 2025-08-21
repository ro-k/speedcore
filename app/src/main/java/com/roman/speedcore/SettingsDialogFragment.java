package com.roman.speedcore;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsDialogFragment extends DialogFragment {

    public static final String TAG = "SettingsDialogFragment";
    public static final String PREFS_NAME = "SpeedCorePrefs";
    public static final String KEY_IS_METRIC = "isMetric";
    public static final String KEY_KEEP_SCREEN_ON = "keepScreenOn";
    public static final String KEY_SHOW_SATELLITES = "showSatellites";

    private SwitchMaterial switchUnits;
    private SwitchMaterial switchKeepScreenOn;
    private SwitchMaterial switchShowSatellites;

    private SharedPreferences sharedPreferences;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_SpeedCore_Dialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_settings, null);

        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        switchUnits = view.findViewById(R.id.switch_units);
        switchKeepScreenOn = view.findViewById(R.id.switch_keep_screen_on);
        switchShowSatellites = view.findViewById(R.id.switch_show_satellites);

        loadSettings();

        builder.setView(view)
                .setPositiveButton("Done", (dialog, id) -> {
                    saveSettings();
                });
        return builder.create();
    }

    private void loadSettings() {
        boolean isMetric = sharedPreferences.getBoolean(KEY_IS_METRIC, false);
        boolean keepScreenOn = sharedPreferences.getBoolean(KEY_KEEP_SCREEN_ON, false);
        boolean showSatellites = sharedPreferences.getBoolean(KEY_SHOW_SATELLITES, true);

        switchUnits.setChecked(isMetric);
        switchKeepScreenOn.setChecked(keepScreenOn);
        switchShowSatellites.setChecked(showSatellites);
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_METRIC, switchUnits.isChecked());
        editor.putBoolean(KEY_KEEP_SCREEN_ON, switchKeepScreenOn.isChecked());
        editor.putBoolean(KEY_SHOW_SATELLITES, switchShowSatellites.isChecked());
        editor.apply();

        if (getActivity() instanceof SettingsDialogListener) {
            ((SettingsDialogListener) getActivity()).onSettingsChanged();
        }
    }

    public interface SettingsDialogListener {
        void onSettingsChanged();
    }
}