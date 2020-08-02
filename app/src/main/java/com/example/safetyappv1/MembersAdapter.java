package com.example.safetyappv1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerViewAccessibilityDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MembersViewHolder>
{
    ArrayList<CreateUser> NameList;
    Context context;

    private RecyclerViewClickListener listener;

    MembersAdapter(ArrayList<CreateUser> NameList, Context context,RecyclerViewClickListener listener)
    {
        this.NameList = NameList;
        this.context = context;
        this.listener = listener;
    }


    @Override
    public int getItemCount() {
        return NameList.size();
    }


    public interface RecyclerViewClickListener{
        void onClick(View v ,int position);

    }

    @NonNull
    @Override
    public MembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout,parent,false);
        MembersViewHolder membersViewHolder = new MembersViewHolder(view,context,NameList);

        return membersViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MembersViewHolder holder, int position) {

        CreateUser createUser = NameList.get(position);
        holder.name_txt.setText(createUser.name);


    }


    public class MembersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        TextView name_txt;
        Context context;
        ArrayList<CreateUser> arrayList;
        FirebaseAuth mAuth;
        FirebaseUser MUser;

        public MembersViewHolder(@NonNull View itemView, Context context, ArrayList<CreateUser> arrayList) {
            super(itemView);
            this.context = context;
            this.arrayList = arrayList;

            itemView.setOnClickListener(this);
            mAuth = FirebaseAuth.getInstance();
            MUser = mAuth.getCurrentUser();

            name_txt = itemView.findViewById(R.id.ItemTitle);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v,getAdapterPosition());
        }

    }
}