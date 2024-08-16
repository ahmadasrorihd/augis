package com.esriindonesia.augis.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esriindonesia.augis.R;
import com.esriindonesia.augis.databinding.ActivityMapsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MapsActivity extends AppCompatActivity {
    private ActivityMapsBinding binding;

    private LocationDisplay locationDisplay;
    private LocationManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    private void initView() {
        setKey();
        displayMap();
    }

    private void setKey() {
        ArcGISRuntimeEnvironment.setApiKey("AAPTxy8BH1VEsoebNVZXo8HurOM-YW4eZr_SYZjL_q3SzSQ-8Pvq-TChtn22A00N-eTKYA1sObt-OYaWnV_gPgmRnt_cDGwP_EvmxcyB6QQVyUrlgqq7KH5o8qISG4LInPRyYYnaIwRbuXkm34LyeCUCfPsCu3fo80wAuvt3cyhToGzxPJJ_WnFl-Ny8RzCx4mITueVKZvQpik_8Nc3ESzowOvKAJn5CodpVKHEgzjwfHpnf4K5hyy69WvMpg_-4m5fOAT1_fuP4m3Q9");

        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud2232708308,none,C6JC7XLS1MH0F5KHT033");
    }

    private void displayMap() {
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        binding.mapView.setMap(new ArcGISMap(BasemapStyle.OSM_STREETS));
        binding.mapView.setViewpoint(new Viewpoint(34.056295, -117.195800, 5000.0));

        locationDisplay = binding.mapView.getLocationDisplay();
        locationDisplay.addDataSourceStatusChangedListener(it -> {
            if (!it.isStarted() && it.getError() != null) {
                requestPermissions(it);
            } else {
                if (!locationDisplay.isStarted()) locationDisplay.startAsync();
                displayLocation();
            }
        });
        if (!locationDisplay.isStarted()) locationDisplay.startAsync();
    }

    private void checkGPS() {
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            openSheetDialogGPS();
        }
    }

    private void requestPermissions(LocationDisplay.DataSourceStatusChangedEvent dataSourceStatusChangedEvent) {
        int requestCode = 2;
        String[] reqPermissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        boolean permissionCheckFineLocation =
                ContextCompat.checkSelfPermission(this, reqPermissions[0]) == PackageManager.PERMISSION_GRANTED;

        boolean permissionCheckCoarseLocation =
                ContextCompat.checkSelfPermission(this, reqPermissions[1]) == PackageManager.PERMISSION_GRANTED;

        if (!(permissionCheckFineLocation && permissionCheckCoarseLocation)) {
            ActivityCompat.requestPermissions(this, reqPermissions, requestCode);
        } else {
            if (!locationDisplay.isStarted()) locationDisplay.startAsync();
            displayLocation();
        }
    }

    private void displayLocation() {
        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
        if (!locationDisplay.isStarted()) locationDisplay.startAsync();
        locationDisplay.addLocationChangedListener(it -> {
            double x = it.getLocation().getPosition().getX();
            double y = it.getLocation().getPosition().getY();
            binding.btnStartARView.setOnClickListener(view -> {
                Intent i = new Intent(this, RealworldActivity.class);
                i.putExtra("X", x);
                i.putExtra("Y", y);
                startActivity(i);
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationDisplay.startAsync();
        } else {
            Toast.makeText(this, getResources().getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
            if (!locationDisplay.isStarted()) locationDisplay.startAsync();
            displayLocation();
        }
    }

    @SuppressLint("SetTextI18n")
    private void openSheetDialogGPS() {
        @SuppressLint("InflateParams") View dialogView = LayoutInflater.from(this).inflate(R.layout.sheet_yes_no, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        Button btnNo = dialogView.findViewById(R.id.btn_no);
        Button btnYes = dialogView.findViewById(R.id.btn_yes);
        TextView tvTitle = dialogView.findViewById(R.id.tv_title);
        dialog.setContentView(dialogView);
        dialog.show();
        dialog.setCancelable(false);
        tvTitle.setText("Please enable your GPS");
        btnNo.setVisibility(View.GONE);
        btnYes.setText("Go to setting");
        btnNo.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            dialog.dismiss();
        });
    }

    @Override
    protected void onPause() {
        binding.mapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.resume();
        checkGPS();
    }

    @Override
    protected void onDestroy() {
        binding.mapView.dispose();
        super.onDestroy();
    }
}