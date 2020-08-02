package com.example.safetyappv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import android.telephony.SmsManager;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private static final int REQUEST_CODE_SMS_PERMISSION = 2;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    String GroupCode;
    Toolbar toolbar;
    TextView wishing_tv;
    CardView MapcardView, GroupsCardView, VideosCard;


    //Firebase Database
    private GoogleSignInClient googleSignInClient;
    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference RootRef, UserRef, nameRef;
    String GroupUserId;

    TextView header_name, header_email;

    String Profile_name, Profile_email;

    SwitchCompat switchCompat;

    CircleImageView header_image;

    String message = "Hello this masi";
    int SMS_REQUEST_CODE = 100;

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";
    PendingIntent sentPI, deliveredPI;
    BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;

    //For whatsapp group sending sending
    private DatabaseReference GroupNameReference, GroupMessageKeyRef, CircleMemberGroupName, CircleGroupReference;
    private DatabaseReference circleGroupMessageKeyRef;
    private DatabaseReference circleGroupNameReference;
    private double LiveLatitude, LiveLongitude;
    private String currentGroupName, currentUserId, currentUserName, currentDate, currentTime;

    //These are the variables for the array of number;
    String PhoneNumbers;
    private Geocoder geocoder;
    String messageLocation;
    String messageLocationStop;

    FusedLocationProviderClient fusedLocationProviderClient;
    String streetAddress;
    String finalMessage;
    String  SmsSendStreetAddress;
    double SmsLat, SmsLong;
    String SmsMessage;

    double Lat ,Long;
    String LastAddress;

    private DatabaseReference GroupCreationReference;

    private static final String TAG = "MainActivity";;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //-----------Connecting the corresponding icon---------
        Button SosBtn = findViewById(R.id.SOS_btn);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.my_toolbar);
        wishing_tv = findViewById(R.id.Wishing_tv);
        MapcardView = findViewById(R.id.Map_card);
        GroupsCardView = findViewById(R.id.GroupsCardView);
        VideosCard = findViewById(R.id.VideosCardView);
        switchCompat = findViewById(R.id.SwitchButton);
        GroupCreationReference = FirebaseDatabase.getInstance().getReference();

        //Save the switch state
        SharedPreferences sharedPreferences = getSharedPreferences("save", MODE_PRIVATE);

        switchCompat.setChecked(sharedPreferences.getBoolean("value", false));


        geocoder = new Geocoder(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        header_name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.header_name);
        header_email = (TextView) navigationView.getHeaderView(0).findViewById(R.id.header_email);
        header_image = navigationView.getHeaderView(0).findViewById(R.id.profile_image);


        //Pending Intents
        sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);


        //Firebase mAuth instance
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //Reference to the current user
        UserRef = RootRef.child(user.getUid());


        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    header_name.setText(snapshot.child("name").getValue().toString());
                    header_email.setText(snapshot.child("email").getValue().toString());

                    //TODO the app is crashing because there is no photo in database need to set that up
                    //Picasso.get().load(snapshot.child("image").getValue().toString()).into(header_image);

                    Profile_name = snapshot.child("name").getValue().toString();
                    Profile_email = snapshot.child("email").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        LocationRequest locationRequest = new LocationRequest();
