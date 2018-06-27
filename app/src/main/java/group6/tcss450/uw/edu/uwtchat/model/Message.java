package group6.tcss450.uw.edu.uwtchat.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class represents a Message object. A message is either sent or received, has a sender,
 * a timestamp, and content.
 * @author Gus
 */
public class Message {

    public enum MessageType {
        SENT, RECEIVED
    }

    //Format returned by web server.
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss.SSS");

    //Format for displaying to the user.
    private static final DateFormat USER_FORMATTER = new SimpleDateFormat("MM/dd/yy hh:mm:ss aa");

    private final String mSender;
    private final String mContent;
    private final Date mTime;
    private final MessageType mType;

    public Message(String theSender, String theTime, String theContent, boolean theSent)
    {
        mSender = theSender;
        mContent = theContent;
        theTime = theTime.replaceAll("\\n", " ");   //gets rid of newlines
        theTime = theTime.replaceAll(" ", "");
        theTime = theTime.substring(0, 21); //ignores microseconds

        Date d;
        try {
            d = FORMATTER.parse(theTime);
        } catch (ParseException e) {
            e.printStackTrace();
            d = null;
        }
        mTime = d;

        if(theSent)
            mType = MessageType.SENT;
        else
            mType = MessageType.RECEIVED;
    }

    public String getContent() { return mContent; }

    public String getSender() { return mSender;}

    public Date getTime() {return mTime;}

    public String getFormattedTime() {

        if(mTime == null)
            return "";
        return USER_FORMATTER.format(mTime);
    }

    public boolean isFromMe()
    {
        if(mType == MessageType.RECEIVED)
            return false;
        else
            return true;
    }
}
