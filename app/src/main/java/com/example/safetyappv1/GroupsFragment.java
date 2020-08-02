package com.example.safetyappv1;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsFragment extends Fragment {


    private View GroupFragmentView;

    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_groups = new ArrayList<>();

    private DatabaseReference GroupRef;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        GroupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        user = mAuth.getCurrentUser();


        GroupRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Groups");

        InitializeFields();

        RetrieveandDisplayGroups();

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroupName = parent.getItemAtPosition(position).toString();

                Intent groupChatIntent =new Intent(getContext(),GroupChatActivity.class);
                groupChatIntent.putExtra("groupName",currentGroupName);
                startActivity(groupChatIntent);
            }
        });

        return GroupFragmentView;
    }


    //TODO
    //I need to replace the ListView with Recycler View
    private void InitializeFields() {

        list_view = (ListView) GroupFragmentView.findViewById(R.id.List_View);
        arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,list_of_groups);
        list_view.setAdapter(arrayAdapter);
    }

    private void RetrieveandDisplayGroups() {

        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Set<String> set = new HashSet<>();
                Iterator iterator = snapshot.getChildren().iterator();
                while (iterator.hasNext())
                {
                    set.add(((DataSnapshot) iterator.next()).getKey());
                }
                list_of_groups.clear();
                list_of_groups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}