//        locationRequest.setInterval(4000);
//        locationRequest.setFastestInterval(2000);
//        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
//        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            LocationServices.getFusedLocationProviderClient(MainActivity.this).requestLocationUpdates(locationRequest,
//                    new LocationCallback() {
//                        @Override
//                        public void onLocationResult(LocationResult locationResult) {
//                            super.onLocationResult(locationResult);
//                            LocationServices.getFusedLocationProviderClient(MainActivity.this)
//                                    .removeLocationUpdates(this);
//                            if (locationResult != null && locationResult.getLocations().size() > 0) {
//                                int latestLocationIndex = locationResult.getLocations().size() - 1;
//                                LiveLatitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
//                                LiveLongitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
//                                List<Address> addresses = null;
//                                try {
//                                    addresses = geocoder.getFromLocation(LiveLatitude, LiveLongitude, 1);
//                                    if (addresses.size() > 0) {
//                                        Address address = addresses.get(0);
//                                        streetAddress = address.getAddressLine(0);
//                                    }
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//
//                            }
//                        }
//                    }, Looper.getMainLooper());
//        }

        getLocation();

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    SharedPreferences.Editor editor = getSharedPreferences("save", MODE_PRIVATE).edit();
                    editor.putBoolean("value", true);
                    editor.apply();
                    //Start the location Updates
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                    } else {

                        //---------method for starting the service--------
                        startLocationService();
                        //TODO here i need to send the location of the user

                        String messageStart = "has started journey from ";
                        SendMessagetoGroup(messageStart);
                    }
                } else {
                    SharedPreferences.Editor editor = getSharedPreferences("save", MODE_PRIVATE).edit();
                    editor.putBoolean("value", false);
                    editor.apply();
                    switchCompat.setChecked(false);

                    //Stop the location
                    stopLocationService();

                    String messageStop = "has ended journey at";
                    SendMessagetoGroup(messageStop);

                    //TODO here i need to send the location of the user

                }
            }
        });


        //TOOLBAR
        toolbar.setTitle("My Dashboard");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);


        //Sos button functionality
        //----------------SOS Botton ----------------------
        SosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String message;
                // String number = "9113659231";
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    final SmsManager smsManager = SmsManager.getDefault();
                    //smsManager.sendTextMessage(number,null,message,sentPI,deliveredPI);
                    //Here i am retriving phone number from firebase;
                    final List<String> list = new ArrayList();
                    final int position = 0;
                    UserRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            SmsLat = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                            SmsLong = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                            List<Address> addresses = null;
                            try {
                                addresses = geocoder.getFromLocation(SmsLat, SmsLong, 1);
                                if (addresses.size() > 0) {
                                    Address address = addresses.get(0);
                                    SmsSendStreetAddress = address.getAddressLine(0);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    UserRef.child("Circle_members").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Iterator<DataSnapshot> item = snapshot.getChildren().iterator();
                            while (item.hasNext()) {
                                DataSnapshot SingleItem = item.next();
                                PhoneNumbers = SingleItem.child("phone").getValue().toString();
                                SmsMessage = "Your Friend" + " " + Profile_name + " " + "is in Danger" + "\n" + SmsSendStreetAddress;
                                smsManager.sendTextMessage(PhoneNumbers, null, SmsMessage, sentPI, deliveredPI);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
//                String numbers[] = {"9113659231", "9844463873","9964752648"};
//                    for(String number : numbers) {
//                        smsManager.sendTextMessage(number, null, message, sentPI, deliveredPI);
//                    }
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, SMS_REQUEST_CODE);
                }

            }
        });


        //The bottom three Lines can be used to hide any item
        //Based on the condition
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_Login).setVisible(false);
        //menu.findItem(R.id.nav_profile).setVisible(false);


        //--------------Navigation toggle ---------------------
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            toggle.getDrawerArrowDrawable().setColor(getColor(R.color.white));
        }
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        //This is keeps the default on home
        navigationView.setCheckedItem(R.id.nav_home);


        //-------setting the greeting based on time-----------
        Calendar mcalendar = Calendar.getInstance();
        int timeOfDay = mcalendar.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 0 && timeOfDay < 12) {
            wishing_tv.setText("Good Morning.");
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            wishing_tv.setText("Good Afternoon.");
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            wishing_tv.setText("Good Evening.");
        } else if (timeOfDay >= 21 && timeOfDay < 24) {
            wishing_tv.setText("Good Night.");
        } else {
            wishing_tv.setText("Cant be determined");
        }


        MapcardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent MapIntent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(MapIntent);
                MapIntent.putExtra("Empty", 0);
            }
        });


        GroupsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GroupIntent = new Intent(getApplicationContext(), GroupActivity.class);
                startActivity(GroupIntent);
            }
        });


        VideosCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent VideoIntent = new Intent(getApplicationContext(), VideosActivity.class);
                startActivity(VideoIntent);
            }
        });


        //This bottom attribute for getting instance of google sign in and firebase sign
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void SendMessagetoGroup(String message) {
        String messageAddress;
        GroupNameReference = UserRef.child("Groups").child(Profile_name + " " + "Location Group");
        final String messageKey = GroupNameReference.push().getKey();

//        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    for (DataSnapshot iterator : snapshot.getChildren()) {
//                        if (iterator.getKey().equals("latitude")) {
//                            latitude = Double.parseDouble(snapshot.child("latitude").getValue().toString());
//                        }
//                        if (iterator.getKey().equals("longitude")) {
//                            longitude = Double.parseDouble(snapshot.child("longitude").getValue().toString());
//                        }
//                    }
//
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        messageAddress = getLocation();

        Log.d(TAG, "SendMessagetoGroup: " + messageAddress);


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        currentDate = currentDateFormat.format(calendar.getTime());


        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calendarTime.getTime());

