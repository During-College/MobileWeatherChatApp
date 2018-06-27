package group6.tcss450.uw.edu.uwtchat;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import group6.tcss450.uw.edu.uwtchat.utils.SendPostAsyncTask;

/**
 * This class defines the Fragment that displays the Login page for the app.
 * @author Anh
 * @link Fragment
 */
public class PasswordFragment extends Fragment  {

    private EditText mEditText;


    public PasswordFragment() {
        // Required empty public constructor
    }


    /**
     * Called when creating a PasswordFragment. No bundle is required.
     * @param inflater creates the layout from XML file fragment_password
     * @param container view that holds this fragment.
     * @param savedInstanceState saved previous state.
     * @return A PasswordFragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_password, container, false);
        // Inflate the layout for this fragment

        Button b = (Button) v.findViewById(R.id.sendPasswordB);
        mEditText = v.findViewById(R.id.editText);
        b.setOnClickListener(view ->sendReset()); //don't use view here
        return v;
    }

    /**
     * This method is called when the Reset button is clicked. It sends the email to the web
     * server using a SendPostAsyncTask.
     */
    private  void sendReset() {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_reset_password))
                .build();
        //build the JSONObject
        JSONObject msg = new JSONObject();
        try {
            msg.put(getString(R.string.keys_json_email), mEditText.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleResetOnPost)
                .build().execute();
    }

    /**
     * This method is called when the web server responds from the reset request. A toast is
     * displayed if the reset was successful, or the email wasn't found.
     * @param theResult The response from the web server.
     */
    private void handleResetOnPost(String theResult)
    {
        try {
            JSONObject resultsJSON = new JSONObject(theResult);
            boolean success = resultsJSON.getBoolean("success");
            if(success)
                Toast.makeText(getActivity(), "Reset link sent", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity(), "Email not found!", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
