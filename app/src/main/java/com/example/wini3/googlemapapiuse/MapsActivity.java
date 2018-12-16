package com.example.wini3.googlemapapiuse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.wini3.googlemapapiuse.model.JsonParserController;
import com.example.wini3.googlemapapiuse.model.PlacesDialogController;
import com.example.wini3.googlemapapiuse.model.PointLocation;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.Task;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.example.wini3.googlemapapiuse.model.JsonParserController.TAG_DESCRIPTION;
import static com.example.wini3.googlemapapiuse.model.JsonParserController.TAG_LAT;
import static com.example.wini3.googlemapapiuse.model.JsonParserController.TAG_LON;
import static com.example.wini3.googlemapapiuse.model.JsonParserController.TAG_NAME;
import static com.example.wini3.googlemapapiuse.model.JsonParserController.TAG_UUID;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnPoiClickListener {


    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private GoogleMap mMap;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLastKnownLocation;
    private PlaceDetectionClient mPlaceDetectionClient;

    public static String SIM_SERIAL_NUMBER = "";
    private Marker myMarker;
    private PointLocation mePointLocation;
    private ArrayList<PointLocation> worldWonders;
    private List<Marker> requestMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // asd
        mMap.setOnPoiClickListener(this);

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker == null || marker.getTag() == null) return false;
                PointLocation pointLocation = (PointLocation) marker.getTag();
//                tvVersion.setText(pointLocation.getComment());
                return false;
            }
        });

        getLocationPermission();
        locationSetup();
        updateLocationUI();

        init();
        simSerialNumber();
        callAsynchronousTask();
    }

    @SuppressLint({"SimpleDateFormat", "ClickableViewAccessibility"})
    private void init() {
        mePointLocation = new PointLocation();
        mePointLocation.setName("Oleg Sh.");
        mePointLocation.setComment("8PS");
        mePointLocation.setUuid(SIM_SERIAL_NUMBER);
    }

    @SuppressLint("MissingPermission")
    private void simSerialNumber() {
        TelephonyManager telephonyManager = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
        SIM_SERIAL_NUMBER = telephonyManager.getSimSerialNumber();
    }

    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            if (mMap != null /*&& myMarker != null && myMarker.getPosition() != null*/) {
                                new LocationLoader().execute();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 1000*1, 1000*8);
    }

    @SuppressLint("MissingPermission") // не проверять Permission
    private void locationSetup() {
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                changeLocation(location);
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {  }
            @Override
            public void onProviderEnabled(String provider) {  }
            @Override
            public void onProviderDisabled(String provider) {  }
        };
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        assert mLocationManager != null;
        mLocationManager.requestLocationUpdates(GPS_PROVIDER, 10000, 0, mLocationListener);
        mLocationManager.requestLocationUpdates(NETWORK_PROVIDER, 10000, 0, mLocationListener);
    }

    @SuppressLint("SetTextI18n")
    private void changeLocation(Location location) {
        if (location == null) return;
//        String provide = location.getProvider();
//        if(provide.equals(GPS_PROVIDER))
//            tvGps.setText("GPS " + formatLocation(location));
//        if(provide.equals(NETWORK_PROVIDER))
//            tvNet.setText("NET " + formatLocation(location));

        LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions myMarkerOptions = new MarkerOptions();
        myMarkerOptions.position(myPosition);

        mLastKnownLocation = location;

        if (myMarker != null) myMarker.remove();
        myMarker = mMap.addMarker(myMarkerOptions.position(myPosition).title("Me"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(myPosition), 1500, null);

        mePointLocation.setLat(myPosition.latitude);
        mePointLocation.setLon(myPosition.longitude);
        mePointLocation.setComment( new SimpleDateFormat("dd.MM.yyyy hh:mm:ss").format(new Date()) );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_get_place:
                showCurrentPlace();
                break;
            case R.id.option_get_place_picker:
                showPlacePicker();
                break;
        }
        return true;
    }

    private void showPlacePicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }
        @SuppressLint("MissingPermission")
        Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
        PlacesDialogController placesDialog = new PlacesDialogController(this, mMap);
        placesDialog.openPlacesDialog(placeResult);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                Log.i("PLACE_PICKER_REQUEST", toastMsg + " " + place.toString());
            }
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {
        Toast.makeText(getApplicationContext(), "Clicked: " +
                        pointOfInterest.name + "\nPlace ID:" + pointOfInterest.placeId +
                        "\nLatitude:" + pointOfInterest.latLng.latitude +
                        " Longitude:" + pointOfInterest.latLng.longitude,
                Toast.LENGTH_SHORT).show();
    }

    private void clearMarker() {
        for (Marker marker: requestMarkers) {
            marker.remove();
        }
    }

    private JsonParserController controller = new JsonParserController();
    private class LocationLoader extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            worldWonders = controller.requestUpdate(pointParams());
//            worldWonders = controller.request();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(worldWonders == null || worldWonders.size() < 1) return;
            clearMarker();
            for (PointLocation point: worldWonders) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(point.getLat(), point.getLon()));
                markerOptions.title(point.getName());
                Marker marker = mMap.addMarker(markerOptions);
                marker.setTag(point);
                requestMarkers.add(marker);
            }
//            LatLng latLon = new LatLng(worldWonders.get(0).getLat(), worldWonders.get(0).getLon());
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLon));
        }

        private List pointParams() {
            List<NameValuePair> params = new ArrayList<>();
            params.add( new BasicNameValuePair(TAG_NAME, mePointLocation.getName()));
            params.add( new BasicNameValuePair(TAG_LAT, Double.toString(mePointLocation.getLat())) );
            params.add( new BasicNameValuePair(TAG_LON, Double.toString(mePointLocation.getLon())) );
            params.add( new BasicNameValuePair(TAG_DESCRIPTION, mePointLocation.getComment()) );
            params.add( new BasicNameValuePair(TAG_UUID, SIM_SERIAL_NUMBER));
            return params;
        }
    }
}


