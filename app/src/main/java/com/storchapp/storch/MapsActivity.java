package com.storchapp.storch;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import android.support.v4.view.*;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
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

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.model.ExpandableBadgeDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.ToggleDrawerItem;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener,
        SearchView.OnQueryTextListener {
    //our valid characters OnMapReadyCallback
    private static final Pattern queryPattern = Pattern.compile("[a-zA-Z \t]+");
    private GoogleMap mMap;
    private String url = "govelapp.com/api";     //getResources().getString(R.string.url);
    private List<Shop> shopList;
    private String query;

    private SlidingUpPanelLayout slidingLayout;
    private TabLayout mTabLayout;
    private Toolbar mToolbar;

    private double latitude, longitude, longitude_cur, latitude_cur;

    private Marker marker;

    private Location mBestLocation;
    private LocationRequest mLocationRequest;

    private GoogleApiClient mGoogleApiClient;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    private FloatingActionButton settingsButton, gMapButton, mLocationButton;

    private Menu menu;

    private Drawer mDrawer = null;

    private Drawer rightDrawer = null;

    private TextView slidingDrawerTextView;

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
        slidingDrawerTextView = (TextView) findViewById(R.id.shopNameText);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);

        //set tabLayout functionality
        mTabLayout.addTab(mTabLayout.newTab().setText("Info"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Search in Store"));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        final android.support.v4.view.PagerAdapter mPagerAdapter =
                new com.storchapp.storch.PagerAdapter(getSupportFragmentManager(),mTabLayout.getTabCount());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("tab switch", tab.toString());
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //test purposed

        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(true)
                .withHeaderBackground(R.drawable.foto_background)
                .addProfiles(
                        new ProfileDrawerItem().withName("Kenan Soylu")
                                .withEmail("adsasd@gmail.com").withIcon(R.drawable.ic_launcher),
                        new ProfileSettingDrawerItem().withName("Add Account")
                                .withDescription("Add new Google Account")
                                .withIcon(FontAwesome.Icon.faw_plus).withIdentifier(1))
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        if (profile instanceof IDrawerItem && ((IDrawerItem) profile).getIdentifier() == 1) {
                            Intent logIn = new Intent(MapsActivity.this, LoginActivity.class);
                            startActivity(logIn);
                        }
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        PrimaryDrawerItem appName = new PrimaryDrawerItem().withIdentifier(1)
                .withName(R.string.app_name).withIcon(FontAwesome.Icon.faw_home);
        SecondaryDrawerItem settings = new SecondaryDrawerItem().withIdentifier(2)
                .withName(R.string.settings).withIcon(FontAwesome.Icon.faw_optin_monster);
        SecondaryDrawerItem webSite = new SecondaryDrawerItem().withIdentifier(3)
                .withName(R.string.web_site).withIcon(FontAwesome.Icon.faw_internet_explorer);
        SecondaryDrawerItem rateUs = new SecondaryDrawerItem().withIdentifier(4)
                .withName(R.string.rate_us).withIcon(FontAwesome.Icon.faw_file_text);
        SecondaryDrawerItem privacy = new SecondaryDrawerItem().withIdentifier(5)
                .withName(R.string.privacy_policy).withIcon(FontAwesome.Icon.faw_lock);
        SecondaryDrawerItem favs = new SecondaryDrawerItem().withIdentifier(6)
                .withName(R.string.favourites).withIcon(FontAwesome.Icon.faw_heart);

        mDrawer = new DrawerBuilder().withAccountHeader(accountHeader)
                .withSavedInstance(savedInstanceState)
                .withActivity(this)
                .withActionBarDrawerToggleAnimated(true)
                .withToolbar(mToolbar)
                .addDrawerItems(
                        appName,
                        new DividerDrawerItem(),
                        favs,
                        settings,
                        rateUs,
                        privacy,
                        webSite
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                switch (position) {
                    case 1:
                        onBackPressed();
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

                        break;

                    case 6:

                        break;

                    case 7:
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.storchapp.com"));
                        startActivity(browserIntent);
                        break;

                }
                return true;
            }
        }).build();

        rightDrawer = new DrawerBuilder()
                .withDisplayBelowStatusBar(true)
                .withSavedInstance(savedInstanceState)
                .withActivity(this)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(1).withName(R.string.app_name),
                        new DividerDrawerItem(),
                        new ExpandableBadgeDrawerItem().withName(R.string.settings).withSelectable(false)
                                .withSubItems(
                                new SwitchDrawerItem().withName("Show only nearest")
                                        .withIcon(FontAwesome.Icon.faw_location_arrow),
                                new SwitchDrawerItem().withName("Pins")
                                        .withIcon(FontAwesome.Icon.faw_map_pin)
                                )
        ).withDrawerGravity(Gravity.END).build();

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rightDrawer.openDrawer();
            }
        });

        if (mBestLocation != null) {
            mBestLocation.reset();
        }

        slidingLayout.addPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel,
                                            SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED &&
                        previousState == SlidingUpPanelLayout.PanelState.DRAGGING){
                    SpannableStringBuilder ss = new SpannableStringBuilder();
                    Spannable snippet = new SpannableString(marker.getSnippet());
                    snippet.setSpan(new ForegroundColorSpan(Color.BLACK),0
                            ,snippet.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ss.append(marker.getTitle());
                    ss.append("\n");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ss.append(snippet, new RelativeSizeSpan(0.6f),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }else{
                        ss.append(snippet);
                    }
                    slidingDrawerTextView.setText(ss);
                }else if(previousState == SlidingUpPanelLayout.PanelState.DRAGGING &&
                        newState == SlidingUpPanelLayout.PanelState.COLLAPSED){
                    slidingDrawerTextView.setText(marker.getTitle());
                }
            }
        });
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
                LatLng latLng = new LatLng(mMap.getMyLocation()
                        .getLatitude(), mMap.getMyLocation().getLongitude());
                if(latLng != null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
                }else{
                    Toast.makeText(MapsActivity.this, "Location not available.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        showcaseBesiktas();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            mBestLocation = bestLastKnownLocation(MIN_LAST_READ_ACCURACY, FIVE_MIN);
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
                LocationServices.FusedLocationApi
                        .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

                // Schedule a runnable to unregister location listeners
                Executors.newScheduledThreadPool(1).schedule(new Runnable() {

                    @Override
                    public void run() {
                        LocationServices.FusedLocationApi
                                .removeLocationUpdates(mGoogleApiClient, MapsActivity.this);
                    }

                }, ONE_MIN, TimeUnit.MILLISECONDS);
            }
        }
    }

  //TODO:must be updated to get last known locattion of the user
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

 //TODO:need permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Toast.makeText(MapsActivity.this, "Location permission", Toast.LENGTH_SHORT).show();
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationServices.FusedLocationApi
                            .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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
        this.marker = marker;
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
    public void onBackPressed() {
        final MenuItem searchMenuItem = menu.findItem(R.id.search);

        if (mDrawer != null && mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        }else if(rightDrawer != null && rightDrawer.isDrawerOpen()){
            rightDrawer.closeDrawer();
        } else if(slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }else if(searchMenuItem.isActionViewExpanded()){
            searchMenuItem.collapseActionView();
        }else if(slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            gMapButton.setVisibility(View.GONE);
        }else{
                super.onBackPressed();
            }
        }
    }


