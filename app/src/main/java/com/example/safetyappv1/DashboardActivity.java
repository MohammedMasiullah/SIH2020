package com.example.safetyappv1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {



    Button signout;
    private GoogleSignInClient googleSignInClient;
    FirebaseAuth mAuth;
    FirebaseUser user;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        signout = findViewById(R.id.SignOutButton);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this,gso);

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                user = mAuth.getCurrentUser();
                if(user != null || account != null)
                {
                    FirebaseAuth.getInstance().signOut();
                    googleSignInClient.signOut();
                    Toast.makeText(getApplicationContext(),"Sign out Successfull",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                    finish();
                }
            }
        });
    }
}