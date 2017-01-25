package com.govelapp.govelapp;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SlidingPaneLayout;
import com.google.android.gms.location.LocationListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
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
import com.govelapp.govelapp.locationmenager.GPSTracker;
import com.govelapp.govelapp.locationmenager.LocationManagerCheck;
import com.govelapp.govelapp.restclient.RestClient;
import com.govelapp.govelapp.shopclasses.Shop;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {
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

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    GPSTracker gps = new GPSTracker(this);

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

        //slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        //create a seperate adapter for maps activity search actv
        /*actv = (AutoCompleteTextView) findViewById(R.id.search);
        String[] items = {"tea", "apple", "phone case", "tooth paste", "tennis racket",
         "tooth brush", "tooth pick", "kahve"}; //this is for testing purposes
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_expandable_list_item_1, items);
        actv.setAdapter(adapter);
        actv.setText(getIntent().getExtras().getString("query"));
        actv.clearFocus();
        actv.setSelection(actv.getText().length());
        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mapsQuery = actv.getText().toString();
                actv.setText(mapsQuery);
                actv.setSelection(mapsQuery.length()); //set the cursor position
                if (mapsQuery.length() > 0 && queryValidityTest(mapsQuery)) {
                    query = mapsQuery;
                    new webGetSetMarkers().execute(url, query);
                } else {
                    Toast.makeText(MapsActivity.this, "Invalid query.", Toast.LENGTH_LONG).show();
                }
            }
        });*/

        query = getIntent().getExtras().getString("query");
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mLocationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(gps.canGetLocation()){
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gps.getLatitude(), gps.getLongitude()), 16.5f));
                            }else{
                                Toast.makeText(MapsActivity.this, "cant get location.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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

