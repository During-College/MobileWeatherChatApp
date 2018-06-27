package group6.tcss450.uw.edu.uwtchat.model;

/**
 * This class represents a Notification object. A notification has a sender, a message a chatID,
 * and a timestamp.
 * @author Gus
 */
public class Notification {
    private String mSender;
    private String mMessage;
    private String mChatID;
    private String mTimestamp;

    public Notification(String theSender, String theMessage, String theChatID, String
                        theStamp)
    {
        mSender = theSender;
        mMessage = theMessage;
        mChatID = theChatID;
        mTimestamp = theStamp;
    }

    public String getMessage() { return mMessage;}

    public String getSender()   {   return mSender;}

    public String getChatID()   {return mChatID;}

    public boolean isConnection() {return mChatID.equals("connection");}

    @Override
    public boolean equals(Object theO)
    {
        if(theO == null)
            return false;
        if(theO instanceof Notification == false)
            return false;
        Notification x = (Notification) theO;
        if(x.mSender.equals(mSender))
            if(x.mTimestamp.equals(mTimestamp))
                if(x.mMessage.equals(mMessage))
                    return true;
        return false;
    }
}
