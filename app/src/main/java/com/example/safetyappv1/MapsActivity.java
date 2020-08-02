package com.example.safetyappv1;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.safetyappv1.nearbyPlaces.GetNearbyPlace;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener, LocationListener {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private DatabaseReference ref, FriendsReference;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private LocationManager manager;
    String FriendsUserId;

    private Geocoder geocoder;
    private int ACCESS_LOCATION_REQUEST_CODE = 1001;
    //ACCESSING LOCATION USING FUSED LOCATION PROVIDER CLIENT
    FusedLocationProviderClient fusedLocationProviderClient;
    double currentLat = 0 , currentLong = 0 ;

    LatLng previousLatLng;
    LatLng currentLatLng;
    LatLng FriendsCurrentLatLng;
    LatLng FriendsPreviousLatLng;

    double Lat;
    double Long;
    private int ProximityRadius = 5000;
    double FriendLat;
    double FriendLong;

    private Polyline polyline1;
    FirebaseUser user = mAuth.getCurrentUser();

    String userId = user.getUid();

    //To calculate the distance
    double theta;
    double dist;


    private Marker mCurrLocationMarker;
    private Marker mFriendLocationMarker;

    Intent intent = getIntent();


    //For setting the coordinates on the markers


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        geocoder = new Geocoder(this);

        if(ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            //when permission granted we call the method
            getCurrentLocation();
        }
        else
        {
            ActivityCompat.requestPermissions(MapsActivity.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},44);
        }
    }

    private void getCurrentLocation() {
        //Initialize task Location
        @SuppressLint("MissingPermission")
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null)
                {
                    //When location is not null
                    currentLat = location.getLatitude();
                    currentLong = location.getLongitude();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //this helps to add a marker on the map on long click on any place
        mMap.setOnMapLongClickListener(this);

        //This helps to drag the marker
        mMap.setOnMarkerDragListener(this);

//        LatLng DummyLocation = new LatLng(13.1,77.43);
//        MarkerOptions newMarker = new MarkerOptions().position(DummyLocation);
//        newMarker.icon(BitmapDescriptorFactory.defaultMarker(270));
//        mMap.addMarker(newMarker);
        Intent intent = getIntent();
        if (intent.hasExtra("username")) {
            mMap.clear();
            Bundle extras = getIntent().getExtras();
            fetchFriendLocationUpdates(extras.getString("username"));

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
                locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null)
                        {
                            Lat = location.getLatitude();
                            Long = location.getLongitude();
                            List<Address> addresses = null;
                            try {
                                addresses = geocoder.getFromLocation(Lat,Long,1);
                                if(addresses.size() > 0) {
                                    Address address = addresses.get(0);
                                    String streetAddress = address.getAddressLine(0);
                                    LatLng Current = new LatLng(Lat,Long);
                                    MarkerOptions markerOptions = new MarkerOptions().position(Current).title(streetAddress).snippet("My Location");
                                    mMap.addMarker(markerOptions);
                                }

                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                locationTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MapsActivity.this, "Current Location cant be determined", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            fetchFriendLocationUpdates(extras.getString("username"));


//            Toast.makeText(this, "This has Extra", Toast.LENGTH_SHORT).show();
//            LatLng FriendsLatLng = new LatLng(13.01,77.71);
//            MarkerOptions option = new MarkerOptions().position(FriendsLatLng);
//            option.icon(BitmapDescriptorFactory.defaultMarker(120));
//            mMap.addMarker(option);
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(FriendsLatLng,16));
        }
        else {
            fetchLocationUpdates();
        }
    }

    private void fetchFriendLocationUpdates(String id) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Users").child(id);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.i("tag", "New location updated:" + dataSnapshot.getKey());
                updateFriendMap(dataSnapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateFriendMap(DataSnapshot dataSnapshot) {
        double latitude = 0, longitude = 0;

        Iterable<DataSnapshot> data = dataSnapshot.getChildren();
        for (DataSnapshot d : data) {
            if (d.getKey().equals("latitude")) {
                latitude = (Double) d.getValue();
                FriendLat = latitude;
            } else if (d.getKey().equals("longitude")) {
                longitude = (Double) d.getValue();
                FriendLong = longitude;
            }
        }
        currentLatLng = new LatLng(latitude, longitude);
        if (previousLatLng == null || previousLatLng != currentLatLng) {
            // add marker line
            if (mMap != null) {
                previousLatLng = currentLatLng;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.setPosition(currentLatLng);
                } else {
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(latitude,longitude,1);
                        if(addresses.size() > 0) {
                            Address address = addresses.get(0);
                            String streetAddress = address.getAddressLine(0);
                            Intent nameIntent = getIntent();
                            Bundle extras = getIntent().getExtras();
                            MarkerOptions Options = new MarkerOptions().position(currentLatLng).title(streetAddress).snippet(extras.getString("name"));
                            Options.icon(BitmapDescriptorFactory.defaultMarker(200));
                            mCurrLocationMarker = mMap.addMarker(Options);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
            }

        }
    }


    //This is for getting the current user Updates
    private void fetchLocationUpdates() {

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Users").child(userId);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.i("tag", "New location updated:" + dataSnapshot.getKey());
                updateMap(dataSnapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateMap(DataSnapshot dataSnapshot) {
        double latitude = 0, longitude = 0;

        Iterable<DataSnapshot> data = dataSnapshot.getChildren();
        for (DataSnapshot d : data) {
            if (d.getKey().equals("latitude")) {
                latitude = (Double) d.getValue();
            } else if (d.getKey().equals("longitude")) {
                longitude = (Double) d.getValue();
            }
        }

        currentLatLng = new LatLng(latitude, longitude);

        if (previousLatLng == null || previousLatLng != currentLatLng) {
            // add marker line
            if (mMap != null) {
                previousLatLng = currentLatLng;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.setPosition(currentLatLng);
                } else {
                    mCurrLocationMarker = mMap.addMarker(new MarkerOptions()
                            .position(currentLatLng).draggable(true));
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
            }

        }


    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Log.d(TAG, "onMapLongClick: " + latLng.toString());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addresses.size() >0)
            {
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0);
                mMap.addMarker(new MarkerOptions().draggable(true).position(latLng).title(streetAddress));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng latLng = marker.getPosition();
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addresses.size() >0)
            {
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0);
                marker.setTitle(streetAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private double distance(double Lat, double Long , double FriendLat , double FriendLong)
    {
        theta = Long - FriendLong;
        dist = Math.sin(deg2rad(Lat))
                + Math.sin(deg2rad(FriendLat))
                + Math.cos(deg2rad(Lat))
                + Math.cos(deg2rad(FriendLat))
                + Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg)
    {
        return (deg * Math.PI / 100);
    }

    private double rad2deg(double rad)
    {
        return (rad *180 / Math.PI);
    }

    public void onClick(View v)
    {
        getCurrentLocation();
        String hospital = "hospital" ;
        Object transferData[] = new Object[2];


        switch (v.getId())
        {
            case R.id.hospital:
                String url = getUrl(Lat,Long,hospital);
                transferData[0] = mMap;
                transferData[1] = url;
                GetNearbyPlace getNearbyPlace = new GetNearbyPlace();
                getNearbyPlace.execute(transferData);
//                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
//                        +"?location=" + currentLat + "," + currentLong +
//                        "&radius=5000" +
//                        "&types=hospital" +
//                        "&sensor=true" +
//                        "&key=" + getResources().getString(R.string.google_api_key);

                //new PlaceTask().execute(url);
                Toast.makeText(this, "Searching for nearby hospitals", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "showing Nearby hospitals", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onClick: " + url);
                break;

        }
    }

    private String getUrl(double lat, double aLong, String nearbyPlace) {
        StringBuilder googleUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleUrl.append("location=" + "13.0068" + "," + "77.0012");
        googleUrl.append("&radius=" + ProximityRadius);
        googleUrl.append("&type=" + nearbyPlace);
        googleUrl.append("&sensor=true");
        googleUrl.append("&key=" + "AIzaSyBu6aVKSYADt4Kw3wTt5BMwH2wM7qP8Z4k");

        Log.d(TAG, "getUrl: " + googleUrl.toString());
        return googleUrl.toString();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 44)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                getCurrentLocation();
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

//    private class PlaceTask extends AsyncTask<String,Integer,String> {
//
//        @Override
//        protected String doInBackground(String... strings) {
//            String data = null;
//            try {
//                data = downloadUrl(strings[0]);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return data;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            new ParseTask().execute(s);
//        }
//    }
//
//    private String downloadUrl(String string) throws IOException{
//
//
//            URL url = new URL((string));
//            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//            connection.connect();
//            InputStream stream = connection.getInputStream();
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
//
//            StringBuilder builder = new StringBuilder();
//
//            String line = "";
//
//            while((line = reader.readLine()) != null)
//            {
//                builder.append(line);
//            }
//
//            //Getting the data
//            String data = builder.toString();
//            reader.close();
//            return data;
//    }
//
//    private class ParseTask extends AsyncTask<String,Integer,List<HashMap<String,String>>> {
//
//        @Override
//        protected List<HashMap<String, String>> doInBackground(String... strings) {
//
//            //Create json Parser Class
//            JasonParser jasonParser = new JasonParser();
//
//            List<HashMap<String,String>> mapList = null;
//            JSONObject object = null;
//            try {
//                object = new JSONObject(strings[0]);
//
//                mapList = jasonParser.parseResult(object);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            return mapList;
//        }
//
//        @Override
//        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
//
//            mMap.clear();
//            for(int i = 0 ; i <hashMaps.size(); i++)
//            {
//                HashMap<String,String> hashMapList = hashMaps.get(i);
//
//                double lat = Double.parseDouble(hashMapList.get("lat"));
//                double lng = Double.parseDouble(hashMapList.get("lng"));
//
//                String name = hashMapList.get("name");
//
//                LatLng latLng = new LatLng(lat,lng);
//
//                MarkerOptions options = new MarkerOptions();
//
//                options.position(latLng);
//
//                options.title(name);
//
//                mMap.addMarker(options);
//            }
//        }
//    }
}