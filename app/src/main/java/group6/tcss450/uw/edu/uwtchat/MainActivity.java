package group6.tcss450.uw.edu.uwtchat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import group6.tcss450.uw.edu.uwtchat.model.ChatListAdapter;
import group6.tcss450.uw.edu.uwtchat.model.Notification;
import group6.tcss450.uw.edu.uwtchat.utils.ChatListManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ChatListAdapter.ChatListListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
        {

            private class DataUpdateReciever extends BroadcastReceiver {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(NotificationsService.RECEIVED_UPDATE)) {
                        Log.d("DemoActivity", "hey I just got your broadcast!");
                        String s = intent.getStringExtra(getString(R.string.keys_json_notifications));
                       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        ImageButton b = findViewById(R.id.notificationButton);
                        Log.e("bl", s);
                        if(s.isEmpty())
                            b.setVisibility(View.INVISIBLE);
                        else
                           b.setVisibility(View.VISIBLE);
                    }
                }
            }

    public static final String USER_KEY = "USER_KEY";
    public static final String CHAT_ID_KEY = "CHAT_ID_KEY";
    public static final String KEY = "tcss450.group6.MainActivity";
    public static final String RECENT_CHATS_KEY = "tcss450.group6.MainActivity.RECENT_CHATS_KEY";

    private String mCurrentUser;
    private String mChatId;
    private DataUpdateReciever mDataUpdateReceiver;
    private ChatListManager mListenManager;
    private Notification[] mPrevNotes;

    private NavigationView mNavigationView;

    private static final String TAG = "MainActivity";
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
                    UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private static final int MY_PERMISSIONS_LOCATIONS = 814;

    // Weather Fragment Variables
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private boolean searchCurrentLocation = true;
    private String mCurrentCity;
    private String mCurrentState;
    private String mActualCurrentCity;
    private String mActualCurrentState;
    private boolean firstTime = true;
    private Geocoder mGeocoder;
    private boolean zipMode = false;
    private boolean mapMode = false;
    private ArrayList<String> locationSpinner = new ArrayList<>();
    private ArrayList<String> mRecentChats = new ArrayList<>();

    // Returns the array that will be displayed in the spinner for weather
    public ArrayList<String> getLocationSpinnerArray() {
        return locationSpinner;
    }

    // Returns whether or not we are currently in Zip mode for weather fragment
    public boolean getZipMode() {
        return zipMode;
    }

    // Sets the zip mode to true or false
    public void setZipMode(boolean theMode) {
        zipMode = theMode;
    }

    // Sets the map mode to true or false
    public void setMapMode(boolean theMode) {
        mapMode = theMode;
    }

    // Returns whether or not we are currently in Map mode for weather fragment
    public boolean getMapMode() {
        return mapMode;
    }

    // Returns the geocoder for use in the weather fragment
    public Geocoder getGeocoder() {
        return mGeocoder;
    }

    // Sets whether or not to use the current location for the weather
    public void setCurrentLocationMode(boolean mode) {
        searchCurrentLocation = mode;
        if (mode) {
            if (mActualCurrentCity != null && mActualCurrentState != null) {
                mCurrentCity = mActualCurrentCity;
                mCurrentState = mActualCurrentState;
            }
            setZipMode(false);
            setMapMode(false);
        } else if (!zipMode && !mapMode) {
            List<String> split = Arrays.asList(locationSpinner.get(0).split(", "));
            String city = split.get(0);
            String state = split.get(1);
            mCurrentCity = city;
            mCurrentState = state;
        }
    }

    // Returns whether or not we are currently using the current location for weather
    public boolean getCurrentLocationMode() {
        return searchCurrentLocation;
    }

    // Sets the current city that is being used for weather
    public void setCurrentCity(String city) {
        mCurrentCity = city;
    }

    // Sets the current state that is being used for weather
    public void setCurrentState(String state) {
        mCurrentState = state;
    }

    // Empties out the spinner for use when trying to refresh spinner data
    public void emptySpinner() {
        locationSpinner = new ArrayList<String>();
    }

    // Adds to the end of the spinner for weather
    public void addToSpinner(String city, String state) {
        boolean found = false;
        for (int i = 0; i < locationSpinner.size(); i++) {
            if (locationSpinner.get(i).equals(city + ", " + state)) {
                found = true;
            }
        }
        if (!found) { // Only add to spinner if the city + state does not already exist in the array
            locationSpinner.add(city + ", " + state);
        }
    }


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);
        /*mCurrentUser = prefs.getString(getString(R.string.keys_prefs_username), "DEFAULT");*/

        Intent i = getIntent();
        if(i!=null)
            mCurrentUser = i.getStringExtra(USER_KEY);

        NotificationsService.startServiceAlarm(this, true, mCurrentUser);

        if (prefs.getStringSet("locationSpinner", null) != null) {
            searchCurrentLocation = prefs.getBoolean("searchCurrentLocation", true);
            mCurrentCity = prefs.getString("mCurrentCity", "Tacoma");
            mCurrentState = prefs.getString("mCurrentState", "Washington");
            locationSpinner = new ArrayList<String>(prefs.getStringSet("locationSpinner", null));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        View header = mNavigationView.getHeaderView(0);
        header.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.purple));

        //NotificationsService.startServiceAlarm(this, true, mCurrentUser);

        // Google API for weather fragment
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Requesting permissions for weather fragment
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                        , Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_LOCATIONS);
        }

            //This block checks if a notification started the activity.
            if(i != null)
            {
                String s = i.getStringExtra(KEY);
                if(s!=null)
                {
                    if(s.equals(getString(R.string.keys_fragment_individual_chat)))
                    {
                        firstTime = false;
                        onChatSelect(i.getStringExtra(CHAT_ID_KEY));
                        return;
                    }
                    else
                    {
                        firstTime = false;
                        loadFragment(new ReceivedConnections(), getString(R.string.keys_fragment_received_connections),false);
                        return;
                    }
                }

            }

        loadFragment(new HomeFragment(), getResources().getString(R.string.keys_fragment_home), false);
       // updateNavigationSelected();
        //mNavigationView.setCheckedItem(R.id.nav_home);
    }

    public void onNotificationButtonClick(View theV)
    {
        loadFragment(new HomeFragment(), getString(R.string.keys_fragment_home));
        //need to update navigation
    }

    // Returns the current location object
    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    // Returns the current city
    public String getCurrentCity() {
        return mCurrentCity;
    }

    // Returns the current state
    public String getCurrentState() {
        return mCurrentState;
    }

    // Requesting permissions for weather fragment usage
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_LOCATIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // locations-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("PERMISSION DENIED", "Nothing to see or do here.");

                    //Shut down the app. In production release, you would let the user
                    //know why the app is shutting down…maybe ask for permission again?
                    finishAndRemoveTask();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    public void onResume() {
        super.onResume();
                if (mDataUpdateReceiver == null) {
                    mDataUpdateReceiver = new DataUpdateReciever();
                }
                IntentFilter iFilter = new IntentFilter(NotificationsService.RECEIVED_UPDATE);
                registerReceiver(mDataUpdateReceiver, iFilter);
                mListenManager.startListening();
    }



    @Override
    public void onPause() {
        super.onPause();
        if (mDataUpdateReceiver != null) {
            unregisterReceiver(mDataUpdateReceiver);
        }
    }

    // Fixing the navigation drawer on back press
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(getFragmentManager().getBackStackEntryCount() > 1)
                getFragmentManager().popBackStackImmediate();
            else
                super.onBackPressed();
            onAttachFragment(getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer));
        }
    }

    // Loading the right fragment on navigation drawer item selection
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_chats) {
            //loadFragment(new IndividualChatFragment(), getResources().getString(R.string.keys_fragment_chat_list));
            ChatListFragment frag = new ChatListFragment();
            /*Bundle bundle = new Bundle();
            bundle.putString(USER_KEY, mCurrentUser);
            frag.setArguments(bundle);*/
            loadFragment(frag, getResources().getString(R.string.keys_fragment_chat_list));
        } else if (id == R.id.nav_home) {
            loadFragment(new HomeFragment(), getResources().getString(R.string.keys_fragment_home));
        } else if (id == R.id.nav_weather) {
            loadFragment(new WeatherFragment(), getResources().getString(R.string.keys_fragment_weather));
        } else if (id == R.id.nav_contacts) {
            loadFragment(new ConnectionsFragment(), getResources().getString(R.string.keys_fragment_contacts));
        } else if (id == R.id.nav_logout) {
            onLogout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    // Loads a specific fragment and attaches a tag to it
    public void loadFragment(Fragment frag, String tag) {
        loadFragment(frag, tag, true);
    }

    // Loads a specific fragment and attaches a tag to it, and adds it to the backstack if passed true
    public void loadFragment(Fragment frag, String tag, boolean addToBackStack) {
        Bundle bundle = new Bundle();
        bundle.putString(USER_KEY, mCurrentUser);
        bundle.putStringArrayList(RECENT_CHATS_KEY, mRecentChats);
        frag.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragmentContainer, frag, tag); // Added tag parameter to attach to the fragment
                if (addToBackStack) {
                    transaction.addToBackStack(null);
                }
        transaction.commit();
    }

    // Updates the navigation drawer to highlight the correct fragment that we are on
    @Override
    public void onAttachFragment(Fragment theFrag) {
        int id = -1;
        NavigationView navigationView = findViewById(R.id.nav_view);

        if(theFrag instanceof  HomeFragment)
            id = R.id.nav_home;
        else if(theFrag instanceof ChatListFragment || theFrag instanceof IndividualChatFragment)
            id = R.id.nav_chats;
        else if(theFrag instanceof ConnectionsFragment || theFrag instanceof SearchConnections ||
                theFrag instanceof ViewConnectionsFragment|| theFrag instanceof SentConnections)
            id = R.id.nav_contacts;
        else if(theFrag instanceof WeatherFragment)
            id = R.id.nav_weather;

        // Setting the currently selected item in the navigation to the navigation id if set
        if (id != -1) {
            navigationView.setCheckedItem(id);
        }
    }

    public void onLogout() {
        SharedPreferences prefs =
                getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        prefs.edit().remove(getString(R.string.keys_prefs_username));
        prefs.edit().putBoolean(
                getString(R.string.keys_prefs_stay_logged_in),
                false)
                .apply();
        //setContentView(R.layout.activity_launch);
        NotificationsService.stopServiceAlarm(this);
        Intent intent = new Intent(this, LaunchActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public String getUsername() {return mCurrentUser;}

    @Override
    public void onChatSelect(String theChatID) {
        Bundle bundle = new Bundle();
        mChatId = theChatID;
        if(mRecentChats.contains(mChatId) == false)
            mRecentChats.add(mChatId);
        bundle.putString(CHAT_ID_KEY, theChatID);
        bundle.putString(USER_KEY, mCurrentUser);
        IndividualChatFragment frag = new IndividualChatFragment();
        frag.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragmentContainer, frag, "IndividualChatFragmentTag"); // Added tag parameter to attach to the fragment
            transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public void onNewChatClick() {
        ViewConnectionsFragment frag = new ViewConnectionsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(USER_KEY, mCurrentUser);
        frag.setArguments(bundle);
        //replace this fragment with new chat fragment
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragmentContainer, frag, getString(R.string.keys_fragment_new_chat)) // Added tag parameter to attach to the fragment
                .addToBackStack(null);
        transaction.commit();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        if (searchCurrentLocation) {

        }
        super.onStart();
        createNotificationListener();
        //NotificationsService.stopServiceAlarm(this);
    }

    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        SharedPreferences prefs =
                getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("searchCurrentLocation", searchCurrentLocation);
        editor.putString("mCurrentCity", mCurrentCity);
        editor.putString("mCurrentState", mCurrentState);

        Set<String> set = new HashSet<String>(locationSpinner);

        editor.putStringSet("locationSpinner", set);
        editor.apply();

        super.onStop();
        mListenManager.stopListening();
        //NotificationsService.startServiceAlarm(this, true, mCurrentUser);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.

        if (mCurrentLocation == null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                mCurrentLocation =
                        LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                startLocationUpdates();
            }
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        List<Address> addressList;
        mGeocoder = new Geocoder(this, Locale.getDefault());
        try {
            addressList = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addressList.size() > 0) {
                mActualCurrentCity = addressList.get(0).getLocality(); // Storing the physical current city and state for future use
                mActualCurrentState = addressList.get(0).getAdminArea();
                if (searchCurrentLocation) {
                    mCurrentCity = addressList.get(0).getLocality(); // Changing the current city and state that are being used
                    mCurrentState = addressList.get(0).getAdminArea();
                }

                if (firstTime) {
                    firstTime = false; // Loading the home fragment only on the first time the app is opened
                    loadFragment(new HomeFragment(), getResources().getString(R.string.keys_fragment_home), false);
                    //updateNavigationSelected();
                    //mNavigationView.setCheckedItem(R.id.nav_home);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " +
                connectionResult.getErrorCode());
    }

    //The following is for the notification badge on the app bar.
    private void createNotificationListener()
    {
        Uri retrieve = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_unread_notifications))
                .appendQueryParameter(getString(R.string.keys_json_username), mCurrentUser)
                .build();

            //no record of a saved timestamp. must be a first time login
            mListenManager = new ChatListManager.Builder(retrieve.toString(),
                    this::publishProgress)
                    .setExceptionHandler(this::handleError)
                    .setDelay(1000)
                    .build();
    }

            private void publishProgress(JSONObject theJSON)
            {
                try {
                    if(theJSON.has(getString(R.string.keys_json_notifications))) {

                        JSONArray resultArray = theJSON.getJSONArray(getString(R.string.keys_json_notifications));
                        final Notification[] notes = new Notification[resultArray.length()];
                        for (int i = 0; i < resultArray.length(); i++) {
                            JSONObject theNotification = resultArray.getJSONObject(i);
                            String primaryKey = theNotification.getString(getString(R.string.keys_json_primary_key));
                            String sender = theNotification.getString(getString(R.string.keys_json_sender));
                            String message = theNotification.getString(getString(R.string.keys_json_message));
                            String chatid = theNotification.getString(getString(R.string.keys_json_chatid));
                            String timestamp = theNotification.getString(getString(R.string.keys_json_timestamp));
                            notes[i] = new group6.tcss450.uw.edu.uwtchat.model.Notification(sender, message, chatid, timestamp);
                        }

                        this.runOnUiThread(() -> {
                            ImageButton b = findViewById(R.id.notificationButton);
                            if(notes.length < 1)
                                b.setVisibility(View.INVISIBLE);
                            else
                                b.setVisibility(View.VISIBLE);

                            LinearLayout layout = findViewById(R.id.notificationScrollView);
                            if(layout != null)
                            {
                                layout.removeAllViews();
                                if(notes.length <1)
                                {
                                    TextView t = new TextView(getApplicationContext());
                                    t.setText("No notifications!");
                                    layout.addView(t);
                                } else {
                                    for (Notification n : notes) {
                                        TextView t = new TextView(getApplicationContext());
                                        t.setText(n.getMessage());
                                        if(n.isConnection())
                                            t.setOnClickListener((view)-> {
                                                if(notes.length == 1)
                                                    b.setVisibility(View.INVISIBLE);
                                                loadFragment(new ReceivedConnections(), getString(R.string.keys_fragment_received_connections),true);
                                            });
                                        else
                                            t.setOnClickListener((view)-> {
                                                if (notes.length == 1)
                                                    b.setVisibility(View.INVISIBLE);
                                                onChatSelect(n.getChatID());
                                            });
                                        layout.addView(t);
                                    }
                                }
                            }
                            mPrevNotes = notes;
                        });
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }

    private void handleError(final Exception e) {
        Log.e("LISTEN ERROR!!! (in MainActivity)", e.getMessage() + e.getCause());
    }
}
