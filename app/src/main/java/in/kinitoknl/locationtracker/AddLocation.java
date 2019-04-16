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
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AddLocation extends AppCompatActivity {

    private TextInputEditText mLatitude;
    private TextInputEditText mLongitude;
    private Button mStore;
    private Button mGetLocation;

    DatabaseReference mDatabase;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    FirebaseAuth mAuth;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        FirebaseApp.initializeApp(this);
        mLatitude = findViewById(R.id.storeLatitude);
        mLongitude=findViewById(R.id.storeLongitude);
        mStore=findViewById(R.id.store);
        mGetLocation=findViewById(R.id.getLocation2);

        mAuth=FirebaseAuth.getInstance();

        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Black Spots");
        mStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lat = mLatitude.getLayout().getText().toString();
                String lon = mLongitude.getLayout().getText().toString();

                DatabaseReference databaseReference = mDatabase.child(mDatabase.push().getKey());
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("latitude",lat);
                hashMap.put("longitude",lon);
                databaseReference.setValue(hashMap);
                startActivity(new Intent(AddLocation.this,MainActivity.class));
                finish();

            }
        });
        mGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(AddLocation.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddLocation.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddLocation.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION);
                }

                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, new LocationListener() {

                        @Override
                        public void onLocationChanged(Location location) {
                            mLatitude.setText(String.valueOf(location.getLatitude()));
                            mLongitude.setText(String.valueOf(location.getLongitude()));
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

                                mLatitude.setText(String.valueOf(location.getLatitude()));
                                mLongitude.setText(String.valueOf(location.getLongitude()));

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
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser()!=null){
            Toast.makeText(AddLocation.this,"Already login",Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.signOut){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AddLocation.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


}
