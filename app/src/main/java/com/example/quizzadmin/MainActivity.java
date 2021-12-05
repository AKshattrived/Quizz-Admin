package com.example.quizzadmin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button goToCategories;//to access go to categories btn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goToCategories = findViewById(R.id.goToCategories);//Initialise goToCategories btn

        goToCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //intent to start category activity
                Intent intent = new Intent(MainActivity.this,CategoryActivity.class);
                startActivity(intent);
            }
        });
    }
}