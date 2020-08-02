package com.example.safetyappv1;

import android.app.Service;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationService extends Service {


    //I HAVE TO CHANGE THE DATABASE REFERENCE
    //TODO
    private static final String TAG = "Location_Update";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    String userId = user.getUid();
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");

    private DatabaseReference LatReference,LonReference;

    //private DatabaseReference LatReference = reference.child("latitude");
   // private DatabaseReference LonReference = reference.child("longitude");

    //-------------This method is for getting the location--------------
    public LocationCallback locationCallback  = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            reference = FirebaseDatabase.getInstance().getReference().child("Users");
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                Log.d("Location_Update", latitude + ", " + longitude);
                if(user != null) {
                    LatReference = reference.child(userId).child("latitude");
                    LonReference = reference.child(userId).child("longitude");
                    LatReference.setValue(latitude);
                    LonReference.setValue(longitude);
                }
                else
                {
                    Log.d("Location_Update", "onLocationResult: " + "User is null");
                }

            }
        }
    };



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet Implemented");
    }


    //--------------This method is for bringing up the notification-------------
    @SuppressLint("MissingPermission")
    private void startLocationService() {
        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
    }



    //-------------method for stopping the service--------------
    private void stopLocationService()
    {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }



    //----------------background method---------------------------
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null)
        {
            String action = intent.getAction();
            if(action != null)
            {
                if(action.equals(Constants.ACTION_START_LOCATION_SERVICE))
                {
                    startLocationService();
                }
                else if(action.equals(Constants.ACTION_STOP_LOCATION_SERVICE))
                {
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    //----------------background-method---------------------
}


