package group6.tcss450.uw.edu.uwtchat;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.util.List;

import group6.tcss450.uw.edu.uwtchat.model.Chat;
import group6.tcss450.uw.edu.uwtchat.model.ChatListAdapter;

import static group6.tcss450.uw.edu.uwtchat.MainActivity.USER_KEY;


/**
 * This class defines the Fragment that displays the home/landing page for the app.
 * @author Gus
 * @author Matthew
 * @link Fragment
 */
public class HomeFragment extends Fragment {

    private JSONObject result;
    private JSONManager manager;
    private String mCurrentUser;
    private List<String> mRecentChats;
    private ChatListAdapter.ChatListListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Called when creating a HomeFragment. The username and a list of strings with recent
     * chatIDs needs to be included in the bundle.
     * @param inflater creates the layout from XML file fragment_home
     * @param container view that holds this fragment.
     * @param savedInstanceState saved previous state.
     * @return A HomeFragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        manager = new JSONManager();
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            mCurrentUser = bundle.getString(USER_KEY);
            mRecentChats =  bundle.getStringArrayList(MainActivity.RECENT_CHATS_KEY);
        }

        MainActivity theActivity = (MainActivity)getActivity();

        String state = theActivity.getCurrentState();
        String city = theActivity.getCurrentCity();
        if (state != null && city != null) {
            manager.execute("http://api.wunderground.com/api/2ac2fb0eb781bb4f/conditions/q/" + state + "/" + city + ".json");
        }

        String url = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_chats))
                .build().toString();

        url += "?"+ getString(R.string.keys_json_username)+"=" + mCurrentUser;
        new ChatManager().execute(url);


        Button weatherShortcut = v.findViewById(R.id.weatherShortcut);
        weatherShortcut.setOnClickListener(v1 -> {
            theActivity.loadFragment(new WeatherFragment(), getResources().getString(R.string.keys_fragment_weather));
            //theActivity.updateNavigationSelected();
            //theActivity.getNavigationView().setCheckedItem(R.id.nav_weather);
        });

        return v;
    }

    /**
     * Called when attaching Fragment to container. Context mist implement
     * ChatListAdapter.ChatListListener for opening notifications and recent chats.
     * @param context The context this fragment is being attached to.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ChatListAdapter.ChatListListener) {
            mListener = (ChatListAdapter.ChatListListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ChatListAdapter.ChatListListener");
        }
    }

    /**
     * This class manages the recent chats section of the HomeFragment. It retrieves the usernames
     * of the recent chat members and displays them in a list of the recent chats.
     * @author Gus
     */
    private class ChatManager extends JSONManager
    {
        @Override
        protected void onPostExecute(String resultingString) {
            super.onPostExecute(resultingString);
            Activity theActivity = getActivity();
            if (isAdded() && theActivity != null) {
                try {
                    JSONObject theChats = new JSONObject(resultingString);
                    final ArrayList<Chat> chats;

                    if (theChats.has(getString(R.string.keys_json_chats))) {
                        JSONArray jChats = theChats.getJSONArray(getString(R.string.keys_json_chats));
                        chats = new ArrayList<>();
                        for (int i = 0; i < jChats.length(); i++) {
                            JSONObject chat = jChats.getJSONObject(i);
                            JSONArray members = chat.getJSONArray(getString(R.string.keys_json_members));
                            String id = chat.get(getString(R.string.keys_json_chatid)).toString();
                            Chat c = new Chat(id);
                            for (int j = 0; j < members.length(); j++) {
                                JSONObject current = members.getJSONObject(j);
                                String username = current.get(getString(R.string.keys_json_username)).toString();
                                if (username.equals(mCurrentUser)) continue;
                                else c.addMember(username);
                            }
                            chats.add(c);
                            //Log.e("UWTCHAT", c.getMembers().toString());
                        }

                        getActivity().runOnUiThread(() -> {
                            LinearLayout layout = getActivity().findViewById(R.id.recentChats);
                            for (int i = mRecentChats.size() - 1; i >= 0; i--) {
                                int location = chats.indexOf(new Chat(mRecentChats.get(i), null));
                                if (location > -1) {
                                    Chat c = chats.get(location);
                                    List<String> members = c.getMembers();
                                    String memNames;
                                    if (members.size() < 1) continue;
                                    else {
                                        memNames = members.get(0);
                                        for (int j = 1; j < members.size(); j++) {
                                            memNames = memNames + ", " + members.get(j);
                                        }
                                    }
                                    TextView tv = new TextView(getActivity().getApplicationContext());
                                    tv.setBackgroundResource(R.color.lightGray);
                                    tv.setText(memNames);
                                    tv.setPadding(8, 8, 8, 8);
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    params.setMargins(0, 15, 0, 0);
                                    tv.setLayoutParams(params);
                                    tv.setOnClickListener((view) -> {
                                        mListener.onChatSelect(c.getChatID());
                                    });
                                    layout.addView(tv);
                                }
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }


    }

    // Class that handles the given URL
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

        // Called when done retrieving data from the weather url
        @Override
        protected void onPostExecute(String resultingString) {
            super.onPostExecute(resultingString);

            try {
                MainActivity theActivity = (MainActivity)getActivity();
                if (theActivity != null) {
                    String state = theActivity.getCurrentState();
                    String city = theActivity.getCurrentCity();

                    result = new JSONObject(resultingString);
                    JSONObject currentObservation = result.getJSONObject("current_observation");
                    TextView locationLabel = getView().findViewById(R.id.homeWeatherLocation);
                    locationLabel.setText(city + ", " + state);
                    TextView temperatureLabel = getView().findViewById(R.id.homeWeatherTemperature);
                    temperatureLabel.setText(currentObservation.getString("temp_f") + " °F    " + currentObservation.getString("temp_c") + " °C");
                    TextView conditionLabel = getView().findViewById(R.id.homeWeatherCondition);
                    conditionLabel.setText(currentObservation.getString("weather"));


                    try {
                        ImageView imageView = getView().findViewById(R.id.homeWeatherImg);
                        new URLImage(imageView).execute(currentObservation.getString("icon_url"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    getActivity().findViewById(R.id.homeWeatherProgress).setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    // Private async class that is used to load the url image for weather usage
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
