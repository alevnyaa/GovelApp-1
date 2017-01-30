package com.govelapp.govelapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.view.animation.Animation.AnimationListener;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.AlphaAnimation;
import android.view.animation.AccelerateInterpolator;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    public static final String TAG = "MainActivity";

    private AutoCompleteTextView searchBar;
    private ImageView logo;
    private static final Pattern queryPattern = Pattern.compile("[a-zA-Z \t/&]+");

    private Toolbar mToolbar;

    private boolean hasSearchItem = false;

    private Menu menu;

    private ListView suggestionList;

    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logo = (ImageView) findViewById(R.id.ic_launcher);
        searchBar = (AutoCompleteTextView) findViewById(R.id.searchBar);
        suggestionList = (ListView) findViewById(R.id.listview);

        suggestionList.setVisibility(View.GONE);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        //drawer build
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
        SecondaryDrawerItem webSite = new SecondaryDrawerItem().withIdentifier(3).withName(R.string.web_site);
        SecondaryDrawerItem feedback = new SecondaryDrawerItem().withIdentifier(4).withName(R.string.feedback);
        SecondaryDrawerItem privacy = new SecondaryDrawerItem().withIdentifier(5).withName(R.string.privacy_policy);
        SecondaryDrawerItem favs = new SecondaryDrawerItem().withIdentifier(6).withName(R.string.favourites);

        new DrawerBuilder().withAccountHeader(accountHeader)
                .withActivity(this)
                .withToolbar(mToolbar)
                .addDrawerItems(
                        appName,
                        new DividerDrawerItem(),
                        favs,
                        settings,
                        feedback,
                        privacy,
                        webSite
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Toast.makeText(MainActivity.this, "Item pressed " + position, Toast.LENGTH_SHORT).show();
                        if(position == 7){
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://www.storchapp.com"));
                            startActivity(browserIntent);
                        }
                        return true;
                    }
                })
                .build();


        //will get from our database per week
        String[] items = {"Market & Food/Food/Cheese",
                "Market & Food/Food/Kasar Cheese",
                "Market & Food/Food/Cream Cheese",
                "Market & Food/Food/Vodka",
                "Market & Food/Food/Rum",
                "Market & Food/Food/Raki",
                "Grocery/Vegetables/Chilli",
                "Grocery/Vegetables/Green Pepper",
                "Grocery/Vegetables/Chili",
                "Grocery/Fruits/Strawberry",
                "Grocery/Fruits/Grape",
                "Grocery/Fruits/Grapefruit",
                "Copy & Print/Print/Letterhead",
                "Copy & Print/Print/Invitation Card Printing",
                "Copy & Print/Print/Pillow Printing",
                "Copy & Print/Print/Scanning",
                "Copy & Print/Print/Photocopy"};

        adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, items);
        suggestionList.setAdapter(adapter);

        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    logo.setVisibility(View.GONE);
                    //searchBar.setY(250);
                    searchBar.setVisibility(View.GONE);

                    if (!hasSearchItem) {
                        // Retrieve the SearchView and plug it into SearchManager
                        final SearchView searchView = (SearchView) MenuItemCompat
                                .getActionView(menu.findItem(R.id.search));
                        final SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
                        final MenuItem searchMenuItem = menu.findItem(R.id.search);
                        searchView.setSearchableInfo(searchManager
                                .getSearchableInfo(getComponentName()));
                        searchMenuItem.expandActionView();
                        searchView.requestFocus();
                        searchView.setOnQueryTextListener(MainActivity.this);
                        hasSearchItem = true;
                    } else {
                        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
                        final SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
                        final MenuItem searchMenuItem = menu.findItem(R.id.search);
                        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                        searchMenuItem.expandActionView();
                        searchView.requestFocus();
                        searchView.setOnQueryTextListener(MainActivity.this);
                    }

                }
            }
        });
        //writes the text to searchBar
        searchBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = searchBar.getText().toString();
                searchBar.setText(s);
                searchBar.setSelection(s.length()); //set the cursor position
                doSearch(s);
            }
        });

        //search starter for keyboard
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int i, KeyEvent keyEvent) {
                String query = searchBar.getText().toString();
                if (i == EditorInfo.IME_ACTION_SEARCH && query.length() > 0 && isValid(query)) {
                    doSearch(query);
                    return true;
                } else {
                    Toast.makeText(MainActivity.this, "Invalid search parameters.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: " + item.getItemId());
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_toolbar_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: " + keyCode);
       if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(logo.isShown()){
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }else{
                logo.setVisibility(View.VISIBLE);
                searchBar.setVisibility(View.VISIBLE);
                searchBar.clearFocus();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void doSearch(String query) {
        Intent queryIntent = new Intent(MainActivity.this, MapsActivity.class);
        Log.d(TAG, query);
        queryIntent.putExtra("query", query);
        startActivity(queryIntent);
    }

    //returns true if its a valid query
    private boolean isValid(String s) {
        Matcher mMatch = queryPattern.matcher(s);
        return mMatch.matches();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if(isValid(query)){
            doSearch(query);
        }else{
            Toast.makeText(MainActivity.this, "Invalid search parameters.", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}