package group6.tcss450.uw.edu.uwtchat;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import group6.tcss450.uw.edu.uwtchat.utils.SendPostAsyncTask;


/**
 * Weather Fragment that displays weather data from Wunderground
 *
 * Worked on by Matthew
 */
public class WeatherFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

    private TextView currentTemperatureText;
    private JSONObject result;
    private JSONManager manager;
    private Spinner mSpinner;
    boolean userTouched = false; // Used for manually triggering the spinner item selection

    // When the spinner has been clicked by the user to make sure it is the only thing that triggers spinner item selection
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        userTouched = true;
        return false;
    }

    public WeatherFragment() {
        // Required empty public constructor
    }

    // Updates the spinner to load from the array that is stored in the MainActivity
    @SuppressLint("ResourceAsColor")
    private void updateSpinner(View v) {
        MainActivity theActivity = (MainActivity)getActivity();
        ArrayList<String> theArray = theActivity.getLocationSpinnerArray();
        if (theArray.size() == 0 && !theActivity.getMapMode() && !theActivity.getZipMode()) {
            theActivity.setCurrentLocationMode(true); // If the spinner is empty from deletion, automatically set to current location
            try {
                updateCondition();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (theActivity.getCurrentLocationMode()) { // Disable current location button and enable everything else
            v.findViewById(R.id.currentLocationButton).setEnabled(false);
            mSpinner.setVisibility(View.INVISIBLE);
            v.findViewById(R.id.savedLocationsButton).setEnabled(true);
            v.findViewById(R.id.weatherLocationTextView).setVisibility(View.INVISIBLE);
        }
        if (theActivity.getZipMode()) { // Enable all buttons ad hide spinner
            v.findViewById(R.id.currentLocationButton).setEnabled(true);
            mSpinner.setVisibility(View.INVISIBLE);
            v.findViewById(R.id.savedLocationsButton).setEnabled(true);
            v.findViewById(R.id.weatherLocationTextView).setVisibility(View.INVISIBLE);
        }
        if (theActivity.getMapMode()) { // Enable all buttons and hide spinner
            v.findViewById(R.id.currentLocationButton).setEnabled(true);
            mSpinner.setVisibility(View.INVISIBLE);
            v.findViewById(R.id.savedLocationsButton).setEnabled(true);
            v.findViewById(R.id.weatherLocationTextView).setVisibility(View.INVISIBLE);
        }
        if (!theActivity.getCurrentLocationMode() && !theActivity.getZipMode() && !theActivity.getMapMode()) {
            mSpinner.setVisibility(View.VISIBLE); // Show the spinner and disable the show spinner button while enabling everything else
            v.findViewById(R.id.currentLocationButton).setEnabled(true);
            v.findViewById(R.id.savedLocationsButton).setEnabled(false);
            v.findViewById(R.id.weatherLocationTextView).setVisibility(View.VISIBLE);
        }
        if (theArray.size() == 0) {
            v.findViewById(R.id.savedLocationsButton).setEnabled(false); // Disable the saved locations button if the spinner is empty
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, theArray);
        adapter.setDropDownViewResource(R.layout.dropdown_layout); // Sets the drop down to a custom dropdown layout
        mSpinner.setAdapter(adapter);

        if (v.findViewById(R.id.weatherLocationTextView).getVisibility() == View.VISIBLE) {
            for (int i = 0; i < theArray.size(); i++) {
                if (theArray.get(i).equals(theActivity.getCurrentCity() + ", " + theActivity.getCurrentState())) {
                    mSpinner.setSelection(i); // Mainly for use when loading the fragment. Remembers the user's choice
                }
            }
        }

        // Checking if the current city + state is found in the array, and enabling/disabling the save/delete buttons
        boolean found = false;
        for (int i = 0; i < theArray.size(); i++) {
            if (theArray.get(i).equals(theActivity.getCurrentCity() + ", " + theActivity.getCurrentState())) {
                found = true;
            }
        }
        if (found) {
            v.findViewById(R.id.weatherSaveButton).setEnabled(false);
            v.findViewById(R.id.weatherSaveButton).setBackgroundResource(R.color.lightGray);
            v.findViewById(R.id.weatherDeleteButton).setEnabled(true);
            v.findViewById(R.id.weatherDeleteButton).setBackgroundResource(R.color.colorPrimaryDark);
        } else {
            v.findViewById(R.id.weatherSaveButton).setEnabled(true);
            v.findViewById(R.id.weatherSaveButton).setBackgroundResource(R.color.colorPrimaryDark);
            v.findViewById(R.id.weatherDeleteButton).setEnabled(false);
            v.findViewById(R.id.weatherDeleteButton).setBackgroundResource(R.color.lightGray);
        }
    }

    // Updates the arraylist from retrieved data from the database of saved locations
    @SuppressLint("ResourceAsColor")
    private void updateList(JSONArray savedLocations) {
        MainActivity theActivity = (MainActivity) getActivity();
        theActivity.emptySpinner();

        for (int i = 0; i < savedLocations.length(); i++) {
            try {
                JSONObject theLocationSaved = savedLocations.getJSONObject(i);
                String location = theLocationSaved.get("location").toString();
                List<String> split = Arrays.asList(location.split(", "));
                String city = split.get(0);
                String state = split.get(1);
                theActivity.addToSpinner(city, state);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_weather, container, false);

        MainActivity theActivity = (MainActivity)getActivity();

        mSpinner = (Spinner) v.findViewById(R.id.locationSpinner);

        String retrieve = "https://tcss450-group-6.herokuapp.com/getLocations?username=" + theActivity.getUsername();

        new loadLocationsList().execute(retrieve);

        updateSpinner(v);

        mSpinner.setOnItemSelectedListener(this);
        mSpinner.setOnTouchListener(this);

        currentTemperatureText = (TextView) v.findViewById(R.id.currentTemperature);

        try {
            updateCondition();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button currentLocationButton = v.findViewById(R.id.currentLocationButton);
        currentLocationButton.setOnClickListener(v1 -> {
            theActivity.setCurrentLocationMode(true);
            updateSpinner(v);
            try {
                updateCondition();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        Button saveButton = v.findViewById(R.id.weatherSaveButton);
        saveButton.setOnClickListener(v1 -> {
            onSave();
        });

        Button deleteButton = v.findViewById(R.id.weatherDeleteButton);
        deleteButton.setOnClickListener(v1 -> {
            onDelete();
        });

        Button savedButton = v.findViewById(R.id.savedLocationsButton);
        savedButton.setOnClickListener(v1 -> {
            theActivity.setCurrentLocationMode(false);
            theActivity.setZipMode(false);
            theActivity.setMapMode(false);
            updateSpinner(v);
            try {
                updateCondition();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            boolean found = false;
            userTouched = true;
            for (int i = 0; i < theActivity.getLocationSpinnerArray().size(); i++) {
                if ((theActivity.getCurrentCity() + ", " + theActivity.getCurrentState()).equals(theActivity.getLocationSpinnerArray().get(i))) {
                    mSpinner.setSelection(i);
                    found = true; // Upon showing the spinner make it select the current city + state if found
                }
            }
            if (!found) {
                mSpinner.setSelection(0); // Otherwise default to show the very first element
            }
        });

        Button zipCodeButton = v.findViewById(R.id.zipCodeButton);
        zipCodeButton.setOnClickListener(v1 -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext()); // Creating zip code dialog popup
            alert.setTitle("Zip Code Search");
            alert.setMessage("Input Zip Code");
            final EditText input = new EditText(getContext());
            input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(5)});
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            alert.setView(input);
            alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String result = input.getText().toString();

                    Location mCurrentLocation = theActivity.getCurrentLocation();
                    List<Address> addressList;
                    Geocoder mGeocoder = theActivity.getGeocoder();
                    try {
                        addressList = mGeocoder.getFromLocationName(result, 1);
                        if (addressList != null && addressList.size() > 0) { // Found an address at given zip code
                            getView().findViewById(R.id.currentLoading).setVisibility(View.VISIBLE);

                            getView().findViewById(R.id.currentTemperature).setVisibility(View.INVISIBLE);
                            getView().findViewById(R.id.conditionLabel).setVisibility(View.INVISIBLE);
                            getView().findViewById(R.id.humidityLabel).setVisibility(View.INVISIBLE);
                            getView().findViewById(R.id.currentLocationLabel).setVisibility(View.INVISIBLE);

                            theActivity.setZipMode(true);
                            theActivity.setMapMode(false);
                            theActivity.setCurrentCity(addressList.get(0).getLocality());
                            theActivity.setCurrentState(addressList.get(0).getAdminArea());
                            theActivity.setCurrentLocationMode(false);
                            updateSpinner(v);
                            try {
                                updateCondition();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(getContext(), "Invalid Zip Code", duration);
                            toast.show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            AlertDialog dialog = alert.show();
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        });

        Button mapButton = v.findViewById(R.id.mapSearchButton);
        mapButton.setOnClickListener(v1 -> {
            List<Address> addressList;
            Geocoder mGeocoder = theActivity.getGeocoder();
            try {
                addressList = mGeocoder.getFromLocationName((theActivity.getCurrentCity() + ", " + theActivity.getCurrentState()), 5);
                if (addressList != null && addressList.size() > 0) {
                    for (int a = 0; a < addressList.size(); a++) {
                        if (addressList.get(a).hasLatitude() && addressList.get(a).hasLongitude()) { // If a location has been found
                            Intent i = new Intent(getContext(), MapActivity.class);
                            i.putExtra(MapActivity.LATITUDE, addressList.get(a).getLatitude());
                            i.putExtra(MapActivity.LONGITUDE, addressList.get(a).getLongitude());
                            startActivityForResult(i, 1);
                        }
                    }
                } else { // Some locations aren't found on the google map for some reason, so defaulting to current location if necessary
                    Intent i = new Intent(getContext(), MapActivity.class);
                    i.putExtra(MapActivity.LATITUDE, theActivity.getCurrentLocation().getLatitude());
                    i.putExtra(MapActivity.LONGITUDE, theActivity.getCurrentLocation().getLongitude());
                    startActivityForResult(i, 1);
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(getContext(), "Unable to find " + theActivity.getCurrentCity() + ", " + theActivity.getCurrentState() + " on the map. Using current location instead.", duration);
                    toast.show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return v;
    }

    // Retrieving data back from the Map
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == 2) {
                LatLng latLng = (LatLng) data.getExtras().get("locationValue");
                MainActivity theActivity = (MainActivity) getActivity();
                List<Address> addressList;
                Geocoder mGeocoder = theActivity.getGeocoder();

                try {
                    addressList = mGeocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        getView().findViewById(R.id.currentLoading).setVisibility(View.VISIBLE);

                        getView().findViewById(R.id.currentTemperature).setVisibility(View.INVISIBLE);
                        getView().findViewById(R.id.conditionLabel).setVisibility(View.INVISIBLE);
                        getView().findViewById(R.id.humidityLabel).setVisibility(View.INVISIBLE);
                        getView().findViewById(R.id.currentLocationLabel).setVisibility(View.INVISIBLE);

                        theActivity.setZipMode(false);
                        theActivity.setMapMode(true);
                        theActivity.setCurrentLocationMode(false);
                        theActivity.setCurrentCity(addressList.get(0).getLocality());
                        theActivity.setCurrentState(addressList.get(0).getAdminArea());
                        updateSpinner(getView());
                        try {
                            updateCondition();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(getContext(), "Invalid Map Location", duration);
                        toast.show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Used on fragment loading to load the spinner list (retrieve data from database)
    private class loadLocationsList extends AsyncTask<String, String, String> {

        protected String doInBackground(String... theURL) {

            Uri retrieve = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_get_locations))
                    .appendQueryParameter("name", ((MainActivity)getActivity()).getUsername())
                    .build();

            StringBuilder response = new StringBuilder();
            HttpURLConnection urlConnection = null;
            response = new StringBuilder();
            try {
                URL urlObject = new URL(retrieve.toString());
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s;
                while ((s = buffer.readLine()) != null) {
                    response.append(s);
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            String responseString = response.toString();
            try {
                JSONObject resultObject = new JSONObject(responseString);
                JSONArray resultArray = resultObject.getJSONArray("locations");
                updateList(resultArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            updateSpinner(getView());
        }
    }

    // When the user presses the save button
    public void onSave()
    {
        JSONObject messageJson = new JSONObject();
        MainActivity theActivity = (MainActivity)getActivity();
        try {
            messageJson.put("name", theActivity.getUsername());
            messageJson.put("location", theActivity.getCurrentCity() + ", " + theActivity.getCurrentState());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String mSaveUrl = "https://tcss450-group-6.herokuapp.com/addLocation";

        new SendPostAsyncTask.Builder(mSaveUrl, messageJson)
                .onPostExecute(this::endOfSaveTask)
                .onCancelled(this::handleError)
                .build().execute();
    }

    // When the user presses the delete button
    public void onDelete()
    {
        JSONObject messageJson = new JSONObject();
        MainActivity theActivity = (MainActivity)getActivity();
        try {
            messageJson.put("name", theActivity.getUsername());
            messageJson.put("location", theActivity.getCurrentCity() + ", " + theActivity.getCurrentState());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String mDeleteUrl = "https://tcss450-group-6.herokuapp.com/removeLocation";

        new SendPostAsyncTask.Builder(mDeleteUrl, messageJson)
                .onPostExecute(this::endOfDeleteTask)
                .onCancelled(this::handleError)
                .build().execute();
    }

    private void handleError(final String msg) {
        Log.e("Error adding to weather database", msg.toString());
    }

    // Saving the location to the database and updating list/spinner
    private void endOfSaveTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);

            try {
                JSONArray locationsSaved = res.getJSONArray("locations");
                updateList(locationsSaved);
                updateSpinner(getView());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Deleting the location from the database and updating list/spinner
    private void endOfDeleteTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);

            try {
                JSONArray locationsSaved = res.getJSONArray("locations");
                updateList(locationsSaved);
                updateSpinner(getView());
                MainActivity theActivity = (MainActivity) getActivity();
                if (theActivity.getLocationSpinnerArray().size() > 0 && !theActivity.getZipMode() && !theActivity.getMapMode()) {
                    userTouched = true;
                    mSpinner.setSelection(0);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // When spinner item is selected and making it only trigger off of manual triggers or when item has been touched
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (userTouched) {
            userTouched = false;
            String choice = (String) parent.getAdapter().getItem(position);
            Log.e("SELECTEDTEST", "" + position + ", " + choice);
            List<String> split = Arrays.asList(choice.split(", "));
            String city = split.get(0);
            String state = split.get(1);
            MainActivity theActivity = (MainActivity) getActivity();
            theActivity.setCurrentCity(city);
            theActivity.setCurrentState(state);
            getView().findViewById(R.id.currentLoading).setVisibility(View.VISIBLE);

            getView().findViewById(R.id.currentTemperature).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.conditionLabel).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.humidityLabel).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.currentLocationLabel).setVisibility(View.INVISIBLE);

            updateSpinner(getView());

            try {
                updateCondition();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSpinner.setSelection(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // Updates the weather data displayed based on current city and state
    private void updateCondition() throws JSONException {
        manager = new JSONManager();
        String url = "";
        MainActivity theActivity = (MainActivity)getActivity();
        String state = theActivity.getCurrentState();
        String city = theActivity.getCurrentCity();
        if (state != null && city != null) {
            url = "http://api.wunderground.com/api/2ac2fb0eb781bb4f/conditions/hourly/forecast10day/q/" + state + "/" + city + ".json";
            manager.execute(url);
        }
    }

    private class JSONManager extends AsyncTask<String, String, String> {
        private StringBuilder builder = null;
        private HttpURLConnection connection = null;

        protected String doInBackground(String... parameters) {
            builder = new StringBuilder();
            try {
                URL url = new URL(parameters[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String currentLine = "";
                while ((currentLine = reader.readLine()) != null) {
                    builder.append(currentLine);
                }
                reader.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
            }
            return builder.toString();
        }

        @Override
        protected void onPostExecute(String resultingString) {
            super.onPostExecute(resultingString);
            try {
                MainActivity theActivity = (MainActivity)getActivity();

                String state = theActivity.getCurrentState();
                String city = theActivity.getCurrentCity();

                TextView locationLabel = getActivity().findViewById(R.id.currentLocationLabel);
                locationLabel.setText("Current Location: " + city + ", " + state);
                result = new JSONObject(resultingString);
                JSONObject currentObservation = result.getJSONObject("current_observation");
                currentTemperatureText.setText(currentObservation.getString("temp_f") + " °F  " + currentObservation.getString("temp_c") + " °C");
                TextView conditionLabel = getView().findViewById(R.id.conditionLabel);
                TextView humidityLabel = getView().findViewById(R.id.humidityLabel);
                conditionLabel.setText(currentObservation.getString("weather"));
                humidityLabel.setText("Humidity: " + currentObservation.getString("relative_humidity"));

                Integer[] idArray = {
                        R.id.hourlyTemplate1, R.id.hourlyTemplate2, R.id.hourlyTemplate3,
                        R.id.hourlyTemplate4, R.id.hourlyTemplate5, R.id.hourlyTemplate6,
                        R.id.hourlyTemplate7, R.id.hourlyTemplate8, R.id.hourlyTemplate9,
                        R.id.hourlyTemplate10, R.id.hourlyTemplate11, R.id.hourlyTemplate12,
                        R.id.hourlyTemplate13, R.id.hourlyTemplate14, R.id.hourlyTemplate15,
                        R.id.hourlyTemplate16, R.id.hourlyTemplate17, R.id.hourlyTemplate18,
                        R.id.hourlyTemplate19, R.id.hourlyTemplate20, R.id.hourlyTemplate21,
                        R.id.hourlyTemplate22, R.id.hourlyTemplate23, R.id.hourlyTemplate24
                };

                for (int i = 0; i < 24; i++) {
                    View hourlyTemplate = getView().findViewById(idArray[i]);
                    JSONArray hourlyForecast = result.getJSONArray("hourly_forecast");
                    JSONObject object = hourlyForecast.getJSONObject(i);
                    JSONObject time = object.getJSONObject("FCTTIME");
                    JSONObject temp = object.getJSONObject("temp");

                    String textId = "weatherNow" + (i + 1) + "TextView";
                    int intId = getResources().getIdentifier(textId, "id", getActivity().getPackageName());
                    TextView textView = hourlyTemplate.findViewById(intId);
                    Log.e("log", textView.toString());
                    textView.setText(time.getString("civil"));
                    textId = "weatherTemp" + (i + 1) + "TextView";
                    intId = getResources().getIdentifier(textId, "id", getActivity().getPackageName());
                    textView = hourlyTemplate.findViewById(intId);
                    textView.setText(temp.getString("english") + " °F\n" + temp.getString("metric") + " °C" );

                    try {
                        textId = "hourly" + (i + 1) + "ImageView";
                        intId = getResources().getIdentifier(textId, "id", getActivity().getPackageName());
                        ImageView imageView = hourlyTemplate.findViewById(intId);
                        new URLImage(imageView).execute(object.getString("icon_url"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Integer[] idArray2 = {
                        R.id.weather10DayContainer1, R.id.weather10DayContainer2, R.id.weather10DayContainer3,
                        R.id.weather10DayContainer4, R.id.weather10DayContainer5, R.id.weather10DayContainer6,
                        R.id.weather10DayContainer7, R.id.weather10DayContainer8, R.id.weather10DayContainer9, R.id.weather10DayContainer10
                };
                for (int i = 0; i < 10; i++) {
                    View dayTemplate = getView().findViewById(idArray2[i]);
                    JSONObject forecast = result.getJSONObject("forecast");
                    JSONObject simple = forecast.getJSONObject("simpleforecast");
                    JSONArray forecastDay = simple.getJSONArray("forecastday");
                    JSONObject day = forecastDay.getJSONObject(i);
                    JSONObject date = day.getJSONObject("date");

                    String textId = "weather10DayLabel" + (i + 1);
                    int intId = getResources().getIdentifier(textId, "id", getActivity().getPackageName());
                    TextView textView = dayTemplate.findViewById(intId);
                    textView.setText(date.getString("weekday") + ", " + date.getString("month") + "/" + date.getString("day") + "/" + date.getString("year"));

                    textId = "weather10DayTextView" + (i + 1) + "b";
                    intId = getResources().getIdentifier(textId, "id", getActivity().getPackageName());
                    textView = dayTemplate.findViewById(intId);
                    JSONObject high = day.getJSONObject("high");
                    JSONObject low = day.getJSONObject("low");
                    int fahrenheit = (high.getInt("fahrenheit") + low.getInt("fahrenheit")) / 2;
                    int celsius = (high.getInt("celsius") + low.getInt("celsius")) / 2;
                    textView.setText(fahrenheit + " °C");

                    textId = "weather10DayTextView" + (i + 1) + "a";
                    intId = getResources().getIdentifier(textId, "id", getActivity().getPackageName());
                    textView = dayTemplate.findViewById(intId);
                    textView.setText(fahrenheit + " °F");

                    try {
                        textId = "weather10DayImageView" + (i + 1);
                        intId = getResources().getIdentifier(textId, "id", getActivity().getPackageName());
                        ImageView imageView = dayTemplate.findViewById(intId);
                        new URLImage(imageView).execute(day.getString("icon_url"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    getView().findViewById(R.id.currentLoading).setVisibility(View.INVISIBLE);

                    getView().findViewById(R.id.currentTemperature).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.conditionLabel).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.humidityLabel).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.currentLocationLabel).setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class URLImage extends AsyncTask<String, String, Bitmap> {
        ImageView imageHolder;

        public URLImage(ImageView theImageHolder) {
            imageHolder = theImageHolder;
        }

        protected Bitmap doInBackground(String... theURL) {
            Bitmap icon = null;
            try {
                InputStream inputStream = new java.net.URL(theURL[0]).openStream();
                icon = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return icon;
        }

        protected void onPostExecute(Bitmap result) {
            imageHolder.setImageBitmap(result);
        }
    }
}
