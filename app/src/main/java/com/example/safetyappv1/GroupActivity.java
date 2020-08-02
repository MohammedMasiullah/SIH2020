package com.example.safetyappv1;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.safetyappv1.ui.main.SectionsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class GroupActivity extends AppCompatActivity {

    Toolbar mToolbar;
    private DatabaseReference GroupReference,CurrentUserGroupReference;
    FirebaseUser user;
    FirebaseAuth mAuth;
    String GroupName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group2);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        mToolbar = findViewById(R.id.my_toolbar);
        mToolbar.setTitle("Connect");
        setSupportActionBar(mToolbar);


        TabLayout tabs = findViewById(R.id.tabs);

        tabs.setupWithViewPager(viewPager);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        CurrentUserGroupReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Groups");
        GroupReference = FirebaseDatabase.getInstance().getReference().child("ChatRoom");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_menu_options,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.FindsFriends)
        {
            Intent FriendActivityIntent = new Intent(GroupActivity.this,FindFriendsActivity.class);
            startActivity(FriendActivityIntent);
        }
        if(item.getItemId() == R.id.JoinGroupChat)
        {
            RequestJoiningGroup();
        }
        return true;
    }

    private void RequestJoiningGroup() {


        AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Your Friends Group Code");
        final EditText groupNameField = new EditText(GroupActivity.this);
        groupNameField.setHint("e.g name_code");


        builder.setView(groupNameField);

        builder.setPositiveButton("Join", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupCode = groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupCode))
                {
                    groupNameField.setError("Group Name is Required");
//                    Toast.makeText(MainActivity.this, "Group Name is Required", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //Create in Database
                    JoinNewGroup(groupCode);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void JoinNewGroup(String groupCode) {
        GroupReference.child(groupCode).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    GroupName = snapshot.child("Group_name").getValue().toString();
                    CurrentUserGroupReference.child(GroupName).setValue("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}