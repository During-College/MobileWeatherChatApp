package group6.tcss450.uw.edu.uwtchat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import group6.tcss450.uw.edu.uwtchat.model.Credentials;


/**
 * This class defines the Fragment that displays the Login page for the app.
 * @author Gus
 * @link Fragment
 */
public class RegisterFragment extends LoginFragment {

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Called when creating a RegisterFragment. No bundle is required.
     * @param inflater creates the layout from XML file fragment_register
     * @param container view that holds this fragment.
     * @param savedInstanceState saved previous state.
     * @return A RegisterFragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register, container, false);

        Button b = (Button) v.findViewById(R.id.regB);
        b.setOnClickListener(view -> register()); //don't use view here

        return v;
    }


    /**
     * This method is called when the register button is clicked. It checks to make sure the
     * fields meet the requirements. Passwords must be at least 6 characters long, contain a letter
     * and number. A valid email address must also be provided.
     */
    public void register()
    {
        EditText username = getActivity().findViewById(R.id.regUserET);
        EditText password1 = getActivity().findViewById(R.id.regPW1);
        EditText password2 = getActivity().findViewById(R.id.regPW2);
        EditText emailbox = getActivity().findViewById(R.id.regEmail);
        EditText firstbox = getActivity().findViewById(R.id.regFirstName);
        EditText lastbox = getActivity().findViewById(R.id.regLastName);

        //trimming username, email, first and last
        String user = username.getText().toString().trim();
        String pw1 = password1.getText().toString().trim();
        String pw2 = password2.getText().toString().trim();
        String email = emailbox.getText().toString().trim();
        String first = firstbox.getText().toString().trim();
        String last = lastbox.getText().toString().trim();



        boolean success = true;

        if(email.isEmpty())
        {
            emailbox.setError("Please enter a valid email address here");
            success = false;
        }
        if (!email.contains("@")) {
            emailbox.setError("Please enter a valid email address here");
            success = false;
        }

        if(first.isEmpty())
        {
            firstbox.setError("Please enter your first name here");
            success = false;
        }

        if(last.isEmpty())
        {
            lastbox.setError("Please enter your last name here");
            success = false;
        }

        if(user.isEmpty())
        {
            username.setError("Please enter a username here");
            success = false;
        }

        if (user.contains(" ")) {
            username.setError("Username cannot have spaces");
            success = false;
        }

        if(pw1.isEmpty() || pw1.length() < 6)
        {
            password1.setError("Please enter a password that is at least 6 characters long");
            success = false;
        }

        if(pw2.isEmpty())
        {
            password2.setError("Please enter a matching password here");
            success = false;
        }

        String oneNumber = "(.)*(\\d)(.)*";
        Pattern oneLetter = Pattern.compile("[a-zA-Z]");
        Matcher m = oneLetter.matcher(pw1);
        if(!pw1.matches(oneNumber) || m.find() == false)
        {
            String s;
            if(password1.getError()==null)
                s = "";
            else
                s = password1.getError() + ". ";
            password1.setError(s + "Password must contain at least one letter" +
                    " and number");
            success = false;
        }

        /*if (success == false)
            return;*/

        if(pw1.equals(pw2) == false)
        {
            //password1.setError("Passwords must match");
            password2.setError("Passwords must match");
            success = false;
        }


        if (success == false)
            return;

        if(mListener != null)
        {
            getActivity().findViewById(R.id.registerProgress).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.regB).setEnabled(false);
            Credentials creds = new Credentials.Builder(user, password1.getText())
                    .addEmail(email)
                    .addFirstName(first)
                    .addLastName(last)
                    .build();
            mListener.onRegisterAttempt(creds);
        }
    }

    /**
     * Allows an external source to set an error message on this fragment. This may
     * be needed if an Activity includes processing that could cause login to fail.
     * @param err the error message to display.
     */
    public void setError(String err) {
        //Log in unsuccessful for reason: err. Try again.
        //you may want to add error stuffs for the user here.
        ((TextView) getView().findViewById(R.id.regEmail))
                .setError("Register Unsuccessful");    //+ err ?
    }


}
