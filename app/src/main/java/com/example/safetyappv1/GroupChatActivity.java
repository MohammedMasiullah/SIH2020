package com.example.safetyappv1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.IntentSender;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton SendImageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersReference,GroupNameReference,GroupMessageKeyRef,CircleMemberGroupName,CircleGroupReference;

    private String currentGroupName,currentUserId,currentUserName,currentDate,currentTime;

    //For the circle members
    private String GroupUserId;
    private DatabaseReference circleGroupMessageKeyRef;
    private DatabaseReference circleGroupNameReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();
        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameReference = UsersReference.child(currentUserId).child("Groups").child(currentGroupName);

        InitializeTheFields();

        GetUserInfo();

        SendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDatabase();


                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

                userMessageInput.setText("");


            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        GroupNameReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                if(snapshot.exists())
                {
                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists())
                {
                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void InitializeTheFields()
    {
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);

        SendImageButton = (ImageButton) findViewById(R.id.SendImageView);
        userMessageInput = findViewById(R.id.message_input);
        displayTextMessages = findViewById(R.id.group_chat_text_display);
        mScrollView = findViewById(R.id.My_scroll_View);
    }

    private void GetUserInfo()
    {
        UsersReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    currentUserName = snapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SaveMessageInfoToDatabase() {
        final String message = userMessageInput.getText().toString();
        final String messageKey = GroupNameReference.push().getKey();
        if(TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Please Type in your message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calendar.getTime());


            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calendarTime.getTime());

            HashMap<String,Object> groupMessageKey = new HashMap<>();
            GroupNameReference.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameReference.child(messageKey);
            HashMap<String,Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap);


            //Put the message with the same key in other CircleMembersGroup
            UsersReference.child(currentUserId).child("Circle_members").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Iterator<DataSnapshot> item = snapshot.getChildren().iterator();
                    while (item.hasNext())
                    {
                        DataSnapshot SingleItem = item.next();
                        GroupUserId = SingleItem.child("UserId").getValue().toString();
                        //RootRef.child(GroupUserId).child("Groups").child(groupName).setValue("");
                        circleGroupNameReference = UsersReference.child(GroupUserId).child("Groups").child(currentGroupName);

                        HashMap<String,Object> CircleGroupMessageKey = new HashMap<>();
                        circleGroupNameReference.updateChildren(CircleGroupMessageKey);


                        circleGroupMessageKeyRef = circleGroupNameReference.child(messageKey);
                        HashMap<String,Object> CircleMessageInfoMap = new HashMap<>();
                        CircleMessageInfoMap.put("name",currentUserName);
                        CircleMessageInfoMap.put("message",message);
                        CircleMessageInfoMap.put("date",currentDate);
                        CircleMessageInfoMap.put("time",currentTime);

                        circleGroupMessageKeyRef.updateChildren(CircleMessageInfoMap);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(GroupChatActivity.this, "Data messages Cant be Inserted" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }

    }

    private void DisplayMessages(DataSnapshot snapshot)
    {
        Iterator iterator = snapshot.getChildren().iterator();

        while(iterator.hasNext())
        {
            String ChatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String ChatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String ChatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String ChatTime = (String) ((DataSnapshot) iterator.next()).getValue();

            displayTextMessages.append(ChatName + ":\n" + ChatMessage + "\n" + ChatTime + "    " + ChatDate + "\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }
    }

}