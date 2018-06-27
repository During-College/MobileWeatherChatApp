package group6.tcss450.uw.edu.uwtchat;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import group6.tcss450.uw.edu.uwtchat.model.Connection;
import group6.tcss450.uw.edu.uwtchat.utils.ListenManager;
import group6.tcss450.uw.edu.uwtchat.utils.SendPostAsyncTask;

import static group6.tcss450.uw.edu.uwtchat.MainActivity.USER_KEY;


/**
 * This class displays the contacts/connections for the current user as a list of checkboxes and
 * allows for users to start a chat with one or more of their contacts.
 * @author Gus
 * @link Fragment
 */
public class ViewConnectionsFragment extends Fragment {

    private String mUsername;
    private ListenManager mListenManager;
    private LinearLayout mButtonLayout;
    private LinearLayout mBoxLayout;
    private Map<CheckBox, Connection> mCheckboxes= new HashMap<>();
    private Connection[] mPrevConns;

    public ViewConnectionsFragment() {
        // Required empty public constructor
    }

    /**
     * Called when creating a ViewConnectionsFragment. The current username is required in the
     * bundle.
     * @param inflater creates the layout from XML file fragment_connections_template
     * @param container view that holds this fragment.
     * @param savedInstanceState saved previous state.
     * @return A ViewConnectionsFragment.
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
        b.setText(getString(R.string.user_start_chat));
        b.setOnClickListener(view->onStartChatClicked());

        mButtonLayout.addView(b);

        return v;
    }

    /**
     * This method is called when the start chat button is clicked. It sends a JSON request to the
     * web server to start a new chat with the selected contacts.
     */
    public void onStartChatClicked()
    {
        JSONObject msg = new JSONObject();
        try {
            msg.put(getString(R.string.keys_json_username), mUsername);
            JSONArray members = new JSONArray();
            for(CheckBox c: mCheckboxes.keySet())
            {
                if(c.isChecked())
                    members.put(mCheckboxes.get(c).getUsername());
            }
            if(members.length()<1)
                return;

            members.put(mUsername);  //instead of sending username seperate or also??{}
            msg.put(getString(R.string.keys_json_members), members);
            //Log.e("UWTCHAT", msg.toString());
        } catch (JSONException e) {
            Log.wtf("CREDENTIALS", "Error creating JSON: " + e.getMessage());
        }

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_start_chat))
                .build();

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleNewChatStarted)
                .build().execute();
    }

    /**
     * This method is called when the new chat ASyncTask finished. It loads an IndividualChatFragment
     * for the user to start chatting if the task was successful.
     * @param result the response from the web server.
     */
    private void handleNewChatStarted(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean(getString(R.string.keys_json_success));
            String chatID = resultsJSON.getString(getString(R.string.keys_json_chatid));
            if (success) {
                //load individual chat fragment but don't put on back stack
                //Log.e("UWTCHAT", "Success");
                IndividualChatFragment frag = new IndividualChatFragment();
                Bundle bundle = new Bundle();
                bundle.putString(USER_KEY, mUsername);
                bundle.putString(MainActivity.CHAT_ID_KEY, chatID);
                frag.setArguments(bundle);
                //replace this fragment with new chat fragment
                FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainFragmentContainer, frag, getString(R.string.keys_fragment_new_chat)) // Added tag parameter to attach to the fragment
                        .addToBackStack(null);
                transaction.commit();
            } else {
                //TODO set an error somehow
                Log.e("UWTCHAT", "Faulire from JHSon");
                Log.e("UWTCHAT", result);
            }
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
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
                .appendPath(getString(R.string.ep_get_connections))
                .appendQueryParameter(getString(R.string.keys_json_username), mUsername)
                .build();
       /* if (prefs.contains(getString(R.string.keys_prefs_time_stamp))) {
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
            mListenManager = new ListenManager.Builder(retrieve.toString(),
                    this::publishProgress)
                    .setExceptionHandler(this::handleError)
                    .setDelay(1000)
                    .build();
        //}
    }

    /**
     * This method is called once the web server has responded. It parses the JSON for a list of
     * connections, then displays those connections as a list of checkboxes to the user.
     * @param theResult the response from the web server.
     */
    //This will be the same for all connection fragments
    private void publishProgress(JSONObject theResult)
    {
        final Connection[] conns;
        if (theResult.has(getString(R.string.keys_json_connections))) {
            try {
                JSONArray jConns =
                        theResult.getJSONArray(getString(R.string.keys_json_connections));
                conns = new Connection[jConns.length()];
                for (int i = 0; i < jConns.length(); i++) {
                    JSONObject connection = jConns.getJSONObject(i);

                    String user = connection.get(getString(R.string.keys_json_username)).toString();
                    String first = connection.get(getString(R.string.keys_json_firstname)).toString();
                    String last = connection.get(getString(R.string.keys_json_lastname)).toString();

                    conns[i] = new Connection(user, first, last);
                    // Log.d("UWTCHAT", c.getMembers().toString());
                }


                if(Arrays.equals(conns, mPrevConns))    //if no new connections don't update.
                    return;

                getActivity().runOnUiThread(() -> {
                    mCheckboxes.clear();
                    mBoxLayout.removeAllViews();
                    for (Connection c : conns) {
                        CheckBox cb = new CheckBox(getActivity());
                        cb.setText(c.toUserString());
                        mCheckboxes.put(cb, c);
                        mBoxLayout.addView(cb);
                        mPrevConns = conns;
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method prints any errors that occur to Logcat.
     * @param e The exception that occurred.
     */
    private void handleError(final Exception e) {
        Log.e("LISTEN ERROR!!! (in ViewConnecttionsFragment)", e.getMessage());
    }

    /**
     * When the fragment becomes visible, this method is called to start listening for new connections.
     */
    @Override
    public void onResume() {
        super.onResume();
        //mChats.clear();
        //mAdapter.notifyDataSetChanged();
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
}
