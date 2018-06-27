package group6.tcss450.uw.edu.uwtchat;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import org.json.JSONException;
import org.json.JSONObject;

import group6.tcss450.uw.edu.uwtchat.model.Connection;
import group6.tcss450.uw.edu.uwtchat.utils.ChatListManager;
import group6.tcss450.uw.edu.uwtchat.utils.SendPostAsyncTask;

import static group6.tcss450.uw.edu.uwtchat.MainActivity.USER_KEY;

/**
 * This class displays the received connection requests as a bulleted list to the user and
 * offers the choice to approve or deny requests. This extends SentConnections since the
 * functionality is similar.
 * @author Gus
 * @link SentConnections
 */
public class ReceivedConnections extends SentConnections {

    /**
     * Called when creating a ReceivedConnections fragment. The current username is required in the
     * bundle.
     * @param inflater creates the layout from XML file fragment_connections_template
     * @param container view that holds this fragment.
     * @param savedInstanceState saved previous state.
     * @return A ReceivedConnections fragment.
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

        Button b = new Button(getActivity().getApplicationContext());
        b.setText(getString(R.string.user_approve));
        b.setOnClickListener(view-> onApproveClicked());
        mButtonLayout.addView(b);

        b = new Button(getActivity().getApplicationContext());
        b.setText(getString(R.string.user_decline));
        b.setOnClickListener(view->onCancelRequestClicked());
        mButtonLayout.addView(b);

        return v;
    }

    /**
     * This method is called when the approve button is clicked. It sends a request to the server
     * to approve the connection request for the selected connection.
     */
    public void onApproveClicked()
    {
        JSONObject msg = new JSONObject();
        Connection c = null;
        for(RadioButton r: mRadioButtons.keySet())
            if(r.isChecked())
                c = mRadioButtons.get(r);

        if(c==null) //if no connection is selected, return.
            return;

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
                .appendPath(getString(R.string.ep_approve_connection))
                .build();

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                //.onPostExecute(this::onSearchType)
                //.onCancelled(this::handleErrorsInTask)
                .build().execute();
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
                .appendPath(getString(R.string.ep_get_received_connections))
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
        //}
    }

    /**
     * This method removes any notifications from the status bar that pertain to new connection
     * requests since the user is viewing them now.
     * @param theJSON The result from the web server containing connection requests.
     */
    @Override
    protected void publishProgress(JSONObject theJSON)
    {
        super.publishProgress(theJSON);
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(getActivity());
        for(Connection c: mPrevConns)
            mNotificationManager.cancel(c.getUsername().hashCode());
    }
}
