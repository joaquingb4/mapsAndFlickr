package com.example.gmaps;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.gmaps.models.photoSearchModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.gmaps.databinding.ActivityMapsBinding;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    GoogleMap mMap ;
    Button geoLocButton;

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        geoLocButton = findViewById(R.id.geoLocButton);

       // com.example.gmaps.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
      //  setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.flickr.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng){
                double lat = latLng.latitude;
                double lon = latLng.longitude;

                Log.d("Latitud:", "" + lat);
                Log.d("Longitud:", ""+ lon);
                getAddress(lat, lon);
                putMarker(lat, lon);
            }
        });
        geoLocButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                double lat = mMap.getMyLocation().getLatitude();
                double lon = mMap.getMyLocation().getLongitude();
                getAddress(lat, lon);
                putMarker(lat, lon);

                ApiCall apiCall = retrofit.create(ApiCall.class);
                Call<photoSearchModel> call = apiCall.getPhotos(Constants.key,
                        lat,
                        lon);
                call.enqueue(new Callback<photoSearchModel>() {
                    @Override
                    public void onResponse(Call<photoSearchModel> call, Response<photoSearchModel> response) {
                        if (response.code()!=200){
                            Log.i("test","diferente de 200  "+ response.code());
                        }else{
                            for (Photo x:response.body().getPhoto()) {
                                String server_id = x.getServer();
                                String id =x.getId();
                                String secret =x.getSecret();
                                Log.i("test", "https://live.staticflickr.com/"+server_id+"/"+id+"_"+secret+".jpg");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<photoSearchModel> call, Throwable t) {
                        Log.i("test","error");

                    }
                });
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
    public void getAddress(double lat, double lng) {
        try {
            Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(lat, lng, 1);
            if (addresses.isEmpty()) {
                Toast.makeText(this, "No s’ha trobat informació", Toast.LENGTH_LONG).show();
            } else {
                if (addresses.size() > 0) {
                    String msg = addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();

                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "No Location Name Found", Toast.LENGTH_LONG).show();
            Log.d("Error", " Estoy aquí");
        }

    }
    //Poner marcadores
    public void putMarker( double lat, double lon){
        LatLng newPosition = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(newPosition));
    }
    public void localization(){
       // if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, )))
    }

}