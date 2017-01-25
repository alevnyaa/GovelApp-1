package com.govelapp.govelapp;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapFragment;

import com.govelapp.govelapp.jsonparser.QueryParser;
import com.govelapp.govelapp.locationmenager.LocationManagerCheck;
import com.govelapp.govelapp.restclient.RestClient;
import com.govelapp.govelapp.shopclasses.Shop;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    //our valid characters OnMapReadyCallback
    private static final Pattern queryPattern = Pattern.compile("[a-zA-Z \t]+");
    private GoogleMap mMap;
    private AutoCompleteTextView actv;
    private String url = "govelapp.com/api";     //getResources().getString(R.string.url);
    private List<Shop> shopList;
    private String query;

    private SlidingUpPanelLayout slidingLayout;
    private Toolbar mToolbar;

    private double latitude, longitude, longitude_cur, latitude_cur;

    private Location mLastLocation;

    private GoogleApiClient mGoogleApiClient;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    private FloatingActionButton settingsButton, gMapButton, mLocationButton;

    private Marker selectedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //basic setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        settingsButton = (FloatingActionButton) findViewById(R.id.buttonOptions);
        gMapButton = (FloatingActionButton) findViewById(R.id.buttonDirection);
        mLocationButton = (FloatingActionButton) findViewById(R.id.buttonMyLocation);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        query = getIntent().getExtras().getString("query");
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        //MAP SETUP
        mMap = googleMap;
        mMap.setIndoorEnabled(false);
        UiSettings mUI = mMap.getUiSettings();
        mUI.setZoomControlsEnabled(false);
        mUI.setMyLocationButtonEnabled(false);
        mUI.setMapToolbarEnabled(false);
        mUI.setCompassEnabled(false);
        mMap.setOnMarkerClickListener(this);

        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.custom_map);
        mMap.setMapStyle(style);

        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);

        mToolbar.setTitle(query);
        ActionBar mActionBar = getSupportActionBar();

        LocationManagerCheck locationManagerCheck = new LocationManagerCheck(this);
        Location location = null;

        if (locationManagerCheck.isLocationServiceAvailable()) {
            if (locationManagerCheck.getProviderType() == 1) {
                //    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else if (locationManagerCheck.getProviderType() == 2) {
                //  location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        } else {
            locationManagerCheck.createLocationServiceError(MapsActivity.this);
        }

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(MapsActivity.this, "Location permission is disabled.", Toast.LENGTH_SHORT).show();
            //request permission
        }

        gMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (latitude != 0.0 && longitude != 0.0) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?saddr=" + latitude_cur + ","
                                    + longitude_cur + "&daddr=" + latitude + "," + longitude));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);
                }
            }
        });

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng latLng = new LatLng(latitude_cur,longitude_cur);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
            }
        });

        showcaseBesiktas();
    }


    //this is for options menu on toolbar
  /*  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_favorite:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }*/

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
           latitude_cur = mLastLocation.getLatitude();
            longitude_cur = mLastLocation.getLongitude();
            LatLng latLng = new LatLng(latitude_cur,longitude_cur);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        selectedMarker = marker;
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        ((TextView) findViewById(R.id.shopNameText)).setText(marker.getTitle());

        //   showDrawer(marker);
        return false;
    }


    private void showcaseBesiktas() {
        String showcasePlaces[][] = {
                {"Derya Promosyon", "41.044232", "29.008083"},
                {"Nokta Copy Center", "41.044087", "29.008058"},
                {"Tasarım ve Fotoğraf", "41.044232", "29.008083"},
                {"Tufan Kırtasiye", "41.043967", "29.008068"},
                {"Sanat Copy Center", "41.043967", "29.008068"},
                {"Tiridi Fabrika", "41.043913", "29.008064"}
        };
        for (int i = 0; i < showcasePlaces.length; i++) {
            LatLng placeLatLng = new LatLng(Double.parseDouble(showcasePlaces[i][1]),
                    Double.parseDouble(showcasePlaces[i][2]));
            mMap.addMarker(new MarkerOptions()
                    .position(placeLatLng)
                    .title(showcasePlaces[i][0])
                    .snippet("Copy & Print")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_pin)));
        }

    }


    //returns true if its a valid query
    private boolean queryValidityTest(String s) {
        Matcher mMatch = queryPattern.matcher(s);
        return mMatch.matches();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //url, query, void ---- params[0], params[1], void
    private class webGetSetMarkers extends AsyncTask<String, String, Void> {
        //loading screen(?)
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //main function to run
        @Override
        protected Void doInBackground(String... params) {
            RestClient rc = new RestClient();
            String jsonReply = rc.getStandardQueryJson(params[0], params[1]);

            QueryParser qp = new QueryParser();
            shopList = qp.parseShopList(jsonReply);
            return null;
        }

        //do after doInBackground is finished
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            for (Shop sh : shopList) {
                mMap.addMarker(sh.getMarkerOptions());
            }
        }

       /* @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }*/

        @Override
        protected void onCancelled(Void result) {

            super.onCancelled(result);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED
                    || slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED){
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }else{
                Intent backIntent = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(backIntent);
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

