package com.example.safetyappv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class JoiningActivity extends AppCompatActivity
{
    Pinview pinview;
    DatabaseReference reference,CurrentUserReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    String current_user_id,join_user_id,current_user_name,current_user_email,current_user_phone,GroupName;

    DatabaseReference circleReference,JoiningUserReference,JoiningCircleReference,CurrentUserJoiningGroupReference,JoiningUserGroupReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joining);
        pinview = findViewById(R.id.Btn_Pinview);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();


        //This is important to get access to the database
        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        CurrentUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

        current_user_id = user.getUid();

        CurrentUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                current_user_name = snapshot.child("name").getValue().toString();
                current_user_email = snapshot.child("email").getValue().toString();
                current_user_phone = snapshot.child("phone").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void OnClickSubmitButton(View view)
    {
        Query query = reference.orderByChild("code").equalTo(pinview.getValue());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    CreateUser createUser = null;
                    for(DataSnapshot childss : snapshot.getChildren())
                    {
                        createUser = childss.getValue(CreateUser.class);

                        join_user_id = createUser.UserId;


                        //This reference is to the mohammed group
                        circleReference = FirebaseDatabase.getInstance().getReference().child("Users").child(join_user_id).child("Circle_members");

                        CircleJoin circleJoin = new CircleJoin(current_user_id,current_user_name,current_user_email,current_user_phone);
                        //CircleJoin circleJoin1 = new CircleJoin(join_user_id);

                        circleReference.child(user.getUid()).setValue(circleJoin)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(JoiningActivity.this, "User joined Succesfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });



                        //Here i am to check if the the joining member has a group
                        JoiningUserReference = reference.child(join_user_id).child("Groups");
                        JoiningUserReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Iterator<DataSnapshot> item = snapshot.getChildren().iterator();
                                while(item.hasNext())
                                {
                                    DataSnapshot SingleItem = item.next();
                                    GroupName = SingleItem.getKey();
                                    CurrentUserReference.child("Groups").child(GroupName).setValue("");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(JoiningActivity.this, "Data cant be Created", Toast.LENGTH_SHORT).show();
                            }
                        });

                        //TODO from here i changed
                        //Here i am creating the joining Users information in my circle members
                        reference.child(join_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    String JoiningName = snapshot.child("name").getValue().toString();
                                    String JoiningEmail = snapshot.child("email").getValue().toString();
                                    String JoiningUserId = snapshot.child("UserId").getValue().toString();
                                    String JoiningPhone = snapshot.child("phone").getValue().toString();

                                    JoiningCircleReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id).child("Circle_members");
                                    CircleJoin circleJoining = new CircleJoin(JoiningUserId,JoiningName,JoiningEmail,JoiningPhone);

                                    //TODO i made changes here
                                    JoiningCircleReference.child(join_user_id).setValue(circleJoining);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        //Here i need to get the group of the current user and create it in the joining user
                        JoiningUserGroupReference = reference.child(current_user_id).child("Groups");
                        JoiningUserGroupReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Iterator<DataSnapshot> itemJoining = snapshot.getChildren().iterator();
                                while(itemJoining.hasNext())
                                {
                                    DataSnapshot SingleItemJoining = itemJoining.next();
                                    GroupName = SingleItemJoining.getKey();
                                    JoiningUserReference.child(GroupName).setValue("");
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(JoiningActivity.this, "Error!" + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Circle code Invalid",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }
}