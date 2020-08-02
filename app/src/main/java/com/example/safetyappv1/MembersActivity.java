package com.example.safetyappv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.jar.Attributes;

public class MembersActivity extends AppCompatActivity {

    Toolbar toolbar;

    RecyclerView recyclerView;

    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    private MembersAdapter.RecyclerViewClickListener listener;


    FirebaseAuth mAuth;
    FirebaseUser user;

    CreateUser createUser;
    ArrayList<CreateUser> NameList;

    DatabaseReference reference,UserReference;

    String circleMemberId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        toolbar =  findViewById(R.id.members_toolbar);


        toolbar.setTitle("My Group");
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_members);

        recyclerView = findViewById(R.id.my_recycler_view);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        NameList = new ArrayList<>();


        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        UserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Circle_members");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                NameList.clear();
                if(snapshot.exists())
                {
                    for(DataSnapshot dss : snapshot.getChildren())
                    {
                        circleMemberId = dss.child("UserId").getValue(String.class);

                        UserReference.child(circleMemberId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                createUser = snapshot.getValue(CreateUser.class);
                                NameList.add(createUser);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MembersActivity.this, "Error !" + error.getMessage() , Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MembersActivity.this, "Error !" + error.getMessage() , Toast.LENGTH_SHORT).show();
            }
        });
        setOnClicklListener();
        adapter = new MembersAdapter(NameList,getApplicationContext(),listener);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setOnClicklListener() {
        listener = new MembersAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("username", NameList.get(position).UserId);
                intent.putExtra("name",NameList.get(position).name);
                startActivity(intent);
            }
        };
    }


}