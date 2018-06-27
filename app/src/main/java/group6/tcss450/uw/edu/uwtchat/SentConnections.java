package group6.tcss450.uw.edu.uwtchat;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import group6.tcss450.uw.edu.uwtchat.model.Connection;
import group6.tcss450.uw.edu.uwtchat.utils.ChatListManager;
import group6.tcss450.uw.edu.uwtchat.utils.SendPostAsyncTask;

import static group6.tcss450.uw.edu.uwtchat.MainActivity.USER_KEY;

/**
 * This class displays a list of pending connection requests that were sent by the current user.
 * The user has the option to cancel the request.
 * @author Gus
 * @link Fragment
 */
public class SentConnections extends Fragment{
    protected LinearLayout mButtonLayout;
    protected LinearLayout mBoxLayout;
    protected ChatListManager mListenManager;
    protected Connection[] mPrevConns;
    protected Map<RadioButton, Connection> mRadioButtons= new HashMap<>();
    protected String mUsername;

    /**
     * Called when creating a SentConnections fragment. The current username is required in the
     * bundle.
     * @param inflater creates the layout from XML file fragment_connections_template
     * @param container view that holds this fragment.
     * @param savedInstanceState saved previous state.
     * @return A SentConnections fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_connections_template, container, false);

        Bundle bundle = this.getArguments();
        if(bundle != null)
            mUsername = bundle.getString(USER_KEY);

        mButtonLayout = v.findViewById(R.id.buttonsList);
        mBoxLayout = v.findViewById(R.id.connectionsList);

        Button b = new Button(getActivity());
        b.setText(getString(R.string.user_cancel_request));
        b.setOnClickListener(view -> onCancelRequestClicked());

        mButtonLayout.addView(b);

        return v;
    }

    /**
     * This method is called when the cancel request button is clicked. It sends a JSON request to
     * the web server to cancel this request using an ASyncPostTask.
     */
    public void onCancelRequestClicked()
    {
        JSONObject msg = new JSONObject();
        Connection c = null;
        for(RadioButton r: mRadioButtons.keySet())
            if(r.isChecked())
                c = mRadioButtons.get(r);

        if(c==null) //if no connection is selected, return.
            return;
       // Log.e("Vxc", "c not null");

        try {
            msg.put(getString(R.string.keys_json_username), mUsername);
            msg.put(getString(R.string.keys_json_connection), c.getUsername());
            //Log.e("UWTCHAT", msg.toString());
        } catch (JSONException e) {
            Log.wtf("CREDENTIALS", "Error creating JSON: " + e.getMessage());
        }

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_remove_connection))
                .build();

        new SendPostAsyncTask.Builder(uri.toString(), msg).build().execute();
    }

    /**
     * This method starts a listener to update the list of connections while the fragment is visible.
     */
    @Override
    public void onStart() {
        super.onStart();

        Uri retrieve = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_sent_connections))
                .appendQueryParameter(getString(R.string.keys_json_username), mUsername)
                .build();
        /*if (prefs.contains(getString(R.string.keys_prefs_time_stamp))) {
            //ignore all of the seen messages. You may want to store these messages locally
            mListenManager = new ListenManager.Builder(retrieve.toString(),
                    this::publishProgress)
                    //.setTimeStamp(prefs.getString(getString(R.string.keys_prefs_time_stamp), "0"))
                    .setExceptionHandler(this::handleError)
                    .setDelay(1000)
                    .build();
        } else {*/
            //no record of a saved timestamp. must be a first time login
        if(this.isAdded())
            mListenManager = new ChatListManager.Builder(retrieve.toString(),
                    this::publishProgress)
                    .setExceptionHandler(this::handleError)
                    .setDelay(1000)
                    .build();
       // }
    }

    /**
     * This method is called after the response from the web server is received. It parses the
     * JSON passed to it for Connection objects. It then creates a list of connections and displays
     * them on the UI as a list of radio buttons.
     * @param theJSON The response from the web server.
     */
    protected void publishProgress(JSONObject theJSON)
    {
        try {
            final Connection[] conns;
            if (theJSON.has(getString(R.string.keys_json_connections))) {

                JSONArray jConns =
                        theJSON.getJSONArray(getString(R.string.keys_json_connections));
                conns = new Connection[jConns.length()];
                for (int i = 0; i < jConns.length(); i++) {
                    JSONObject connection = jConns.getJSONObject(i);

                    String user = connection.get(getString(R.string.keys_json_username)).toString();
                    String first = connection.get(getString(R.string.keys_json_firstname)).toString();
                    String last = connection.get(getString(R.string.keys_json_lastname)).toString();

                    conns[i] = new Connection(user, first, last);
                    // Log.d("UWTCHAT", c.getMembers().toString());
                }


                if(Arrays.equals(conns, mPrevConns))    //If the list has changes since last update
                    return;

               //Log.e("SearchConnections", "About to call getActivity");
                getActivity().runOnUiThread(() -> {
                    //Log.e("BLAGH", "In run on UI thread");
                    mRadioButtons.clear();
                    mBoxLayout.removeAllViews();
                    RadioGroup group = new RadioGroup(getActivity());
                    for (Connection c : conns) {
                        RadioButton rb = new RadioButton(getActivity());
                        rb.setText(c.toUserString());
                        group.addView(rb);
                        mRadioButtons.put(rb, c);
                    }
                    mPrevConns = conns;
                    mBoxLayout.addView(group);
                });
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * When the fragment becomes visible, this method is called to start listening for new connections.
     */
    @Override
    public void onResume() {
        super.onResume();
        mListenManager.startListening();
    }

    /**
     * This method is called when the fragment is no longer visible and stops updating the list of
     * connections.
     */
    @Override
    public void onStop() {
        super.onStop();
        mListenManager.stopListening();
        mPrevConns = null;
    }


    /**
     * This method prints any errors that occur to Logcat.
     * @param e The exception that occurred.
     */
    protected void handleError(final Exception e) {
        Log.e("LISTEN ERROR!!! (in SendConnectionsFragment)", mUsername + e.getMessage());
        for(StackTraceElement x: e.getStackTrace())
        {
            Log.e("BLAGH", x.toString());
        }
    }
}
