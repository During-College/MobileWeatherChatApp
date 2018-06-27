package group6.tcss450.uw.edu.uwtchat;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import group6.tcss450.uw.edu.uwtchat.utils.ChatListManager;

import static group6.tcss450.uw.edu.uwtchat.MainActivity.USER_KEY;

/**
 * This class displays the contacts/connections for the current user as a bulleted list and allows
 * for users to delete a connection/contact. This extends SentConnections since the
 * functionality is similar.
 * @author Gus
 * @link SentConnections
 */
public class RemoveConnectionsFragment extends SentConnections {

    /**
     * Called when creating a RemoveConnectionsFragment. The current username is required in the
     * bundle.
     * @param inflater creates the layout from XML file fragment_connections_template
     * @param container view that holds this fragment.
     * @param savedInstanceState saved previous state.
     * @return A RemoveConnectionsFragment.
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
        b.setText(getString(R.string.user_delete_connection));
        b.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setCancelable(true);
            builder.setTitle(getString(R.string.user_delete_contact_caps));
            builder.setMessage(getString(R.string.user_delete_contact_msg));
            builder.setPositiveButton(getString(R.string.user_confirm),
                    (dialog, which) -> onCancelRequestClicked());
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        });
        mButtonLayout.addView(b);

        return v;
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
        /*if (prefs.contains(getString(R.string.keys_prefs_time_stamp))) {
            //ignore all of the seen messages. You may want to store these messages locally
            mListenManager = new ListenManager.Builder(retrieve.toString(),
                    super::publishProgress)
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
}
