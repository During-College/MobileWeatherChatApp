package group6.tcss450.uw.edu.uwtchat.model;

/**
 * This class models a Connection (Contact). A contact consists of a first name, last name,
 * and username as strings.
 * @author Gus
 * @version 1.0
 */
public class Connection {
    private final String mUsername;
    private final String mFirstName;
    private final String mLastName;

    public Connection(String theUsername, String theFirstName, String theLastName)
    {
        mUsername = theUsername;
        mFirstName = theFirstName;
        mLastName = theLastName;
    }

    public String toUserString()
    {
        return mUsername + " - " + mFirstName + " " + mLastName;
    }

    @Override
    public boolean equals(Object theO)
    {
        if(theO == null)
            return false;

        if(!(theO instanceof Connection))
            return false;

        Connection other = (Connection) theO;
        if(other.mUsername.equals(this.mUsername))
            if(other.mFirstName.equals(this.mFirstName))
                if(other.mLastName.equals(this.mLastName))
                    return true;

        return false;
    }

    public String getUsername() {
        return mUsername;
    }
}
