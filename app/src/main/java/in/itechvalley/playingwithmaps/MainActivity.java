package in.itechvalley.playingwithmaps;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback
{
    /*
     * Global Instance of Google Maps
     * */
    private GoogleMap gMap;

    private LocationRequest highAccuracyMode;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * Attach ButterKnife to this Activity
         * */
        ButterKnife.bind(this);

        SupportMapFragment googleMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map_view);

        /*
        * Don't load Google Maps Unless and Until Location Permission is Granted
        * */
        int permissionResult = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionResult == PackageManager.PERMISSION_GRANTED)
        {
            googleMapFragment.getMapAsync(this);

            getCurrentLocation();
        }
        else if (permissionResult == PackageManager.PERMISSION_DENIED)
        {
            /*
            * Array of Permissions
            * */
            final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

            /*
            * Permission Maga
            * */
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    permissions,
                    3131
            );
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        /*
         * Init the Global Instance of GoogleMap.
         *
         * From here, only use the gMap object
         * */
        this.gMap = googleMap;

        /*
        * Move the Camera to India or a specific position
        * */
        final LatLng indiaPosition = new LatLng(18.518005, 73.858840);
        final float zoom = 10.0f;

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(indiaPosition, zoom);
        this.gMap.animateCamera(cameraUpdate);

        /*
        * Enable the Blue Dot
        * */
        this.gMap.setMyLocationEnabled(true);

        /*
        * Set Traffic Enabled
        * */
        this.gMap.setTrafficEnabled(true);

        /*
        * Change Map Style
        * */
        MapStyleOptions ourCustomStyle = MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.our_custom_map_style);
        this.gMap.setMapStyle(ourCustomStyle);

        this.gMap.setBuildingsEnabled(true);

        /*
        * What if you want to add a Marker wherever you click on Map?
        * */
        this.gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                /*
                * Create a Marker and then attach it to Map
                * */

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("You are here!");
                markerOptions.snippet(latLng.toString());

                Bitmap bitmapTree = BitmapFactory.decodeResource(getResources(),R.drawable.icon_new);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmapTree));

                gMap.addMarker(markerOptions);

                /*
                * Animate Camera to max zoom
                * */
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20.0f));
            }
        });
    }

    @OnClick(R.id.fab_switch_type_activity_main)
    void onFabChangeTypeClick()
    {
        if (this.gMap == null)
            return;

        if (this.gMap.getMapType() != GoogleMap.MAP_TYPE_SATELLITE)
        {
            /*
             * Switch to satellite mode
             * */
            this.gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
    }

    private void getCurrentLocation()
    {
        highAccuracyMode = LocationRequest.create();
        highAccuracyMode.setInterval(5000);
        highAccuracyMode.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        /*
        * Check if GPS is Off
        * */
        LocationSettingsRequest.Builder locationDialogBuilder = new LocationSettingsRequest.Builder();
        locationDialogBuilder.addLocationRequest(highAccuracyMode);

        LocationSettingsRequest locationPermissionDialog = locationDialogBuilder.build();

        Task<LocationSettingsResponse> permissionResponse = LocationServices.getSettingsClient(this)
                .checkLocationSettings(locationPermissionDialog);

        permissionResponse.addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                try
                {
                    /*
                    * Type cast the Exception class to ResolvableApiException
                    * */
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    apiException.startResolutionForResult(
                            MainActivity.this,
                            3131
                    );
                }
                catch (IntentSender.SendIntentException ex)
                {
                    ex.printStackTrace();
                }
            }
        });

        permissionResponse.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>()
        {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse)
            {
                startTrackingLocation();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 3131)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                if (highAccuracyMode == null)
                    return;

                startTrackingLocation();
            }
            else if (resultCode == Activity.RESULT_CANCELED)
            {
                Toast.makeText(this, "User nahi bolla", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startTrackingLocation()
    {
        LocationCallback locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                super.onLocationResult(locationResult);

                double lat = locationResult.getLastLocation().getLatitude();
                double lng = locationResult.getLastLocation().getLongitude();
                LatLng currentLocation = new LatLng(lat, lng);

                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17.0f));
            }
        };

        // Start Tracking Location
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        client.requestLocationUpdates(
                highAccuracyMode, locationCallback, null);
    }
}
