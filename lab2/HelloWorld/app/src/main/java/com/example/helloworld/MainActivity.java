package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "Hello World");
        try {
            throw new RuntimeException("error in MainActivity.onCreate");
        }
        catch (Exception e)
        {
            Log.e(this.getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));
        }
    }
}