//        if (SendStreetAddress != null) {
//            finalMessage = Profile_name + " " + message + " " + SendStreetAddress;
//        } else {
          //}

        finalMessage = Profile_name + " " + message + " " + messageAddress;
        final CreateMessage createMessage = new CreateMessage(currentDate,finalMessage,currentTime,Profile_name);

        GroupNameReference.child(messageKey).setValue(createMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, "Data sent Succesfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Data cant be sent", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //I will send the Message to all the people belonging to the group
        UserRef.child("Circle_members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator<DataSnapshot> item = snapshot.getChildren().iterator();
                while (item.hasNext())
                {
                    DataSnapshot SingleItem = item.next();
                    GroupUserId = SingleItem.child("UserId").getValue().toString();
                    RootRef.child(GroupUserId).child("Groups").child(Profile_name + " " + "Location Group").child(messageKey).setValue(createMessage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Data Cant be Inserted" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });




    }

    @Override
    protected void onResume() {
        super.onResume();

        smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "Sms sent", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic Failure", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "No Service", Toast.LENGTH_SHORT).show();
                        break;

                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(context, "Null", Toast.LENGTH_SHORT).show();
                            break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio OFF", Toast.LENGTH_SHORT).show();

                }
            }
        };

        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "Sms Delivered", Toast.LENGTH_SHORT).show();
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(context, "Sms not Delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        registerReceiver(smsSentReceiver,new IntentFilter(SENT));
        registerReceiver(smsDeliveredReceiver,new IntentFilter(DELIVERED));
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(smsDeliveredReceiver);
        unregisterReceiver(smsSentReceiver);
    }

    //REQUEST FOR PERMISSION
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length >0)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                startLocationService();
                getLocation();
            }
            else
            {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == REQUEST_CODE_SMS_PERMISSION && grantResults.length > 0)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
