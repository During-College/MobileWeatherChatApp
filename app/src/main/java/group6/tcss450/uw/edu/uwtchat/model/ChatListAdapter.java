package group6.tcss450.uw.edu.uwtchat.model;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import group6.tcss450.uw.edu.uwtchat.R;
import group6.tcss450.uw.edu.uwtchat.utils.SendPostAsyncTask;

/**
 * Provides a binding from Chat objects to be displayed in a RecyclerView. Used in the Chat List
 * Fragment.
 * @author Gus
 * @version 1.0
 */
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private Context mContext;
    private List<Chat> mChats;

    private ChatListListener mListener;

    /**
     * View Holders create the Views that are displayed in a RecyclerView.
     * @author Gus
     * @version 1.0
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;
        private String mChatID;
        private ChatListListener mListener; //for making callbacks when a chat is selected.
        private Button mDeleteButton;
        ViewHolder(View v, ChatListListener theListener) {
            super(v);
            mListener = theListener;
            this.setIsRecyclable(false);    //fixes weird bug where delete button scrolls up
            mTextView = v.findViewById(R.id.chatName);
            mTextView.setOnClickListener(view -> mListener.onChatSelect(mChatID));
            mDeleteButton = v.findViewById(R.id.deleteButton);
            mDeleteButton.setOnClickListener(view -> {  //Confirm delete chat
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setCancelable(true);
                builder.setTitle(mContext.getString(R.string.user_delete_chat_caps));
                builder.setMessage(mContext.getString(R.string.user_delete_confirmation));
                builder.setPositiveButton(mContext.getString(R.string.user_confirm),
                        (dialog, which) -> onDeleteButtonClicked());
                builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            });
        }

        /**
         * Sends a JSON message to the server to delete this chat.
         */
        private void onDeleteButtonClicked() {
            JSONObject msg = new JSONObject();
            try {
                msg.put(mContext.getString(R.string.keys_json_username),
                        mListener.getUsername());
                msg.put(mContext.getString(R.string.keys_json_chatid), mChatID);
            } catch (JSONException e) {
                Log.wtf("CREDENTIALS", "Error creating JSON: " + e.getMessage());
            }

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(mContext.getString(R.string.ep_base_url))
                    .appendPath(mContext.getString(R.string.ep_delete_chat))
                    .build();

            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPostExecute(this::handleChatDeleted)
                    .onPreExecute(()->mDeleteButton.setEnabled(false))
                    .build().execute();
        }

        /**
         * This method gets called when the web server responds from the delete request.
         * @param result The response from the web server.
         */
        private void handleChatDeleted(String result) {
            try {
                JSONObject resultsJSON = new JSONObject(result);
                boolean success = resultsJSON.getBoolean("success");
                if (success) {
                    mTextView.setOnClickListener(null);
                } else {
                    //TODO set an error somehow
                    Log.e("UWTCHAT", "Faulire from JHSon");
                    Log.e("UWTCHAT",result);
                    mDeleteButton.setEnabled(true);
                }
            } catch (JSONException e) {
                //It appears that the web service didn’t return a JSON formatted String
                //or it didn’t have what we expected in it.
                Log.e("JSON_PARSE_ERROR", result
                        + System.lineSeparator()
                        + e.getMessage());
            }
        }

        void setChatID(String mChatID) {
            this.mChatID = mChatID;
        }
    }

    /**
     * Callback interface for when user interacts with Chat List.
     */
    public interface ChatListListener {
        /** When user selects chat, ChatID is passed */
        void onChatSelect(String theChatID);
        /** The ChatListAdapter uses this method to get the username. */
        String getUsername();
        /** Used when the New Chat Button is clicked. */
        void onNewChatClick();
    }


    /**
     * Creates a ChatListAdapter.
     * @param theContext Used to display UI elements.
     * @param theChats The list of Chat objects to be displayed by the adapter.
     * @param theListener The object to handle callbacks from the ChatList.
     */
    public ChatListAdapter(Context theContext, List<Chat> theChats, ChatListListener theListener)
    {
        mListener = theListener;
        mContext = theContext;
        mChats = theChats;
    }


    /**
     * Used by the layout manager to create new chat list elements.
     * @param parent the parent view that contains the elements
     * @param viewType the type of view to be displayed
     * @return The new view element for the RecyclerView
     */
    @NonNull
    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item, parent, false);
        //add delete button action listener here? or prob in constructor for view holder
        return new ViewHolder(v, mListener);
    }

    /**
     * Replaces the contents of the view (invoked by layout manager).
     * @param holder The view holder whose content will be replaced
     * @param position The position in the RecyclerView.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        List<String> members = mChats.get(position).getMembers();
        String memNames;
        if(members.size() < 1)
            memNames = "Conversation Ended";
        else
        {
            memNames = members.get(0);
            for(int i=1; i <members.size(); i++)
            {
                memNames = memNames + ", " + members.get(i);
            }
        }

        holder.mTextView.setText(memNames);
        holder.setChatID(mChats.get(position).getChatID());
    }


    /**
     * Returns the size of the list of Chats.
     * @return The size of the list of chats being displayed.
     */
    @Override
    public int getItemCount() {
        if(mChats== null)
            return 0;
        return mChats.size();
    }
}
