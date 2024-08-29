package com.esriindonesia.augis.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esriindonesia.augis.R;
import com.esriindonesia.augis.databinding.ActivityMapsBinding;
import com.esriindonesia.augis.databinding.ActivitySampleMapsBinding;
import com.esriindonesia.augis.databinding.ActivitySampleSceneBinding;

public class SampleSceneActivity extends AppCompatActivity {
    private ActivitySampleSceneBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySampleSceneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Portal portal = new Portal("https://tiger.maps.arcgis.com/", false);
        PortalItem portalItem = new PortalItem(portal, "0db5628c0c9148f682be02b421f27ad7");
        ArcGISScene map = new ArcGISScene(portalItem);
        // set up binding and UI behaviour
        binding.sceneView.setScene(map);
    }
}