package com.esriindonesia.augis.ui;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProviders;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.IntegratedMeshLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.popup.Popup;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.toolkit.popup.PopupViewModel;
import com.esriindonesia.augis.BuildConfig;
import com.esriindonesia.augis.R;
import com.esriindonesia.augis.databinding.ActivityMapsBinding;
import com.esriindonesia.augis.databinding.ActivitySampleMapsBinding;
import com.esriindonesia.augis.databinding.ActivitySampleSceneBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SampleSceneActivity extends AppCompatActivity {
    private static final String TAG = SampleSceneActivity.class.getSimpleName();
    private ActivitySampleSceneBinding binding;
    private static final int SCALE = 1000;
    private BottomSheetBehavior<CardView> bottomSheetBehavior;
    private PopupViewModel popupViewModel;
    private ArcGISScene scene;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySampleSceneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetContainer);
        popupViewModel = ViewModelProviders.of(this).get(PopupViewModel.class);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        setKey();

//        scene = new ArcGISScene(BasemapStyle.ARCGIS_TOPOGRAPHIC);
//        ArcGISSceneLayer sceneTrafo = new ArcGISSceneLayer("https://services2.arcgis.com/LvCBNZuwhTWWbvod/arcgis/rest/services/GlobalScene_ULP31180_WSL1/SceneServer/layers/0"); // layer url
//        ArcGISSceneLayer sceneKabel = new ArcGISSceneLayer("https://services2.arcgis.com/LvCBNZuwhTWWbvod/arcgis/rest/services/GlobalScene_ULP31180_WSL2/SceneServer/layers/0"); // layer url
//        ArcGISSceneLayer sceneTiang = new ArcGISSceneLayer("https://services2.arcgis.com/LvCBNZuwhTWWbvod/arcgis/rest/services/GlobalScene_ULP31180_WSL3/SceneServer/layers/0"); // layer url
//        scene.getOperationalLayers().addAll(Arrays.asList(sceneTrafo,sceneKabel,sceneTiang));
//        binding.sceneView.setScene(scene);

        Portal portal = new Portal("https://esriid.maps.arcgis.com/", false);
        PortalItem portalItem = new PortalItem(portal, "0f6076590fa0485d8a197f02b8cf4d6a");
        ArcGISScene scene = new ArcGISScene(portalItem);
        binding.sceneView.setScene(scene);

        binding.progressBar.setVisibility(View.VISIBLE);
        scene.addDoneLoadingListener(() -> {
            if (scene.getLoadStatus() == LoadStatus.LOADED) {
                scene.getOperationalLayers().get(0).addDoneLoadingListener(() -> {
                    if (scene.getOperationalLayers().get(0).getLoadStatus() == LoadStatus.LOADED) {
//                        Envelope envelope = sceneTrafo.getFullExtent();
//                        double x = envelope.getCenter().getX();
//                        double y = envelope.getCenter().getY();
////                        double x = binding.sceneView.getScene().getItem().getExtent().getCenter().getX();
////                        double y = binding.sceneView.getScene().getItem().getExtent().getCenter().getY();
////                        binding.sceneView.getScene().getInitialViewpoint().getCamera().getLocation().getX();
////                        binding.sceneView.getScene().getInitialViewpoint().getCamera().getLocation().getY();
//
//                        binding.btnZoomToLayer.setOnClickListener(v -> {
//                            com.esri.arcgisruntime.geometry.Point layerPoint = new com.esri.arcgisruntime.geometry.Point(x, y, SpatialReferences.getWgs84());
//                            Viewpoint viewpoint = new Viewpoint(layerPoint, SCALE);
//                            binding.sceneView.setViewpointAsync(viewpoint, 7f);
//                        });
                        binding.btnZoomToLayer.setVisibility(View.VISIBLE);
                        binding.progressBar.setVisibility(View.GONE);

                        binding.sceneView.setOnTouchListener(new DefaultSceneViewOnTouchListener(binding.sceneView) {
                            @Override
                            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                                binding.progressBar.setVisibility(View.VISIBLE);
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                Point screenPoint = new Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
                                identifyLayer(screenPoint);
                                return true;
                            }
                        });
                    };
                });
            }
        });
    }

    private FeatureLayer getFeatureLayer() {
        if (binding.sceneView.getScene().getOperationalLayers() != null) {
            for (Object layer : binding.sceneView.getScene().getOperationalLayers()) {
                if (layer instanceof FeatureLayer) {
                    FeatureLayer featureLayer = (FeatureLayer) layer;
                    if (featureLayer.isVisible()
                            && featureLayer.isPopupEnabled() && featureLayer.getPopupDefinition() != null) {
                        return featureLayer;
                    }
                }
            }
        }
        return null;
    }

    private ArcGISSceneLayer getArcGISSceneLayer() {
        if (binding.sceneView.getScene().getOperationalLayers() != null) {
            for (Object layer : binding.sceneView.getScene().getOperationalLayers()) {
                if (layer instanceof ArcGISSceneLayer) {
                    ArcGISSceneLayer arcGISSceneLayer = (ArcGISSceneLayer) layer;
                    arcGISSceneLayer.getFeatureTable().getLayer();
                    if (arcGISSceneLayer.isVisible()
                            && arcGISSceneLayer.getFeatureTable().isPopupEnabled()) {
                        return arcGISSceneLayer;
                    }
                }
            }
        }
        return null;
    }

    private void setKey() {
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);
        ArcGISRuntimeEnvironment.setLicense(BuildConfig.LICENSE);
    }

    private void identifyLayer(Point screenPoint) {
//        popup feature
        FeatureLayer featureLayer = getFeatureLayer();
        if (featureLayer != null) {
            resetIdentifyResult();
            binding.sceneView.identifyLayerAsync(featureLayer, screenPoint, 12.0, true)
                    .addDoneListener(() -> {
                        try {
                            IdentifyLayerResult identifyLayerResult =
                                    binding.sceneView.identifyLayerAsync(featureLayer, screenPoint, 12.0, true).get();

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
                            Toast.makeText(SampleSceneActivity.this, error, Toast.LENGTH_SHORT).show();
                        }

                        // set the progressBar visibility
                        binding.progressBar.setVisibility(View.GONE);
                    });
        } else {
            binding.progressBar.setVisibility(View.GONE);
        }

        //popup scene layer
//        ArcGISSceneLayer arcGISSceneLayer = getArcGISSceneLayer();
//        if (arcGISSceneLayer != null) {
//            resetIdentifyResult();
//            binding.sceneView.identifyLayerAsync(arcGISSceneLayer, screenPoint, 10.0, true)
//                    .addDoneListener(() -> {
//                        try {
//                            IdentifyLayerResult identifyLayerResult =
//                                    binding.sceneView.identifyLayerAsync(arcGISSceneLayer, screenPoint, 10.0, true).get();
//                            List<Popup> popups = identifyLayerResult.getPopups();
//                            if (!popups.isEmpty()) {
//                                popupViewModel.setPopup(popups.get(0));
//                                ArcGISSceneLayer identifiedSceneLayer = identifyLayerResult.getLayerContent() instanceof ArcGISSceneLayer
//                                        ? (ArcGISSceneLayer) identifyLayerResult.getLayerContent() : null;
//                                if (identifiedSceneLayer != null) {
//                                    identifiedSceneLayer.selectFeature((Feature) popups.get(0).getGeoElement());
//                                }
//                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
//                            }
//                        } catch (ExecutionException | InterruptedException e) {
//                            String error = "Error identifying results " + e.getMessage();
//                            Log.e(TAG, error);
//                            Toast.makeText(SampleSceneActivity.this, error, Toast.LENGTH_SHORT).show();
//                        }
//
//                        // set the progressBar visibility
//                        binding.progressBar.setVisibility(View.GONE);
//                    });
//        } else {
//            binding.progressBar.setVisibility(View.GONE);
//        }
    }
    private void resetIdentifyResult() {
//        FeatureLayer featureLayer = getFeatureLayer();
//        if (featureLayer != null) {
//            featureLayer.clearSelection();
//        }

        ArcGISSceneLayer arcGISSceneLayer = getArcGISSceneLayer();
        if (arcGISSceneLayer != null) {
            arcGISSceneLayer.clearSelection();
        }
        popupViewModel.clearPopup();
    }

}