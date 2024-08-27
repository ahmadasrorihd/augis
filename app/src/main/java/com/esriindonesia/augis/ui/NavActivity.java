package com.esriindonesia.augis.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.esriindonesia.augis.R;
import com.esriindonesia.augis.adapter.MenuAdapter;
import com.esriindonesia.augis.databinding.ActivityNavBinding;
import com.esriindonesia.augis.databinding.ActivitySampleMapsBinding;
import com.esriindonesia.augis.model.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class NavActivity extends AppCompatActivity {
    private ActivityNavBinding binding;
    private MenuAdapter menuAdapter;
    private List<MenuItem> menuList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        menuList = new ArrayList<>();
        menuList.add(new MenuItem("AR with device location", R.drawable.ic_edit, MapsActivity.class));
        menuList.add(new MenuItem("AR with service location", R.drawable.ic_edit, MapsActivity.class));
        menuList.add(new MenuItem("Popup in a map", R.drawable.ic_edit, MapsActivity.class));

        menuAdapter = new MenuAdapter(this, menuList);
        binding.recyclerView.setAdapter(menuAdapter);
    }
}