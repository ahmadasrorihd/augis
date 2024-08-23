package com.esriindonesia.augis.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esriindonesia.augis.BuildConfig;
import com.esriindonesia.augis.R;
import com.esriindonesia.augis.databinding.ActivityMapsBinding;
import com.esriindonesia.augis.databinding.ActivitySampleSceneViewBinding;

public class SampleSceneView extends AppCompatActivity {
    private ActivitySampleSceneViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySampleSceneViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);
        ArcGISRuntimeEnvironment.setLicense(BuildConfig.LICENSE);

        Portal portal = new Portal("https://www.arcgis.com/");
        PortalItem portalItem = new PortalItem(portal, "31874da8a16d45bfbc1273422f772270");
        ArcGISScene map = new ArcGISScene(portalItem);
        binding.sceneView.setScene(map);
    }

    @Override
    protected void onPause() {
        binding.sceneView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.sceneView.resume();
    }

    @Override protected void onDestroy() {
        binding.sceneView.dispose();
        super.onDestroy();
    }
}