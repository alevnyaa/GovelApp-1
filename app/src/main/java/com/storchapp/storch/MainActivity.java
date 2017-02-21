package com.storchapp.storch;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static final String TAG = "MainActivity";

    private AutoCompleteTextView searchBar;
    private ImageView logo;
    private static final Pattern queryPattern = Pattern.compile("[a-zA-Z \t/&]+");

    private Toolbar mToolbar;

    private Menu menu;

    private ListView suggestionList;

    private AccountHeader accountHeader = null;

    private AppCompatButton logInButton, signUpButton;

    ArrayAdapter<String> adapter;

    private static final int REQUEST_GET_ACCOUNTS = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.drawer_close, R.anim.drawer_open);
        setContentView(R.layout.activity_main);

        logo = (ImageView) findViewById(R.id.ic_launcher);
        searchBar = (AutoCompleteTextView) findViewById(R.id.searchBar);
        logInButton = (AppCompatButton) findViewById(R.id.btn_login);
        signUpButton = (AppCompatButton) findViewById(R.id.btn_sign_up);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        //drawer build
        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.foto_background)
                .addProfiles(
        new ProfileSettingDrawerItem().withName("Add Account").withDescription("Add new Google Account").withIdentifier(1))
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        if (profile instanceof IDrawerItem && ((IDrawerItem) profile).getIdentifier() == 1) {
                            Intent logIn = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(logIn);
                        }
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();


        PrimaryDrawerItem appName = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.app_name);
        SecondaryDrawerItem settings = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.settings);
        SecondaryDrawerItem webSite = new SecondaryDrawerItem().withIdentifier(3).withName(R.string.web_site);
        SecondaryDrawerItem feedback = new SecondaryDrawerItem().withIdentifier(4).withName(R.string.feedback);
        SecondaryDrawerItem privacy = new SecondaryDrawerItem().withIdentifier(5).withName(R.string.privacy_policy);
        SecondaryDrawerItem favs = new SecondaryDrawerItem().withIdentifier(6).withName(R.string.favourites);

        DrawerBuilder mdrawer = new DrawerBuilder();
        mdrawer.withAccountHeader(accountHeader)
                .withActivity(this)
                .withToolbar(mToolbar)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        appName,
                        new DividerDrawerItem(),
                        favs,
                        settings,
                        feedback,
                        privacy,
                        webSite
                ).build();

        mdrawer.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                Toast.makeText(MainActivity.this, "Item pressed " + position, Toast.LENGTH_SHORT).show();
                switch(position){
                    case 1:
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.storchapp.com"));
                        startActivity(browserIntent);
                        break;

                    case 3:
                        Intent favs = new Intent(MainActivity.this,FavouritesActivity.class);
                        startActivity(favs);
                        break;

                    case 4:
                        Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settings);
                        break;

                    case 5:
                        Intent feedBack = new Intent(MainActivity.this, FeedbackActivity.class);
                        startActivity(feedBack);
                        break;

                    case 6:
                        Intent privacy = new Intent(MainActivity.this, FeedbackActivity.class);
                        startActivity(privacy);
                        break;
                }
                return true;
            }
        });

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

        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                    startActivity(searchIntent);
                    searchBar.clearFocus();
                }
            }
        });

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logInIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(logInIntent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_GET_ACCOUNTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_toolbar_menu, menu);
        menu.findItem(R.id.search).setVisible(false);

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                logo.setVisibility(View.VISIBLE);
                searchBar.setVisibility(View.VISIBLE);
                signUpButton.setVisibility(View.VISIBLE);
                logInButton.setVisibility(View.VISIBLE);
                mToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                searchBar.clearFocus();
                return true;
            }
        });

        this.menu = menu;
        return true;
    }

  /*  @Override
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
                signUpButton.setVisibility(View.VISIBLE);
                logInButton.setVisibility(View.VISIBLE);
                mToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                searchBar.clearFocus();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/

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