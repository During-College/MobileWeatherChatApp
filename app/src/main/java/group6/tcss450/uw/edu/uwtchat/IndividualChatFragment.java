package group6.tcss450.uw.edu.uwtchat;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import group6.tcss450.uw.edu.uwtchat.model.Message;
import group6.tcss450.uw.edu.uwtchat.model.MessageListAdapter;
import group6.tcss450.uw.edu.uwtchat.utils.ListenManager;
import group6.tcss450.uw.edu.uwtchat.utils.SendPostAsyncTask;

/**
 * This class defines the Fragment that displays the list of messages for a given chat.
 * @author Gus
 * @link Fragment
 */
public class IndividualChatFragment extends Fragment {

    private String mUsername;
    private String mChatID;
    private String mSendUrl;
    private List mMessageList;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecycler;
    private ListenManager mListenManager;

    public IndividualChatFragment() {
        // Required empty public constructor
    }

    /**
     * Called when creating a IndividualChatFragment. The current username and chatID
     * needs to be included in the bundle.
     * @param inflater creates the layout from XML file fragment_individual_chat
     * @param container view that holds this fragment.
     * @param savedInstanceState saved previous state.
     * @return A IndividualChatFragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_individual_chat, container, false);

        mMessageList = new ArrayList<Message>();
        Button b = v.findViewById(R.id.sendButton);
        b.setOnClickListener(view -> onSend());

       Bundle bundle = this.getArguments();
        if(bundle != null) {
            mUsername = bundle.getString(MainActivity.USER_KEY);
            mChatID = bundle.getString(MainActivity.CHAT_ID_KEY);
        }

        mRecycler = v.findViewById(R.id.recyclerView);
        mAdapter = new MessageListAdapter(mMessageList);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        ((LinearLayoutManager)mRecycler.getLayoutManager()).setStackFromEnd(true);
        //((LinearLayoutManager)recycler.getLayoutManager()).setReverseLayout(true);


        return v;
    }

    /**
     * This method is called when the "Send" button is clicked. It sends the text in the
     * EditText to the webserver and includes the ChatID and the current user.
     */
    public void onSend()
    {
        EditText textbox = getActivity().findViewById(R.id.textBox);
        String text = textbox.getText().toString();
        if(text == null)
            return;
        /**/
        //textbox.setText("");

        JSONObject messageJson = new JSONObject();

        try {
            messageJson.put(getString(R.string.keys_json_username), mUsername);
            messageJson.put(getString(R.string.keys_json_message), text);
            messageJson.put(getString(R.string.keys_json_chat_id), mChatID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
                .onPostExecute(this::endOfSendMsgTask)
                .onCancelled(this::handleError)
                .build().execute();
    }

    private void handleError(final String msg) {
        Log.e("CHAT ERROR!!!", msg.toString());
    }
    private void endOfSendMsgTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);
            if(res.get(getString(R.string.keys_json_success)).toString()
                    .equals(getString(R.string.keys_json_success_value_true))) {
                ((EditText) getView().findViewById(R.id.textBox))
                        .setText("");   //If it was successful, clear the textbox.
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleError(final Exception e) {
        Log.e("LISTEN ERROR!!!", e.getMessage());
    }

    /**
     * This method handles the response from the webserver and parses the messages from the JSON.
     * The messages are then displayed in the view.
     * @param messages The response from the server.
     */
    private void publishProgress(JSONObject messages) {
        final Message[] msgs;
        //((MainActivity)getActivity()).buildNotification("New Chat Message", "test");

        if(messages.has(getString(R.string.keys_json_messages))) {
            try {
                JSONArray jMessages =
                        messages.getJSONArray(getString(R.string.keys_json_messages));
                msgs = new Message[jMessages.length()];
                NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(getActivity());
                mNotificationManager.cancel(Integer.parseInt(mChatID));
                for (int i = 0; i < jMessages.length(); i++) {

                    JSONObject msg = jMessages.getJSONObject(i);
                    String username =
                            msg.get(getString(R.string.keys_json_username)).toString();
                    String userMessage =
                            msg.get(getString(R.string.keys_json_message)).toString();
                    String time =
                            msg.get(getString(R.string.keys_json_timestamp)).toString();
                    msgs[i] = new Message(username, time, userMessage, username.equals(mUsername));
                    if (i == jMessages.length() - 1) { // Get the last message and send a notification based on the message
                        //((MainActivity)getActivity()).buildNotification("New Chat Message", username + ": " + userMessage);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
           /* getActivity().runOnUiThread(() -> {
               mMessageList.clear();   //fixes duplication bug when screen locks if not using timestamp.
               // mAdapter.notifyDataSetChanged();
                for (Message msg : msgs) {
                    mMessageList.add(msg);
                    //mAdapter.notifyItemInserted(mMessageList.size()-1);  //move these to outside loop?
                    mRecycler.scrollToPosition(mMessageList.size()-1);
                    mAdapter.notifyDataSetChanged();
                }
            });*/

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                   // mMessageList.clear();
                   // mAdapter.notifyDataSetChanged();
                    for(Message msg: msgs)  //add all the new messages to the view.
                    {
                        mMessageList.add(msg);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        /*SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }
        mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");*/
        mSendUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_send_message))
                .build()
                .toString();

        Uri retrieve = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_message))
                .appendQueryParameter(getString(R.string.keys_json_chat_id), mChatID)
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
            mListenManager = new ListenManager.Builder(retrieve.toString(),
                    this::publishProgress)
                    .setExceptionHandler(this::handleError)
                    .setDelay(1000)
                    .build();
        //}
    }

    /**
     * Onresume clear the list and start listening for new messages.
     */
    @Override
    public void onResume() {
        super.onResume();
        mMessageList.clear();
        mAdapter.notifyDataSetChanged();
        mListenManager.startListening();
    }

    /**
     * Onstop save the timestamp of the last message and stop the listener.
     */
    @Override
    public void onStop() {
        super.onStop();
        String latestMessage = mListenManager.stopListening();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        // Save the most recent message timestamp
        prefs.edit().putString(
                getString(R.string.keys_prefs_time_stamp),
                latestMessage)
                .apply();
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/
}
