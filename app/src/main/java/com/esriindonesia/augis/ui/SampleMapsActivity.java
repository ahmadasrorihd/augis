package com.esriindonesia.augis.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.toolkit.popup.PopupViewModel;
import com.esriindonesia.augis.BuildConfig;
import com.esriindonesia.augis.databinding.ActivitySampleMapsBinding;

public class SampleMapsActivity extends AppCompatActivity {
    private ActivitySampleMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySampleMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);
        Portal portal = new Portal("https://arcgisruntime.maps.arcgis.com/", false);
        PortalItem portalItem = new PortalItem(portal, "fb788308ea2e4d8682b9c05ef641f273");
        ArcGISMap map = new ArcGISMap(portalItem);
        binding.mapView.setMap(map);
    }
}