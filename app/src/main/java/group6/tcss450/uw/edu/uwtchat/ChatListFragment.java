package group6.tcss450.uw.edu.uwtchat;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import group6.tcss450.uw.edu.uwtchat.model.Chat;
import group6.tcss450.uw.edu.uwtchat.model.ChatListAdapter;
import group6.tcss450.uw.edu.uwtchat.utils.ChatListManager;

import static group6.tcss450.uw.edu.uwtchat.MainActivity.USER_KEY;

/**
 * This class defines the Fragment that displays the list of chats.
 * @author Gus
 * @link Fragment
 */
public class ChatListFragment extends Fragment {

    private String mCurrentUser;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecycler;
    private ChatListAdapter.ChatListListener mListener;
    private List<Chat> mChats;
    private ChatListManager mListenManager;

    public ChatListFragment() {
        // Required empty public constructor
    }

    /**
     * Called when creating a ChatListFragment. The username needs to be included in the bundle.
     * @param inflater creates the layout from XML file fragment_chat_list
     * @param container view that holds this fragment.
     * @param savedInstanceState saved previous state.
     * @return A ChatListFragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_list, container, false);

        mChats = new ArrayList<>();

        Button b = v.findViewById(R.id.newButton);
        b.setOnClickListener(view -> mListener.onNewChatClick());

       Bundle bundle = this.getArguments();
        if(bundle != null)
            mCurrentUser = bundle.getString(USER_KEY);


        String url =  new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_chats))
                .build()
                .toString();

        url += "?"+ getString(R.string.keys_json_username)+"=" + mCurrentUser;

        mListenManager = new ChatListManager.Builder(url,
                this::publishProgress)
                .setExceptionHandler(this::handleError)
                .setDelay(2500)
                .build();

        mRecycler = v.findViewById(R.id.recyclerView);
        mAdapter = new ChatListAdapter(getActivity(), mChats, mListener);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        return v;
    }


    private void handleError(final Exception e) {
        Log.e("LISTEN ERROR!!!", e.getMessage());
    }

    /**
     * Called when web server responds.
     * @param theChats The result from the web server.
     */
    private void publishProgress(JSONObject theChats) {
        final Chat[] chats;
        if (theChats.has(getString(R.string.keys_json_chats))) {
            try {
                JSONArray jChats =
                        theChats.getJSONArray(getString(R.string.keys_json_chats));
                chats = new Chat[jChats.length()];
                for (int i = 0; i < jChats.length(); i++) {
                    JSONObject chat = jChats.getJSONObject(i);
                    JSONArray members =
                            chat.getJSONArray(getString(R.string.keys_json_members));
                    String id = chat.get(getString(R.string.keys_json_chatid)).toString();
                    Chat c = new Chat(id);
                    for (int j = 0; j < members.length(); j++) {
                        JSONObject current = members.getJSONObject(j);
                        String username = current.get(getString(R.string.keys_json_username)).toString();
                        if(username.equals(mCurrentUser))   //dont include current user in names.
                            continue;
                        else
                            c.addMember(username);
                    }
                    chats[i] = c;
                   // Log.d("UWTCHAT", c.getMembers().toString());
                }

                getActivity().runOnUiThread(() -> {
                    mChats.clear();
                    mAdapter.notifyDataSetChanged();
                    for (Chat c : chats) {
                        mChats.add(c);
                        mAdapter.notifyItemInserted(mChats.size()-1);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Called when attaching Fragment to container. Context mist implement
     * ChatListAdapter.ChatListListener for New Chat button.
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
     * OnResume start the listener to check for new chats and chat members.
     */
    @Override
    public void onResume() {
        super.onResume();
        //mChats.clear();
        //mAdapter.notifyDataSetChanged();
        mListenManager.startListening();
    }

    /**
     * OnStop stop listening for new chats and chat members.
     */
    @Override
    public void onStop() {
        super.onStop();
        mListenManager.stopListening();
    }

}
