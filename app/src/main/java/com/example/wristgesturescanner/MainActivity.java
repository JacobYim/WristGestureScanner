package com.example.wristgesturescanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.wristgesturescanner.databinding.ActivityMainBinding;

public class MainActivity extends Activity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Button upButton = findViewById(R.id.upButton);
        Button downButton = findViewById(R.id.downButton);
        Button restButton = findViewById(R.id.restButton);

        upButton.setOnClickListener(view -> {
            Log.e("Clicked","Clicked");
            Intent intent = new Intent(MainActivity.this, ScanningActivity.class);
            intent.putExtra("gesture", "up");
            startActivity(intent);
        });
        downButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ScanningActivity.class);
            intent.putExtra("gesture", "down");
            startActivity(intent);
        });
        restButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ScanningActivity.class);
            intent.putExtra("gesture", "rest");
            startActivity(intent);
        });
    }
}