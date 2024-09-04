package com.esriindonesia.augis.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.esriindonesia.augis.databinding.ActivityNavBinding;

public class NavActivity extends AppCompatActivity {
    private ActivityNavBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSampleMap.setOnClickListener(v -> {
            Intent i = new Intent(this, SampleMapsActivity.class);
            startActivity(i);
        });
        binding.btnSampleScene.setOnClickListener(v -> {
            Intent i = new Intent(this, SampleSceneActivity.class);
            startActivity(i);
        });
        binding.btnStartRealworld.setOnClickListener(v -> {
            Intent i = new Intent(this, MapsActivity.class);
            startActivity(i);
        });
    }
}