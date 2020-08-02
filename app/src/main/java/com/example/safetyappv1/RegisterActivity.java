package com.example.safetyappv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    EditText mName, mPhone, mEmail, mPassword;
    Button mSignup;
    FirebaseAuth mAuth;
    DatabaseReference reference, LatReference, LonReference;
    String email, password, name, phone;
    FirebaseUser User;
    String userId;
    String code;
    double latitude;
    double longitude;
    ProgressBar progressBar;


    private static final String TAG = "RegisterActivity";
    int LOCATION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName = findViewById(R.id.name);
        mPhone = findViewById(R.id.RegPhone);
        mEmail = findViewById(R.id.RegEmail);
        mPassword = findViewById(R.id.RegPassword);
        mSignup = findViewById(R.id.buttonSignUp);
        progressBar = findViewById(R.id.progressBar);

        //Input Parameters
        email = mEmail.getText().toString();
        name = mName.getText().toString();
        password = mPassword.getText().toString();
        phone = mPhone.getText().toString();
        mAuth = FirebaseAuth.getInstance();

        reference = FirebaseDatabase.getInstance().getReference().child("Users");


        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = mEmail.getText().toString();
                name = mName.getText().toString();
                password = mPassword.getText().toString();
                phone = mPhone.getText().toString();
                Random r = new Random();
                int n = 100000 + r.nextInt(900000);
                code = String.valueOf(n);
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is Required");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is Required");
                    return;
                }

                if (password.length() < 6) {
                    mPassword.setError("Password must be >= 6 characters");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(RegisterActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
                }
                else
                {
                    getCurrentLocation();
                }



                //HERE IN THIS METHOD I AM CREATING THE USERS AND SAVING THERE DATA IN FIREBASE DATABASE
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            User = mAuth.getCurrentUser();
                            userId = User.getUid();

                            Toast.makeText(RegisterActivity.this, "User is Created", Toast.LENGTH_SHORT).show();
                            CreateUser createUser = new CreateUser(name,password,code,email,"false",phone,latitude,longitude,User.getUid());

                            reference.child(userId).setValue(createUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(RegisterActivity.this, "User Registered Succesfully", Toast.LENGTH_SHORT).show();
                                        //Creating a default Group for sending Location
                                        reference.child(User.getUid()).child("Groups").child( name + " " + "Location Group").setValue("");
                                        Intent myintent = new Intent(getApplicationContext(),LoginActivity.class);
                                        myintent.putExtra("name",name);
                                        myintent.putExtra("email",email);
                                        myintent.putExtra("password",password);
                                        myintent.putExtra("phone",phone);
                                        myintent.putExtra("code",code);
                                        startActivity(myintent);
                                        finish();
                                    }
                                    else
                                    {
                                        Toast.makeText(RegisterActivity.this, "User Not Register!...", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOCATION_REQUEST_CODE)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                getCurrentLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {

        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(RegisterActivity.this).requestLocationUpdates(locationRequest,new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LocationServices.getFusedLocationProviderClient(RegisterActivity.this).removeLocationUpdates(this);
                if(locationResult != null && locationResult.getLocations().size() > 0)
                {
                    int LatestLocationIndex = locationResult.getLocations().size()-1;
                    latitude = locationResult.getLocations().get(LatestLocationIndex).getLatitude();
                    longitude = locationResult.getLocations().get(LatestLocationIndex).getLongitude();
                }
            }
        },Looper.getMainLooper());
    }


}