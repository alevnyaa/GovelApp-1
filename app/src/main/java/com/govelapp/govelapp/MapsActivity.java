package com.govelapp.govelapp;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
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
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, SearchView.OnQueryTextListener {
    //our valid characters OnMapReadyCallback
    private static final Pattern queryPattern = Pattern.compile("[a-zA-Z \t]+");
    private GoogleMap mMap;
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

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //basic setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        query = getIntent().getExtras().getString("query");
        Log.d("Query", query);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        settingsButton = (FloatingActionButton) findViewById(R.id.buttonOptions);
        gMapButton = (FloatingActionButton) findViewById(R.id.buttonDirection);
        gMapButton.setVisibility(View.GONE);
        mLocationButton = (FloatingActionButton) findViewById(R.id.buttonMyLocation);

        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);

        //test purposed

        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.foto_background)
                .addProfiles(
                        new ProfileDrawerItem().withName("Kenan Soylu").withEmail("adsasd@gmail.com").withIcon(getResources().getDrawable(R.drawable.black_marker))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        PrimaryDrawerItem appName = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.app_name);
        SecondaryDrawerItem settings = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.settings);
        SecondaryDrawerItem url = new SecondaryDrawerItem().withIdentifier(3).withName("storchapp.com");
        SecondaryDrawerItem feedback = new SecondaryDrawerItem().withIdentifier(4).withName(R.string.feedback);
        SecondaryDrawerItem privacy = new SecondaryDrawerItem().withIdentifier(5).withName(R.string.privacy_policy);
        SecondaryDrawerItem favs = new SecondaryDrawerItem().withIdentifier(6).withName(R.string.favourites);

        Drawer result = new DrawerBuilder().withAccountHeader(accountHeader)
                .withActivity(this)
                .withToolbar(mToolbar)
                .addDrawerItems(
                        appName,
                        new DividerDrawerItem(),
                        favs,
                        settings,
                        feedback,
                        privacy,
                        url
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Toast.makeText(MapsActivity.this, "Item pressed " + position, Toast.LENGTH_SHORT).show();
                        if(position == 7){
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.storchapp.com"));
                            startActivity(browserIntent);
                        }
                        return true;
                    }
                })
                .build();

        if(mLastLocation != null){
            mLastLocation.reset();
        }

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
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

        mToolbar.setTitle(query);

        //doSearch(query);

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MenuItem searchMenuItem = menu.findItem(R.id.search);
                searchMenuItem.expandActionView();
            }
        });

        LocationManagerCheck locationManagerCheck = new LocationManagerCheck(this);

        if (locationManagerCheck.isLocationServiceAvailable()) {
            if (locationManagerCheck.getProviderType() == 1) {
                   // mLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else if (locationManagerCheck.getProviderType() == 2) {
                 // mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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
                LatLng latLng = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
            }
        });

        showcaseBesiktas();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
           latitude_cur = mLastLocation.getLatitude();
            longitude_cur = mLastLocation.getLongitude();
            LatLng latLng = new LatLng(latitude_cur,longitude_cur);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
        }else{
            Toast.makeText(MapsActivity.this, "Couldn't get location.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    latitude_cur = mLastLocation.getLatitude();
                    longitude_cur = mLastLocation.getLongitude();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
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

        this.menu = menu;
        return true;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        selectedMarker = marker;
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;
        gMapButton.setVisibility(View.VISIBLE);
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
    private boolean isValid(String s) {
        Matcher mMatch = queryPattern.matcher(s);
        return mMatch.matches();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Maps Activity", "onConnectionFailed: Connection Failed");

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude_cur = location.getLatitude();
        longitude_cur = location.getLongitude();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if(isValid(query)){
            doSearch(query);
        }else{
            Toast.makeText(MapsActivity.this, "Invalid search parameters.", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void doSearch(String query){
        String params[] = {url, query};
        try{
            new webGetSetMarkers().execute(params);
        }catch (Exception e){
            Toast.makeText(MapsActivity.this, "Couldn't connect", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    //url, query, void ---- params[0], params[1], void
    private class webGetSetMarkers extends AsyncTask<String, Void, Void> {
        //loading screen(?)
        @Override
        protected void onPreExecute() {
            Toast.makeText(MapsActivity.this, "Getting info from url.", Toast.LENGTH_LONG).show();
            super.onPreExecute();
        }

        //main function to run
        @Override
        protected Void doInBackground(String... params) {
            RestClient rc = new RestClient();
            String jsonReply = rc.getStandardQueryJson(params[0], params[1]);

            if(!jsonReply.isEmpty()){
                QueryParser qp = new QueryParser();
                shopList = qp.parseShopList(jsonReply);
            }else{
                Toast.makeText(MapsActivity.this, "jSonReply is empty", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        //do after doInBackground is finished
        @Override
        protected void onPostExecute(Void result) {
            for (Shop sh : shopList) {
                mMap.addMarker(sh.getMarkerOptions());
            }
            super.onPostExecute(result);
        }

        /*@Override
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
                gMapButton.setVisibility(View.GONE);
                latitude = 0.0;
                longitude = 0.0;
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

