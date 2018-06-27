package group6.tcss450.uw.edu.uwtchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * The Map that will be used for the weather fragment
 *
 * Worked on by Matthew
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lng";

    private GoogleMap mGoogleMap;
    private double mLat, mLng;
    private Marker mCurrentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mLat = getIntent().getDoubleExtra(LATITUDE, 0.0);
        mLng = getIntent().getDoubleExtra(LONGITUDE, 0.0);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.mapSearchButton).setOnClickListener(v1 -> {
            EditText theEditText = (EditText)findViewById(R.id.mapEditText);
            InputMethodManager imm = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(theEditText.getWindowToken(), 0);
            String searchResult = theEditText.getText().toString();
            List<Address> addressList;
            if (!searchResult.equals("")) {
                Geocoder geocoder = new Geocoder(this);
                try {
                    addressList = geocoder.getFromLocationName(searchResult, 1);
                    if (addressList != null && addressList.size() > 0) { // Found an address
                        Address newLocation = addressList.get(0);
                        LatLng latLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
                        onMapClick(latLng);
                    } else {
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(this, "Location not found", duration);
                        toast.show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        LatLng latLng = new LatLng(mLat, mLng);
        mGoogleMap.addMarker(new MarkerOptions().
                position(latLng).
                title("Marker"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        mGoogleMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mCurrentMarker == null) {
            mCurrentMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("New Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))); // Custom marker
        } else {
            mCurrentMarker.setPosition(latLng); // Moves the current marker instead of making a new one
        }

        DialogInterface.OnClickListener yesNoListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int choice) {
                switch (choice) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent intent = new Intent();
                        intent.putExtra("locationValue", latLng);
                        setResult(2, intent);
                        finish(); // Returning data back to weather fragment
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), 500, new GoogleMap.CancelableCallback() {
            public void onFinish() { // Animate the camera, and confirmation popup only when animation is complete
                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                builder.setMessage("Choose this as your new location?")
                        .setPositiveButton("Yes", yesNoListener)
                        .setNegativeButton("No", yesNoListener).show();
            }

            public void onCancel() {
            }
        });
    }
}
