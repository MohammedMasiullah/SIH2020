package com.example.safetyappv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class VisitUserProfileActivity extends AppCompatActivity {


    private String ReceiverUserId;

    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileEmail;
    private Button send_request_button;

    private DatabaseReference UserRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_user_profile_actiivity);

        ReceiverUserId = getIntent().getExtras().get("visit_user_id").toString();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userProfileImage =  findViewById(R.id.visit_user_profile_image);
        userProfileName = findViewById(R.id.visit_profile_name);
        userProfileEmail = findViewById(R.id.visit_profile_email);
        send_request_button = findViewById(R.id.Send_Request_btn);

        retrieveUserInfo();
    }

    private void retrieveUserInfo()
    {
        UserRef.child(ReceiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //TODO here this is the validation for the profile image the if loop
                //if((snapshot.exists()) && (snapshot.hasChild("image")));
                if((snapshot.exists()) && (snapshot.hasChild("image")))
                {
                    String VisitorName = snapshot.child("name").getValue().toString();
                    String VisitorEmail = snapshot.child("email").getValue().toString();

                    //TODO this is for the Image
                    String VisitorImage = snapshot.child("image").getValue().toString();

                    Picasso.get().load(VisitorImage).placeholder(R.drawable.profile_image).into(userProfileImage);

                    userProfileName.setText(VisitorName);
                    userProfileEmail.setText(VisitorEmail);
                }

                else
                {
                    String VisitorName = snapshot.child("name").getValue().toString();
                    String VisitorEmail = snapshot.child("email").getValue().toString();

                    userProfileName.setText(VisitorName);
                    userProfileEmail.setText(VisitorEmail);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}