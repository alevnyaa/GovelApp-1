package com.storchapp.storch;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
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

import com.storchapp.storch.jsonparser.QueryParser;
import com.storchapp.storch.locationmenager.LocationManagerCheck;
import com.storchapp.storch.restclient.RestClient;
import com.storchapp.storch.shopclasses.Shop;
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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CONTACTS;

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

    private Location mBestLocation;
    private LocationRequest mLocationRequest;

    private GoogleApiClient mGoogleApiClient;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    private FloatingActionButton settingsButton, gMapButton, mLocationButton;

    private Menu menu;

    private static final long ONE_MIN = 1000 * 60;
    private static final long TWO_MIN = ONE_MIN * 2;
    private static final long FIVE_MIN = ONE_MIN * 5;
    private static final long POLLING_FREQ = 1000 * 30;
    private static final long FASTEST_UPDATE_FREQ = 1000 * 5;
    private static final float MIN_ACCURACY = 25.0f;
    private static final float MIN_LAST_READ_ACCURACY = 500.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //basic setup
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.drawer_close, R.anim.drawer_open);
        setContentView(R.layout.activity_maps);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(POLLING_FREQ);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_FREQ);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }

        //to make the activity fullscreen
       /* getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

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
                        new ProfileDrawerItem().withName("Kenan Soylu")
                                .withEmail("adsasd@gmail.com").withIcon(getResources()
                                .getDrawable(R.drawable.black_marker))
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

        DrawerBuilder mDrawer = new DrawerBuilder();
        mDrawer.withAccountHeader(accountHeader)
                .withActivity(this)
                .withActionBarDrawerToggleAnimated(true)
                .withToolbar(mToolbar)
                .addDrawerItems(
                        appName,
                        new DividerDrawerItem(),
                        favs,
                        settings,
                        feedback,
                        privacy,
                        url
                ).build();
        mDrawer.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                switch (position) {
                    case 1:
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.storchapp.com"));
                        startActivity(browserIntent);
                        break;

                    case 3:
                        Intent favs = new Intent(MapsActivity.this, FavouritesActivity.class);
                        startActivity(favs);
                        break;

                    case 4:
                        Intent settings = new Intent(MapsActivity.this, SettingsActivity.class);
                        startActivity(settings);
                        break;

                    case 5:
                        Intent feedBack = new Intent(MapsActivity.this, FeedbackActivity.class);
                        startActivity(feedBack);
                        break;

                    case 6:
                        Intent privacy = new Intent(MapsActivity.this, FeedbackActivity.class);
                        startActivity(privacy);
                        break;
                }
                return true;
            }
        });

        if (mBestLocation != null) {
            mBestLocation.reset();
        }

        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
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
                    Intent intent = new Intent(Intent.ACTION_VIEW,
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
                //TODO: Getting Location works for now but needs update
                //LatLng latLng = new LatLng(mBestLocation.getLatitude(), mBestLocation.getLongitude());
                LatLng latLng = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));

            }
        });

       /* settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    //test purposed, barely works
                    getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                    Drawer d = new DrawerBuilder().withActivity(MapsActivity.this).withRootView(R.id.main_frame).build();
                    d.openDrawer();
                }
            }
        });*/

        showcaseBesiktas();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            mBestLocation = bestLastKnownLocation(MIN_LAST_READ_ACCURACY, FIVE_MIN);

            if (null == mBestLocation
                    || mBestLocation.getAccuracy() > MIN_LAST_READ_ACCURACY
                    || mBestLocation.getTime() < System.currentTimeMillis() - TWO_MIN) {

                // Get the best most recent location currently available
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                        (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {
                        ActivityCompat.requestPermissions(MapsActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

                // Schedule a runnable to unregister location listeners
                Executors.newScheduledThreadPool(1).schedule(new Runnable() {

                    @Override
                    public void run() {
                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MapsActivity.this);
                    }

                }, ONE_MIN, TimeUnit.MILLISECONDS);
            }
        }
    }


    private Location bestLastKnownLocation(float minAccuracy, long minTime) {
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;

        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCurrentLocation != null) {
            float accuracy = mCurrentLocation.getAccuracy();
            long time = mCurrentLocation.getTime();

            if (accuracy < bestAccuracy) {
                bestResult = mCurrentLocation;
                bestAccuracy = accuracy;
                bestTime = time;
            }
        }


        // Return best reading or null
        if (bestAccuracy > minAccuracy || bestTime < minTime) {
            return null;
        } else {
            return bestResult;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Toast.makeText(MapsActivity.this, "Location permission", Toast.LENGTH_SHORT).show();
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                    if(mBestLocation != null){
                        latitude_cur = mBestLocation.getLatitude();
                        longitude_cur = mBestLocation.getLongitude();
                    }
                } else {
                    // Disable the
                    // functionality that depends on this permission.
                    mGoogleApiClient.disconnect();
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onResume(){
        super.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maps_activity_toolbar_menu, menu);
        menu.findItem(R.id.search).setVisible(false);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        this.menu = menu;
        return true;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
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
        if(location.getLatitude() != 0.0 && location.getLongitude() != 0.0){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            latitude_cur = location.getLatitude();
            longitude_cur = location.getLongitude();
            LatLng latLng = new LatLng(latitude_cur,longitude_cur);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
        }
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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
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
            final MenuItem searchMenuItem = menu.findItem(R.id.search);
            if(slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED
                    || slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED){
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                gMapButton.setVisibility(View.GONE);
                latitude = 0.0;
                longitude = 0.0;
            }else if(searchMenuItem.isActionViewExpanded()){
                searchMenuItem.collapseActionView();
            }else{
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

