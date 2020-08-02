package com.example.safetyappv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerView;

    private DatabaseReference users_ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        FindFriendsRecyclerView = findViewById(R.id.FriendsRecyclerView);
        FindFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        users_ref = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = findViewById(R.id.FriendsToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Finds Friends");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(users_ref,Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull Contacts model)
            {
                holder.user_name.setText(model.getName());
                holder.user_email.setText(model.getEmail());
                //Todo we use the picasso library
                //the getImage Option i have to create in the model class
                //The placeholder displays the default image if the user has not set any profile image
                //Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profile_Image);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();
                        Intent profileIntent = new Intent(FindFriendsActivity.this,VisitUserProfileActivity.class);
                        profileIntent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };

        FindFriendsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView user_name , user_email;
        CircleImageView profile_Image;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            user_name = itemView.findViewById(R.id.user_profile_name);
            user_email = itemView.findViewById(R.id.user_email);
        }
    }
}