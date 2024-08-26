package com.esriindonesia.augis.ui;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.popup.Popup;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.toolkit.popup.PopupViewModel;
import com.esriindonesia.augis.BuildConfig;
import com.esriindonesia.augis.R;
import com.esriindonesia.augis.databinding.ActivityMainBinding;
import com.esriindonesia.augis.databinding.ActivityMapsBinding;
import com.esriindonesia.augis.databinding.ActivitySamplePopupBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class SamplePopupActivity extends AppCompatActivity {

    private static final String TAG = SamplePopupActivity.class.getSimpleName();

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private ArcGISMap map;
    private PopupViewModel popupViewModel;
    private ActivitySamplePopupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySamplePopupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);
        popupViewModel = new ViewModelProvider(this).get(PopupViewModel.class);
        Portal portal = new Portal("https://arcgisruntime.maps.arcgis.com/", false);
        PortalItem portalItem = new PortalItem(portal, "fb788308ea2e4d8682b9c05ef641f273");
        map = new ArcGISMap(portalItem);
        // set up binding and UI behaviour
        binding.mapView.setMap(map);
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetContainer);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // reset the IdentifyResult on a sheet close
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    // Clear the selected features from the feature layer
                    resetIdentifyResult();
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
            }
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        // Clear the selected features from the feature layer
        resetIdentifyResult();

        // set the progressBar visibility
        binding.progressBar.setVisibility(View.GONE);

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

    /**
     * Getter function to retrieve the first available feature layer
     * [featureLayer] updates with every map click
     */
    private FeatureLayer getFeatureLayer() {
        if (map.getOperationalLayers() != null) {
            for (Object layer : map.getOperationalLayers()) {
                if (layer instanceof FeatureLayer) {
                    FeatureLayer featureLayer = (FeatureLayer) layer;
                    if (featureLayer.getFeatureTable().getGeometryType() == GeometryType.POINT
                            && featureLayer.isVisible()
                            && featureLayer.isPopupEnabled()
                            && featureLayer.getPopupDefinition() != null) {
                        return featureLayer;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Performs an identify on the feature layer at the given screen point.
     * [screenPoint] in Android graphic coordinates.
     */
    private void identifyLayer(Point screenPoint) {

        FeatureLayer featureLayer = getFeatureLayer();
        if (featureLayer != null) {
            // clear the selected features from the feature layer
            resetIdentifyResult();

            binding.mapView.identifyLayerAsync(featureLayer, screenPoint, 12.0, true)
                    .addDoneListener(() -> {
                        try {
                            com.esri.arcgisruntime.mapping.view.IdentifyLayerResult identifyLayerResult =
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
                            Toast.makeText(SamplePopupActivity.this, error, Toast.LENGTH_SHORT).show();
                        }

                        // set the progressBar visibility
                        binding.progressBar.setVisibility(View.GONE);
                    });
        }
    }

    /**
     * Resets the Identify Result.
     */
    private void resetIdentifyResult() {
        FeatureLayer featureLayer = getFeatureLayer();
        if (featureLayer != null) {
            featureLayer.clearSelection();
        }
        popupViewModel.clearPopup();
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
    }

    @Override
    protected void onDestroy() {
        binding.mapView.dispose();
        super.onDestroy();
    }
}
