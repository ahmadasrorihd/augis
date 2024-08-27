package com.esriindonesia.augis.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.popup.Popup;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.toolkit.popup.PopupViewModel;
import com.esriindonesia.augis.BuildConfig;
import com.esriindonesia.augis.R;
import com.esriindonesia.augis.databinding.ActivityMapsBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends AppCompatActivity {
    private ActivityMapsBinding binding;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private LocationDisplay locationDisplay;
    private LocationManager manager;
    private PopupViewModel popupViewModel;
    private BottomSheetBehavior<CardView> bottomSheetBehavior;
    private static final int SCALE = 5000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    private void initView() {
        setKey();
        initiateVariable();
        displayMap();
//        binding.btnStartPopup.setOnClickListener(view -> {
//            Intent i = new Intent(this, SamplePopupActivity.class);
//            startActivity(i);
//        });
//        binding.btnStartMap.setOnClickListener(view -> {
//            Intent i = new Intent(this, SampleMapsActivity.class);
//            startActivity(i);
//        });
//        binding.btnStartScene.setOnClickListener(view -> {
//            Intent i = new Intent(this, SampleSceneActivity.class);
//            startActivity(i);
//        });
    }

    private FeatureLayer getFeatureLayer() {
        if (binding.mapView.getMap().getOperationalLayers() != null) {
            for (Object layer : binding.mapView.getMap().getOperationalLayers()) {
                if (layer instanceof FeatureLayer) {
                    FeatureLayer featureLayer = (FeatureLayer) layer;
                    if (featureLayer.isVisible()
                            && featureLayer.isPopupEnabled()) {
                        return featureLayer;
                    }
                }
            }
        }
        return null;
    }

    private void initiateVariable() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetContainer);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        popupViewModel = ViewModelProviders.of(this).get(PopupViewModel.class);
    }

    private void setKey() {
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);
        ArcGISRuntimeEnvironment.setLicense(BuildConfig.LICENSE);
    }

    private void displayMap() {
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        binding.mapView.setMap(new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC));
        binding.mapView.getMap().getOperationalLayers().add(new FeatureLayer(
                new ServiceFeatureTable("https://sampleserver6.arcgisonline.com/arcgis/rest/services/SF311/FeatureServer/0")
        ));
        binding.btnZoomLayer.setOnClickListener(v -> {
            com.esri.arcgisruntime.geometry.Point londonPoint = new com.esri.arcgisruntime.geometry.Point(binding.mapView.getMap().getOperationalLayers().get(0).getFullExtent().getYMax(), binding.mapView.getMap().getOperationalLayers().get(0).getFullExtent().getXMax(), SpatialReferences.getWgs84());
            // create the viewpoint with the London point and scale
            Viewpoint viewpoint = new Viewpoint(londonPoint, SCALE);
            // set the map view's viewpoint to London with a seven second animation duration
            binding.mapView.setViewpointAsync(viewpoint, 7f);
        });

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
        initListener();

    }

    private void initListener() {
        binding.mapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, binding.mapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                // set the progressBar visibility
                binding.progressBar.setVisibility(View.VISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                Point screenPoint = new Point(Math.round(event.getX()), Math.round(event.getY()));
                // setup identifiable layer at the given screen point.
                identifyLayer(screenPoint);
                return true;
            }
        });
    }

    private void identifyLayer(Point screenPoint) {

        FeatureLayer featureLayer = getFeatureLayer();
        if (featureLayer != null) {
            // clear the selected features from the feature layer
            resetIdentifyResult();

            binding.mapView.identifyLayerAsync(featureLayer, screenPoint, 12.0, true)
                    .addDoneListener(() -> {
                        try {
                            IdentifyLayerResult identifyLayerResult =
                                    binding.mapView.identifyLayerAsync(featureLayer, screenPoint, 12.0, true).get();

                            List<Popup> popups = identifyLayerResult.getPopups();
                            if (!popups.isEmpty()) {
                                popupViewModel.setPopup(popups.get(0));
                                FeatureLayer identifiedFeatureLayer = identifyLayerResult.getLayerContent() instanceof FeatureLayer
                                        ? (FeatureLayer) identifyLayerResult.getLayerContent() : null;
                                if (identifiedFeatureLayer != null) {
                                    identifiedFeatureLayer.selectFeature((Feature) popups.get(0).getGeoElement());
                                }
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            String error = "Error identifying results " + e.getMessage();
                            Log.e(TAG, error);
                            Toast.makeText(MapsActivity.this, error, Toast.LENGTH_SHORT).show();
                        }

                        // set the progressBar visibility
                        binding.progressBar.setVisibility(View.GONE);
                    });
        }
    }

    private void resetIdentifyResult() {
        FeatureLayer featureLayer = getFeatureLayer();
        if (featureLayer != null) {
            featureLayer.clearSelection();
        }
        popupViewModel.clearPopup();
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
            binding.btnStartAR.setOnClickListener(view -> {
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