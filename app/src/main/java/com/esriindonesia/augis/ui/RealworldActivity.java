package com.esriindonesia.augis.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProviders;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.NavigationConstraint;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.toolkit.ar.ArcGISArView;
import com.esri.arcgisruntime.toolkit.control.JoystickSeekBar;
import com.esri.arcgisruntime.toolkit.popup.PopupViewModel;
import com.esriindonesia.augis.R;
import com.esriindonesia.augis.databinding.ActivityMapsBinding;
import com.esriindonesia.augis.databinding.ActivityRealworldBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class RealworldActivity extends AppCompatActivity {

    private static final String TAG = RealworldActivity.class.getSimpleName();
    private ActivityRealworldBinding binding;
    private BottomSheetBehavior<CardView> bottomSheetBehavior;
    private PopupViewModel popupViewModel;
    private Double x;
    private Double y;
    private boolean mIsCalibrating = false;
    private ArcGISScene scene;
    private float mCurrentVerticalOffset = 0f;
    private FeatureLayer featureLayer;
    private ArcGISSceneLayer sceneLayerA;
    private ActivityResultLauncher<Intent> startActivityForResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRealworldBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    private void initView() {
        setKey();
        requestCameraPermission();
        getActivityResult();
        initiateVariable();
        getParam();
        initListener();
        setupARView();
    }

    private void getActivityResult() {
        startActivityForResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            binding.progressBar.setVisibility(View.VISIBLE);
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            Date currentDate = new Date();

                            // Format date to ISO 8601
                            SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
                            String formattedDate = iso8601Format.format(currentDate);
                            String returnedResultOperable = data.getStringExtra("operable");
                            String returnedResultCondition = data.getStringExtra("condition");
                            String returnedResultNeedService = data.getStringExtra("service");
                            updateAttribute();
                        }
                    }
                }
        );
    }

    private void initiateVariable() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetContainer);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        popupViewModel = ViewModelProviders.of(this).get(PopupViewModel.class);
    }

    private void getParam() {
        Intent intent = getIntent();
        x = intent.getDoubleExtra("X", 0.0);
        y = intent.getDoubleExtra("Y", 0.0);
        // set x and y if want to configure specific coordinate
//        x = -6.290746038944566;
//        y = 106.80938757443522;
    }
    private void setKey() {
        ArcGISRuntimeEnvironment.setApiKey("AAPTxy8BH1VEsoebNVZXo8HurOM-YW4eZr_SYZjL_q3SzSQ-8Pvq-TChtn22A00N-eTKYA1sObt-OYaWnV_gPgmRnt_cDGwP_EvmxcyB6QQVyUrlgqq7KH5o8qISG4LInPRyYYnaIwRbuXkm34LyeCUCfPsCu3fo80wAuvt3cyhToGzxPJJ_WnFl-Ny8RzCx4mITueVKZvQpik_8Nc3ESzowOvKAJn5CodpVKHEgzjwfHpnf4K5hyy69WvMpg_-4m5fOAT1_fuP4m3Q9");

        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud2232708308,none,C6JC7XLS1MH0F5KHT033");
    }

    private void initListener() {
        binding.btnCalibrate.setOnClickListener(view -> {
            mIsCalibrating = !mIsCalibrating;
            if (mIsCalibrating) {
                scene.getBaseSurface().setOpacity(0.5f);
                binding.layoutCalibrate.calibrationView.setVisibility(View.VISIBLE);
            } else {
                scene.getBaseSurface().setOpacity(0f);
                binding.layoutCalibrate.calibrationView.setVisibility(View.GONE);
            }
        });

        binding.layoutCalibrate.altitudeJoystick.addDeltaProgressUpdatedListener(new JoystickSeekBar.DeltaProgressUpdatedListener() {
            @Override
            public void onDeltaProgressUpdated(float deltaProgress) {
                mCurrentVerticalOffset += deltaProgress;
                Camera camera = binding.arView.getOriginCamera();
                Camera newCam = camera.elevate(Double.valueOf(String.valueOf(deltaProgress)));
                binding.arView.setOriginCamera(newCam);
            }
        });

        binding.layoutCalibrate.headingJoystick.addDeltaProgressUpdatedListener(new JoystickSeekBar.DeltaProgressUpdatedListener() {
            @Override
            public void onDeltaProgressUpdated(float deltaProgress) {
                Camera camera = binding.arView.getOriginCamera();
                double heading = camera.getHeading() + deltaProgress;
                Camera newCam = camera.rotateTo(heading, camera.getPitch(), camera.getRoll());
                binding.arView.setOriginCamera(newCam);
            }
        });
    }

    private void setupARView() {
        Portal portal = new Portal("https://www.arcgis.com/", false);
        PortalItem portalItem = new PortalItem(portal, "d2d79f1af24640b084729eaba3aa41cf"); // main serice itemid
        scene = new ArcGISScene(portalItem);
        scene.getOperationalLayers().clear();
        scene.getBaseSurface().setNavigationConstraint(NavigationConstraint.NONE);
        binding.arView.setTranslationFactor(1.0);
        scene.getBaseSurface().setOpacity(0f);
        binding.arView.getArSceneView().getPlaneRenderer().setEnabled(false);
        binding.arView.getArSceneView().getPlaneRenderer().setVisible(false);

        displayARFeature();
//        displayARScene();
    }

    private void displayARScene() {
        sceneLayerA = new ArcGISSceneLayer("https://services8.arcgis.com/mpSDBlkEzjS62WgX/arcgis/rest/services/AssetScene_2_WSL1/SceneServer"); // layer url
        scene.getOperationalLayers().addAll(Arrays.asList(sceneLayerA));
        binding.arView.getSceneView().setScene(scene);
        scene.addDoneLoadingListener(() -> {
            if (scene.getLoadStatus() == LoadStatus.LOADED) {
                Envelope envelope = featureLayer.getFullExtent();

                // x and y from intent
                //Camera camera = new Camera(y, x, 2, binding.arView.getOriginCamera().getHeading(), 90.0, binding.arView.getOriginCamera().getRoll());

                // x and y from service
                Camera camera = new Camera(envelope.getCenter().getY(), envelope.getCenter().getX(), 2, binding.arView.getOriginCamera().getHeading(), 90.0, binding.arView.getOriginCamera().getRoll());

                binding.arView.setOriginCamera(camera);

                binding.arView.getSceneView().setOnTouchListener(new DefaultSceneViewOnTouchListener(binding.arView.getSceneView()) {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        // clear any previous selection
                        sceneLayerA.clearSelection();
                        Point screenPoint = new Point(
                                Math.round(motionEvent.getX()),
                                Math.round(motionEvent.getY())
                        );
                        // identify clicked feature
                        ListenableFuture<IdentifyLayerResult> identify = binding.arView.getSceneView()
                                .identifyLayerAsync(sceneLayerA, screenPoint, 10.0, true, 1);
                        identify.addDoneListener(() -> {
                            try {
                                // get the identified result and check that it is a feature
                                IdentifyLayerResult result = identify.get();
                                List<GeoElement> geoElements = result.getElements();
                                if (!geoElements.isEmpty()) {
                                    Log.d(TAG, "geoelement not empty");
                                    popupViewModel.setPopup(result.getPopups().get(0));
                                    GeoElement geoElement = geoElements.get(0);
                                    if (geoElement instanceof Feature) {

                                        sceneLayerA.selectFeature((Feature) geoElement);
                                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                                        binding.progressBar.setVisibility(View.GONE);
                                    }
                                }
                                binding.progressBar.setVisibility(View.GONE);
                            } catch (InterruptedException e) {
                                String error = "Error while identifying layer result: " + e.getMessage();
                                Log.e(TAG, error);
                                Toast.makeText(RealworldActivity.this, error, Toast.LENGTH_LONG).show();
                                binding.progressBar.setVisibility(View.GONE);
                            } catch (ExecutionException e) {
                                String error = "Error while identifying layer result: " + e.getMessage();
                                Log.e(TAG, error);
                                Toast.makeText(RealworldActivity.this, error, Toast.LENGTH_LONG).show();
                                binding.progressBar.setVisibility(View.GONE);
                            }
                        });
                        return true;
                    }
                });

            }
        });

    }

    private void displayARFeature() {
        scene = new ArcGISScene(BasemapStyle.OSM_STREETS);
        scene.getOperationalLayers().add(new FeatureLayer(
                new ServiceFeatureTable("https://services8.arcgis.com/mpSDBlkEzjS62WgX/arcgis/rest/services/persil_capital_place/FeatureServer/0")
        ));
        binding.arView.getSceneView().setScene(scene);
        scene.getBaseSurface().setNavigationConstraint(NavigationConstraint.NONE);

        featureLayer = (FeatureLayer) scene.getOperationalLayers().get(0);

        featureLayer.addDoneLoadingListener(() -> {
            if (featureLayer.getLoadStatus() == LoadStatus.LOADED) {
                Envelope envelope = featureLayer.getFullExtent();

                // x and y from intent
                //Camera camera = new Camera(y, x, 2, binding.arView.getOriginCamera().getHeading(), 90.0, binding.arView.getOriginCamera().getRoll());

                // x and y from service
                Camera camera = new Camera(envelope.getCenter().getY(), envelope.getCenter().getX(), 2, binding.arView.getOriginCamera().getHeading(), 90.0, binding.arView.getOriginCamera().getRoll());

                binding.arView.setOriginCamera(camera);
                binding.arView.getSceneView().setOnTouchListener(new DefaultSceneViewOnTouchListener(binding.arView.getSceneView()) {

                    @Override public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        // clear any previous selection
                        featureLayer = (FeatureLayer) scene.getOperationalLayers().get(0);
                        featureLayer.clearSelection();
                        featureLayer.clearSelection();
//                        sceneLayerSample2.clearSelection();

                        Point screenPoint = new Point(Math.round(motionEvent.getX()),
                                Math.round(motionEvent.getY()));
                        // identify clicked feature
                        ListenableFuture<IdentifyLayerResult> identify = binding.arView.getSceneView()
                                .identifyLayerAsync(featureLayer, screenPoint, 10, false, 1);
                        identify.addDoneListener(() -> {
                            try {
                                // get the identified result and check that it is a feature
                                IdentifyLayerResult result = identify.get();
                                List<GeoElement> geoElements = result.getElements();
                                if (!geoElements.isEmpty()) {
                                    Log.d(TAG, "geoelement not empty");
                                    popupViewModel.setPopup(result.getPopups().get(0));
                                    GeoElement geoElement = geoElements.get(0);
//                                    Object IDs = geoElements.get(0).getAttributes().get("OBJECTID");
//                                    selectedIDs = "IDs.toString()";
                                    binding.btnEdit.setOnClickListener(view -> {
                                        Intent intent = new Intent(RealworldActivity.this, EditAttributeActivity.class);
                                        intent.putExtra("", "");
                                        startActivityForResultLauncher.launch(intent);
                                    });
                                    if (geoElement instanceof Feature) {
                                        Object ads = geoElements.get(0).getAttributes().get("OBJECTID");
                                        Toast.makeText(RealworldActivity.this, ads.toString(), Toast.LENGTH_SHORT).show();
                                        featureLayer.selectFeature((Feature) geoElement);
                                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                                    }
                                }
                                binding.progressBar.setVisibility(View.GONE);
                            } catch (InterruptedException | ExecutionException e) {
                                String error = "Error while identifying layer result: " + e.getMessage();
                                Log.e(TAG, error);
                                Toast.makeText(RealworldActivity.this, error, Toast.LENGTH_LONG).show();
                                binding.progressBar.setVisibility(View.GONE);
                            }
                        });
                        return true;
                    }
                });
            } else {
                String error =
                        "getString(R.string.error_loading_integrated_mesh_layer) + sceneLayer.getLoadError().getMessage()";
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                Log.e(TAG, error);
            }
        });
    }

    private void updateAttribute() {

    }

    private void requestCameraPermission() {
        String[] reqPermission = {Manifest.permission.CAMERA};
        int requestCode = 2;
        if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
            setupARView();
        } else {
            ActivityCompat.requestPermissions(this, reqPermission, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupARView();
        } else {
            Toast.makeText(this, getString(R.string.camera_permission_required_for_ar), Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {
        binding.arView.stopTracking();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.arView.startTracking(ArcGISArView.ARLocationTrackingMode.IGNORE);
    }

}