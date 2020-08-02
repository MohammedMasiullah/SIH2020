package com.example.safetyappv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class HomeActivity extends AppCompatActivity {

    Button SignIn,SignUp;
    ImageView googleSignIn;
    private GoogleSignInClient googleSignInClient;
    private String Tag = "HomeActivity";
    FirebaseAuth firebaseAuth;
    ImageView google_button;
    DatabaseReference reference;
    String code;
    String userId;

    private int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SignIn = findViewById(R.id.buttonSignIn);
        SignUp = findViewById(R.id.button_sign_up);
        google_button = findViewById(R.id.image_google);

        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this,gso);

        if(firebaseAuth.getCurrentUser() != null)
        {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();

            }
        });

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
                finish();
            }
        });

        google_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sign_In();
            }
        });
    }

    private void Sign_In() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInresult(task);
        }
    }

    private void handleSignInresult(Task<GoogleSignInAccount> completeTask)
    {
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = completeTask.getResult(ApiException.class);
            Toast.makeText(HomeActivity.this,"Signed in Succesfully",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(account);
            //I DID CHANGE HERE
        }
        catch (ApiException e) {
            Toast.makeText(HomeActivity.this,"Sign In Failed",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(null);
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount account) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Logged in Succesfull", Toast.LENGTH_SHORT).show();
                    FirebaseUser User = firebaseAuth.getCurrentUser();
                    Random r = new Random();
                    int n = 100000 + r.nextInt(900000);
                    code = String.valueOf(n);
                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                    if (account != null) {
                        final String personName = account.getDisplayName();
                        String personGivenName = account.getGivenName();
                        String personFamilyName = account.getFamilyName();
                        final String personEmail = account.getEmail();
                        String personId = account.getId();
                        Uri personPhoto = account.getPhotoUrl();

                        CreateUser createUser = new CreateUser(personName, "na", code, personEmail, "false","na", 0, 0, User.getUid());
                        userId = User.getUid();
                        reference.child(userId).setValue(createUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(HomeActivity.this, "User Registered Succesfully", Toast.LENGTH_SHORT).show();
                                    //TODO
                                    //I need to change the class here also
                                    //Here i am sending the data to the mainActivity to display in the header
                                    Intent myintent = new Intent(getApplicationContext(), MainActivity.class);
                                    myintent.putExtra("name", personName);
                                    myintent.putExtra("email", personEmail);
                                    myintent.putExtra("code", code);
                                    startActivity(myintent);
                                    finish();
                                } else {
                                    Toast.makeText(HomeActivity.this, "User cannot be addded", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    } else {
                        Toast.makeText(HomeActivity.this, "Account Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(HomeActivity.this, "Cannot Log in ", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

}