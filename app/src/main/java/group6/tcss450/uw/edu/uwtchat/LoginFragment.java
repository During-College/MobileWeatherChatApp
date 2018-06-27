package group6.tcss450.uw.edu.uwtchat;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import group6.tcss450.uw.edu.uwtchat.model.Credentials;


/**
 * This class defines the Fragment that displays the Login page for the app.
 * @author Gus
 * @link Fragment
 */
public class LoginFragment extends Fragment {

    protected FragmentInteractionListener mListener;


    public LoginFragment() {
        // Required empty public constructor
    }


    /**
     * Called when creating a LoginFragment. No bundle is required.
     * @param inflater creates the layout from XML file fragment_login
     * @param container view that holds this fragment.
     * @param savedInstanceState saved previous state.
     * @return A LoginFragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);


        Button b = (Button) v.findViewById(R.id.loginB);
        b.setOnClickListener(view -> login()); //don't use view here

        b = (Button) v.findViewById(R.id.registerB);
        b.setOnClickListener(view -> mListener.onRegisterButtonClick());

        b = (Button) v.findViewById(R.id.forgotB);
        b.setOnClickListener(view->mListener.onForgotButtonClick());

        return v;
    }

    /**
     * Called when attaching Fragment to container. Context mist implement
     * FragmentInteractionListener for login,register,and forgot password buttons.
     * @param context The context this fragment is being attached to.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            mListener = (FragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * This method is called when the login button is clicked. It checks to make sure the
     * fields meet the requirements.
     */
    public void login() {
        EditText username = getActivity().findViewById(R.id.nameET);
        EditText password = getActivity().findViewById(R.id.pwET);

        //trimming username
        String user = username.getText().toString().trim();
        String pw = password.getText().toString().trim();


        boolean success = true;

        if (user.isEmpty()) {
            username.setError("Please enter your username here");
            success = false;
        }

        if (pw.isEmpty()) {
            password.setError("Please enter your password here");
            success = false;
        }

        if (success == false)
            return;

        if(mListener != null) {
            Credentials creds = new Credentials.Builder(user, password.getText()).build();
            mListener.onLoginAttempt(creds);
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
        ((TextView) getView().findViewById(R.id.nameET))
                .setError("Login Unsuccessful");    //+ err ?
    }

    public interface FragmentInteractionListener
    {
        /** Used to process the login request */
        public void onLoginAttempt(Credentials theCreds);
        /** Used to process the register request */
        public void onRegisterAttempt(Credentials theCreds);
        /** Used to open the Register Fragment */
        public void onRegisterButtonClick();
        /** Used to open the Forgot Password Fragment */
        public void onForgotButtonClick();
    }
}
