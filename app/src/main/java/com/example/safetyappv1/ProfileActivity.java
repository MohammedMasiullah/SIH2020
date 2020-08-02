package com.example.safetyappv1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.fonts.Font;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.safetyappv1.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseUser User;
    DatabaseReference userReference,RootRef;


    TextView Profile_name_tv, Profile_email_tv,Share_text;
    String ShareName,Code;

    private static final int GalleryPick = 1;
    private StorageReference UserProfileImageRef;

    private CircleImageView userProfileImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Profile_name_tv = findViewById(R.id.profile_name);
        Profile_email_tv = findViewById(R.id.profile_email);
        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser();
        Share_text = findViewById(R.id.Share_tv);
        RootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImage = findViewById(R.id.Profile_image_setting);

        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(User.getUid());

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");



        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               if(snapshot.exists())
               {
                   Profile_name_tv.setText(snapshot.child("name").getValue().toString());
                   Profile_email_tv.setText(snapshot.child("email").getValue().toString());
                   //Picasso.get().load(snapshot.child("image").getValue().toString()).into(userProfileImage);
                   ShareName = snapshot.child("name").getValue().toString();
                   Code = snapshot.child("code").getValue().toString();
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Share_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, ShareName + "\nis inviting you to the group");
                    shareIntent.putExtra(Intent.EXTRA_TEXT,"Please type in this code" + " " + Code + " " + "to track your Friend");
                    startActivity(Intent.createChooser(shareIntent,"Share it"));
                }
                catch (Exception e)
                {
                    Toast.makeText(ProfileActivity.this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GalleryPick && resultCode == RESULT_OK && data!=null)
        {
            Uri ImageUrl = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(User.getUid() + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUri = uri.toString();
                                RootRef.child("Users").child(User.getUid()).child("image").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(ProfileActivity.this, "Image uploaded to database Succesfully", Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                        {
                                            Toast.makeText(ProfileActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }


    }
}