package in.kinitoknl.locationtracker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    TextView mLatitude;
    TextView mLongitude;
    DatabaseReference mDatabase;

    double latitude;
    double longitude;

    boolean flag=true;
    double prevLat=0.0,prevLon=0.0,currLat,currLon;

    String address;

    double lat;
    double lon;

    int distance;
    double speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLatitude = findViewById(R.id.latitude);
        mLongitude = findViewById(R.id.longitude);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Black Spots");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION);
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {
                    while(flag){
                        prevLat=location.getLatitude();
                        prevLon=location.getLongitude();
                        flag=false;
                    }
                    latitude=location.getLatitude();
                    longitude=location.getLongitude();
                    currLat=latitude;
                    currLon=longitude;
                    mLatitude.setText(String.valueOf(location.getLatitude()));
                    mLongitude.setText(String.valueOf(location.getLongitude()));
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Geocoder geocoder;
                            List<Address> addresses;
                            geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                            for(DataSnapshot dsp:dataSnapshot.getChildren()){
                                lat = Double.valueOf(String.valueOf(dsp.child("latitude").getValue()));
                                lon = Double.valueOf(String.valueOf(dsp.child("longitude").getValue()));

                                try {
                                    addresses = geocoder.getFromLocation(lat, lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                    address = addresses.get(0).getAddressLine(0);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                distance = (int)Math.round(distance(latitude,lat,longitude,lon,0,0));

//                                        if(latitude==lat || longitude==lon){
//                                            Toast.makeText(MainActivity.this,"You are approaching the black spot",Toast.LENGTH_LONG).show();
//                                        }
                                speed=18*distance(prevLat,currLat,prevLon,currLon,0,0)/50; //in kmph
                                if(distance<=8000 && speed>0){
                                    Toast.makeText(MainActivity.this,"The Distance is :"+String.valueOf(distance/1000)+" kms"+"\nLocation is :"+address,Toast.LENGTH_LONG).show();
                                    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                                    Intent intent = new Intent(MainActivity.this,NotificationReceiver.class);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,0,intent,0);

                                    Notification notification = new Notification.Builder(MainActivity.this)
                                            .setContentTitle("Block  Spot Alert")
                                            .setSound(soundUri)
                                            .setContentIntent(pendingIntent)
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setContentText(address+" is the Location").build();

                                    NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                                    notificationManager.notify(0,notification);
                                }}
                            Toast.makeText(MainActivity.this, "Old Latitude :"+prevLat+" Old Longitude :"+prevLon+"\nNew Latitude :"+currLat+" New Longitude"+currLon, Toast.LENGTH_SHORT).show();
                            prevLat=currLat;
                            prevLon=currLon;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
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
            });

            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        latitude=location.getLatitude();
                        longitude=location.getLongitude();

                        mLatitude.setText(String.valueOf(location.getLatitude()));
                        mLongitude.setText(String.valueOf(location.getLongitude()));
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Geocoder geocoder;
                                List<Address> addresses;
                                geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                                for(DataSnapshot dsp:dataSnapshot.getChildren()){
                                    lat = Double.valueOf(String.valueOf(dsp.child("latitude").getValue()));
                                    lon = Double.valueOf(String.valueOf(dsp.child("longitude").getValue()));

                                    try {
                                        addresses = geocoder.getFromLocation(lat, lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                        address = addresses.get(0).getAddressLine(0);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    distance = (int)Math.round(distance(latitude,lat,longitude,lon,0,0));

//                                        if(latitude==lat || longitude==lon){
//                                            Toast.makeText(MainActivity.this,"You are approaching the black spot",Toast.LENGTH_LONG).show();
//                                        }
                                    speed=18*distance(prevLat,currLat,prevLon,currLon,0,0)/50; //in kmph
                                    if(distance<=8000 && speed>0){
                                        Toast.makeText(MainActivity.this,"The Distance is :"+String.valueOf(distance/1000)+" kms"+"\nLocation is :"+address,Toast.LENGTH_LONG).show();
                                        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                                        Intent intent = new Intent(MainActivity.this,NotificationReceiver.class);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,0,intent,0);

                                        Notification notification = new Notification.Builder(MainActivity.this)
                                                .setContentTitle("Block Spot Alert")
                                                .setSound(soundUri)
                                                .setContentIntent(pendingIntent)
                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                .setContentText(address+" is the Location").build();

                                        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                                        notificationManager.notify(0,notification);
                                    }}
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
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
                });
            }
        }

    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.login){
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}
