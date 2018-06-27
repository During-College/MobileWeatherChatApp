package group6.tcss450.uw.edu.uwtchat;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import group6.tcss450.uw.edu.uwtchat.utils.SendPostAsyncTask;

import static group6.tcss450.uw.edu.uwtchat.MainActivity.USER_KEY;


/**
 * This class displays an EditText that the user can type the first name, last name, or user name
 * of a user to search for new contacts. The results are displayed as a bulleted list that the
 * user can select and send a connection request to.
 * @author Gus
 * @link Fragment
 */
public class SearchConnections extends Fragment {

    private String mUsername;
    private LinearLayout mButtonLayout;
    private LinearLayout mBoxLayout;
    private Map<RadioButton, Connection> mRadioButtons= new HashMap<>();
    private Connection[] mPrevConns;

    public SearchConnections() {
        // Required empty public constructor
    }

    /**
     * Called when creating a SearchConnections fragment. The current username is required in the
     * bundle.
     * @param inflater creates the layout from XML file fragment_search_connections
     * @param container view that holds this fragment.
     * @param savedInstanceState saved previous state.
     * @return A SearchConnections fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search_connections, container, false);

        mButtonLayout = v.findViewById(R.id.buttonsList);
        mBoxLayout = v.findViewById(R.id.connectionsList);

        Bundle bundle = this.getArguments();
        if(bundle != null)
            mUsername = bundle.getString(USER_KEY);

        Button b = new Button(getActivity().getApplicationContext());
        b.setText(getString(R.string.user_add_contact));
        b.setOnClickListener(view -> onAddContactClicked());

        EditText editText = v.findViewById(R.id.searchBox);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    onSearchType(editText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mButtonLayout.addView(b);

        return v;
    }

    /**
     * This method is called when the add contact button is clicked. It sends a connection request
     * to the web server with the selected user and the current user as a SendPostAsyncTask.
     */
    public void onAddContactClicked()
    {
        JSONObject msg = new JSONObject();
        Connection c = null;
        for(RadioButton r: mRadioButtons.keySet())
            if(r.isChecked())
                c = mRadioButtons.get(r);

        if(c==null) //if nothing was selected
            return;

        try {
            msg.put(getString(R.string.keys_json_username), mUsername);
            msg.put(getString(R.string.keys_json_connection), c.getUsername());
            Log.e("UWTCHAT", msg.toString());
        } catch (JSONException e) {
            Log.wtf("CREDENTIALS", "Error creating JSON: " + e.getMessage());
        }

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_add_connection))
                .build();

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::onSearchType)
                .build().execute();
    }

    /**
     * This method is called every time the user types in the search box. Each time this method is
     * called, a SendPostAsyncTask is sent to the web server searching for users with matching
     * criteria.
     * @param theText the criteria to be sent to the webserver to look up a matching name.
     */
    private void onSearchType(String theText)
    {
        String url = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_search_connections))
                    .toString();

        JSONObject msg = new JSONObject();
        try {
            msg.put(getString(R.string.keys_json_username), mUsername);
            msg.put(getString(R.string.keys_json_search), theText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(url, msg)
                                    .onPostExecute(this::endOfRetrieve).build().execute();
        //Log.e("SearchConnections", mUsername+"End of onSearchType"+msg.toString());
    }

    /**
     * This method is called once the web server has responded. It parses the JSON for a list of
     * connections, then displays those connections as a bulleted list to the user.
     * @param theResult the response from the web server.
     */
    private void endOfRetrieve(final String theResult)
    {
       //Log.e("SearchConnections", "beginning of endOfRetrieve" + theResult);
        try {
        JSONObject res = new JSONObject(theResult);
        final Connection[] conns;
        if (res.has(getString(R.string.keys_json_connections))) {

                JSONArray jConns =
                        res.getJSONArray(getString(R.string.keys_json_connections));
                conns = new Connection[jConns.length()];
                for (int i = 0; i < jConns.length(); i++) {
                    JSONObject connection = jConns.getJSONObject(i);

                    String user = connection.get(getString(R.string.keys_json_username)).toString();
                    String first = connection.get(getString(R.string.keys_json_firstname)).toString();
                    String last = connection.get(getString(R.string.keys_json_lastname)).toString();

                    conns[i] = new Connection(user, first, last);
                    // Log.d("UWTCHAT", c.getMembers().toString());
                }


                if(Arrays.equals(conns, mPrevConns))
                    return;

                //Log.e("SearchConnections", "About to call getActivity");
                getActivity().runOnUiThread(() -> {
                    mRadioButtons.clear();
                    mBoxLayout.removeAllViews();
                    RadioGroup group = new RadioGroup(getActivity().getApplicationContext());
                    for (Connection c : conns) {
                        RadioButton rb = new RadioButton(getActivity().getApplicationContext());
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

}
