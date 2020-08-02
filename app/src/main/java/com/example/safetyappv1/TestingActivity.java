package com.example.safetyappv1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class TestingActivity extends AppCompatActivity {


    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        textView = findViewById(R.id.nametxt);

        String username = "Username not found";
        Bundle extras = getIntent().getExtras();

        if(extras != null)
        {
            username =extras.getString("username");
        }

        textView.setText(username);
    }
}