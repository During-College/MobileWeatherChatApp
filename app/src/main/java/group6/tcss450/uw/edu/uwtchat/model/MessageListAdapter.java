package group6.tcss450.uw.edu.uwtchat.model;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import group6.tcss450.uw.edu.uwtchat.R;

/**
 * This class is responsible for displaying the messages in a style like that of a typical texting
 * app.
 * Code found on: https://blog.sendbird.com/android-chat-tutorial-building-a-messaging-ui
 * @author Gus
 * @author sendbird.com
 */
//https://blog.sendbird.com/android-chat-tutorial-building-a-messaging-ui
public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private List<Message> mMessageList;

    /**
     * View Holders create the Views that are displayed in a RecyclerView. The private datamembers
     * are references to the views.
     * @author Gus
     * @version 1.0
     */
    private static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView mMessageBody;
        TextView mMessageFrom;
        TextView mMessageTime;
        ReceivedViewHolder(View v) {
            super(v);
            mMessageBody = v.findViewById(R.id.messageBody);
            mMessageFrom = v.findViewById(R.id.messageFrom);
            mMessageTime = v.findViewById(R.id.messageTime);
        }

        void bind(Message current) {
            mMessageBody.setText(current.getContent());
            mMessageFrom.setText(current.getSender());
            mMessageTime.setText(current.getFormattedTime());
        }
    }

    /**
     * View Holders create the Views that are displayed in a RecyclerView. The private datamembers
     * are references to the views.
     * @author Gus
     * @version 1.0
     */
    private static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView mMessageBody;
        SentViewHolder(View v) {
            super(v);
            mMessageBody = v.findViewById(R.id.messageBody);
        }

        void bind(Message current) {
            mMessageBody.setText(current.getContent());
        }
    }

    /**
     * Creates the MessageListAdapter
     * @param theMessages The list of Message objects to be displayed.
     */
    public MessageListAdapter(List<Message> theMessages)
    {
        mMessageList = theMessages;
    }

    /**
     * Used by the layout manager to create new chat list elements.
     * @param parent the parent view that contains the elements
     * @param viewType the type of view to be displayed (sent or received message)
     * @return The new view element for the RecyclerView
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v;
        if(viewType == VIEW_TYPE_MESSAGE_SENT)
        {
            v=  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_sent_message, parent, false);
            return new SentViewHolder(v);
        } else
        {
            v=  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_received_message, parent, false);
            return new ReceivedViewHolder(v);
        }
    }

    /**
     * Replaces the contents of the view (invoked by layout manager).
     * @param holder The view holder whose content will be replaced
     * @param position The position in the RecyclerView.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Message current = mMessageList.get(position);
        switch (holder.getItemViewType()){
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedViewHolder) holder).bind(current);
                break;
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentViewHolder) holder).bind(current);
                break;
        }
    }

    /**
     * Returns which type this view holder is (sent or received).
     * @param position Position in the View Holder
     * @return The type this view holder is.
     */
    @Override
    public int getItemViewType(int position) {
        Message message =  mMessageList.get(position);

        if (message.isFromMe()) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    /**
     * Returns the number of messages displayed by the RecyclerView.
     * @return The size of the list of messages being displayed.
     */
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
