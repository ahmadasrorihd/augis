package com.esriindonesia.augis;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.esriindonesia.augis.ui.MapsActivity;
import com.esriindonesia.augis.ui.NavActivity;
import com.esriindonesia.augis.ui.RealworldActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Intent i = new Intent(this, NavActivity.class);
        startActivity(i);
    }
}