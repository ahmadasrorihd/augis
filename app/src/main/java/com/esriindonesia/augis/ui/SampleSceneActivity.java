package com.esriindonesia.augis.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    }
}