//                SmsManager sms = SmsManager.getDefault();
//                String numbers[] = {"9113659231", "9844463873","9964752648"};
//                for(String number : numbers) {
//                    sms.sendTextMessage(number, null, message, sentPI, deliveredPI);
//                }
                //String number = "9113659231";
                //sms.sendTextMessage(number, null, message, sentPI, deliveredPI);
                //Toast.makeText(this, "message Sent succesfully", Toast.LENGTH_SHORT).show();
                //TODO Write the method for sending sms
            }
            else
            {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void startLocationService()
    {
        Intent intent = new Intent(getApplicationContext(),LocationService.class);
        intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
        startService(intent);
        Toast.makeText(this, "Location service started", Toast.LENGTH_SHORT).show();

    }

    private void stopLocationService()
    {
        Intent intent = new Intent(getApplicationContext(),LocationService.class);
        intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
        startService(intent);
        Toast.makeText(this, "Location Service Stopped", Toast.LENGTH_SHORT).show();
    }

    //HANDELLING THE BACK PRESS
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.nav_home:
                break;

            case R.id.nav_Setting:
                Intent my_intent = new Intent(getApplicationContext(),Setting.class);
                startActivity(my_intent);
                break;


            case R.id.nav_JoinGroup:
                Intent Join_intent = new Intent(getApplicationContext(),JoiningActivity.class);
                startActivity(Join_intent);
                break;


            case R.id.nav_Logout:
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                user = mAuth.getCurrentUser();
                if(user != null || account != null)
                {
                    FirebaseAuth.getInstance().signOut();
                    googleSignInClient.signOut();
                    Toast.makeText(getApplicationContext(),"Sign out Successfull",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                    finish();
                }
                break;


            case R.id.nav_Members:
                Intent new_intent = new Intent(getApplicationContext(),MembersActivity.class);
                startActivity(new_intent);
                break;


            case R.id.nav_share:
                Toast.makeText(getApplicationContext(), "Share", Toast.LENGTH_SHORT).show();
                break;


            case R.id.nav_profile:
                Intent ProfileIntent = new Intent(this, ProfileActivity.class);
                ProfileIntent.putExtra("profile_name",Profile_name);
                ProfileIntent.putExtra("profile_email",Profile_email);
                startActivity(ProfileIntent);
                break;


            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void RequestNewGroup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g My Safety");


        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName))
                {
                    groupNameField.setError("Group Name is Required");
//                    Toast.makeText(MainActivity.this, "Group Name is Required", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //Create in Database
                    CreateNewGroup(groupName);
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

    private void CreateNewGroup(final String groupName)
    {

        Random r = new Random();
        int n = 1000 + r.nextInt(9000);
        GroupCode = String.valueOf(n);
        GroupCreationReference.child("ChatRoom").child(Profile_name + "_" + GroupCode).child("UserId").setValue(user.getUid())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, "Creating Please Waiting", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        GroupCreationReference.child("ChatRoom").child(Profile_name + "_" + GroupCode).child("Group_name").setValue(groupName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, "ChatRoom Succesfully Created", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

//        UserRef.child("Circle_members").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Iterator<DataSnapshot> item = snapshot.getChildren().iterator();
//                while (item.hasNext())
//                {
//                    DataSnapshot SingleItem = item.next();
//                    GroupUserId = SingleItem.child("UserId").getValue().toString();
//                    RootRef.child(GroupUserId).child("Groups").child(groupName).setValue("");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(MainActivity.this, "Data Cant be Inserted" + error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        Toast.makeText(this, "Groups Created Sucessfuly", Toast.LENGTH_SHORT).show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_option_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.Create_Group_menu)
        {
            RequestNewGroup();
        }

        return true;
    }


    //This sends sms to users
//    private void SendSmsToUsers() {
//        Intent i = new Intent(android.content.Intent.ACTION_VIEW);
//        i.putExtra("address", "9113659231;9844463873;9964752648;9606468360");
//        i.putExtra("sms_body", "Testing you!");
//        i.setType("vnd.android-dir/mms-sms");
//        startActivity(i);
//    }

    public String getLocation()
    {
        @SuppressLint("MissingPermission") final Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null)
                Lat = location.getLatitude();
                Long = location.getLongitude();
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(Lat, Long, 1);
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        LastAddress = address.getAddressLine(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.getLocalizedMessage());
            }
        });

//        LocationRequest locationRequest = new LocationRequest();
//        locationRequest.setInterval(4000);
//        locationRequest.setFastestInterval(2000);
//        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
//        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            LocationServices.getFusedLocationProviderClient(MainActivity.this).requestLocationUpdates(locationRequest,
//                    new LocationCallback() {
//                        @Override
//                        public void onLocationResult(LocationResult locationResult) {
//                            super.onLocationResult(locationResult);
//                            LocationServices.getFusedLocationProviderClient(MainActivity.this)
//                                    .removeLocationUpdates(this);
//                            if (locationResult != null && locationResult.getLocations().size() > 0) {
//                                int latestLocationIndex = locationResult.getLocations().size() - 1;
//                                Lat = locationResult.getLocations().get(latestLocationIndex).getLatitude();
//                                Long = locationResult.getLocations().get(latestLocationIndex).getLongitude();
//                                List<Address> addresses = null;
//                                try {
//                                    addresses = geocoder.getFromLocation(LiveLatitude, LiveLongitude, 1);
//                                    if (addresses.size() > 0) {
//                                        Address address = addresses.get(0);
//                                        LastAddress = address.getAddressLine(0);
//                                        LastLocationStreet[0] = LastAddress;
//                                    }
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//
//                            }
//                        }
//                    }, Looper.getMainLooper());
        //}
        return LastAddress;
    }

}
