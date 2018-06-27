package group6.tcss450.uw.edu.uwtchat.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents/models a Chat. A chat has a chatID and members. Messages are not used here.
 * @author Gus Penley
 * @version 1.0
 */
public class Chat {
    private final String mChatID;
    private final List<String> mMembers;

    public Chat(String theID, List<String> theMembers)
    {
        mChatID = theID;
        mMembers = theMembers;
    }

    public Chat(String theID)
    {
        mChatID = theID;
        mMembers = new ArrayList<>();
    }

    public void addMember(String theMember)
    {
        mMembers.add(theMember);
    }

    public List<String> getMembers() {
        return mMembers;
    }

    public String getChatID() {
        return mChatID;
    }

    @Override
    public boolean equals(Object theO) {
        if (theO == null)
            return false;

        if ((theO instanceof Chat) == false)
            return false;

        Chat c = (Chat) theO;
        if(c.mChatID.equals(this.mChatID))
            return true;
        else
            return false;
    }
}
