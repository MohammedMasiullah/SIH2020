package com.example.safetyappv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    TextView DontHaveAccount,forgotPassword;
    EditText lEmail,lPassword;
    Button Sign_In;
    String name,email,password,date,code,isSharing,phone;
    private int RC_SIGN_IN = 1;
    FirebaseUser User;
    FirebaseAuth mAuth;
    DatabaseReference reference;
    ProgressBar progressBar2;
    FirebaseAuth mfirebaseauth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        lEmail = findViewById(R.id.inputEmail);
        lPassword = findViewById(R.id.inputPassword);
        Sign_In = findViewById(R.id.buttonSignIn);
        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        forgotPassword = findViewById(R.id.textForgotPassword);
        progressBar2 = findViewById(R.id.progressBar2);

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText restmain = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter your Email to Recieve password Link");
                passwordResetDialog.setView(restmain);
                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //Extract the email and send the link
                        String mail = restmain.getText().toString();
                        mfirebaseauth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this, "Reset Link sent to your mail" , Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Error! Reset link Cannot be Sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close the Dialog
                    }
                });
                //This is to display the dialog box
                passwordResetDialog.create().show();

            }
        });



        Intent myintent = getIntent();
        if(myintent != null)
        {
            name = myintent.getStringExtra("name");
            email = myintent.getStringExtra("email");
            password = myintent.getStringExtra("password");
            phone = myintent.getStringExtra("phone");
            code = myintent.getStringExtra("code");

        }

        lEmail.setText(email);
        lPassword.setText(password);


        DontHaveAccount = findViewById(R.id.Dont_have_account);

        DontHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
                finish();
            }
        });

        Sign_In.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar2.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(lEmail.getText().toString(),lPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(getApplicationContext(),"User Logged in",Toast.LENGTH_SHORT).show();
                            //TODO
                            //I need to change the class
                            Intent myintent = new Intent(getApplicationContext(),MainActivity.class);


                            startActivity(myintent);
                            finish();
                            progressBar2.setVisibility(View.GONE);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"User cannot log in",Toast.LENGTH_SHORT).show();
                            progressBar2.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

    }
}