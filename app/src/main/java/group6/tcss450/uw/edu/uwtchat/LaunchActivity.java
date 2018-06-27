package group6.tcss450.uw.edu.uwtchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import group6.tcss450.uw.edu.uwtchat.model.Credentials;
import group6.tcss450.uw.edu.uwtchat.utils.SendPostAsyncTask;

/**
 * This class is the entry point to the app. It determines if a user has already been logged in.
 * If so, it launches the Main Activity, otherwise, it launches the login activity.
 * @author Gus
 * @author Anh
 */
public class LaunchActivity extends AppCompatActivity implements LoginFragment.FragmentInteractionListener
{
    private Credentials mCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);


        if(savedInstanceState == null) {

            //see if already logged in and launch the appropriate activity.
            if (findViewById(R.id.launchFragmentContainer) != null) {
                SharedPreferences prefs =
                        getSharedPreferences(
                                getString(R.string.keys_shared_prefs),
                                Context.MODE_PRIVATE);
                if (prefs.getBoolean(getString(R.string.keys_prefs_stay_logged_in),
                        false)) {
                    String s =prefs.getString(getString(R.string.keys_prefs_username), "DEFAULT");
                    loadMainActivity(s);
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.launchFragmentContainer,
                                    new LoginFragment(),
                                    getString(R.string.keys_fragment_login))
                            .commit();
                }
            }
        }
    }

    /**
     * This method is called when the Login button is pressed from the LoginFragment. It sends
     * the credentials to the server and attempts to login.
     * @param theCreds The username and password for the user trying to login.
     */
    @Override
    public void onLoginAttempt(Credentials theCreds) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_login))
                .build();
        //build the JSONObject
        JSONObject msg = theCreds.asJSONObject();
        mCredentials = theCreds;
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        findViewById(R.id.loginProgress).setVisibility(View.VISIBLE);
        findViewById(R.id.loginB).setEnabled(false);
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleLoginOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();

    }

    /**
     * This method is called when the Register button is clicked from the Register fragment. It
     * sends the credentials to the web server and tries to create a new account if the parameters
     * meet the requirements.
     * @param theCreds The credentials of the new account.
     */
    @Override
    public void onRegisterAttempt(Credentials theCreds) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_register))
                .build();
        //build the JSONObject
        JSONObject msg = theCreds.asJSONObject();
        mCredentials = theCreds;
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleRegisterOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();

    }

    /**
     * This method loads the Register Fragment if the register button is clicked from the login
     * fragment.
     */
    @Override
    public void onRegisterButtonClick() {
        RegisterFragment registerFragment;

        registerFragment = new RegisterFragment();
        //Bundle args = new Bundle();
        //args.putSerializable(getString(R.string.user_key), theUsername);
        //args.putSerializable(getString(R.string.pw_key), thePassword);
        // registerFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.launchFragmentContainer, registerFragment, getString(R.string.keys_fragment_register))
                .addToBackStack(null);  //used to have "reg" as param. Think this has to do with popping back stack.

        // Commit the transaction
        transaction.commit();
    }

    /**
     * This method opens the PasswordFragment for resetting the password when "Forgot Password" is
     * clicked from the login fragment.
     */
    @Override
    public void onForgotButtonClick() {
        PasswordFragment passwordFragment;

        passwordFragment = new PasswordFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.launchFragmentContainer, passwordFragment, "passwordFragmentTag")
                .addToBackStack(null);
        //commiting the transaction
        transaction.commit();
    }

    /**
     * This method loads the MainActivity which has all the chat features.
     * @param theUsername the username of the current user which is passed to Main Activity.
     */
    private void loadMainActivity(String theUsername) {    //Load MainActivtity once logged in or already loggede in.
        //getSupportFragmentManager().popBackStack("reg", FragmentManager.POP_BACK_STACK_INCLUSIVE);
 /*       DisplayFragment successFragment = new DisplayFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, successFragment);
        // Commit the transaction
        Log.d("TestingMyApp", "About to load success");
        transaction.commit();
  */    Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.USER_KEY, theUsername);
        startActivity(intent);
    }

    /**
     * Handle errors that may occur during the AsyncTask.
     * @param result the error message provide from the AsyncTask
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNCT_TASK_ERROR", result);
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleLoginOnPost(String result) {
        findViewById(R.id.loginProgress).setVisibility(View.INVISIBLE);
        findViewById(R.id.loginB).setEnabled(true);
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            if (success) {
                checkStayLoggedIn();
                //Login was successful. Switch to the loadSuccessFragment.
                loadMainActivity(mCredentials.getUsername());
            } else {
                //Login was unsuccessful. Don’t switch fragments and inform the user
                LoginFragment frag =
                        (LoginFragment) getSupportFragmentManager()
                                .findFragmentByTag(
                                        getString(R.string.keys_fragment_login));
                frag.setError("Log in unsuccessful");
            }
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    /**
     * This method handles saving the username and updating the shared preferences if the user
     * would like to stay logged in.
     */
    private void checkStayLoggedIn() {
        if (((CheckBox) findViewById(R.id.logCheckBox)).isChecked()) {
            SharedPreferences prefs =
                    getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            //save the username for later usage
            prefs.edit().putString(
                    getString(R.string.keys_prefs_username),
                    mCredentials.getUsername())
                    .apply();
            //save the users “want” to stay logged in
            prefs.edit().putBoolean(
                    getString(R.string.keys_prefs_stay_logged_in),
                    true)
                    .apply();
        }
    }

    /**
     * This method handles the response from the web server when the register request is sent to
     * the web server. The result is displayed as a Toast.
     * @param result The result from the web server.
     */
    private void handleRegisterOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            findViewById(R.id.registerProgress).setVisibility(View.INVISIBLE);
            findViewById(R.id.regB).setEnabled(true);
            if (success) {
                //Login was successful. Switch to the loadSuccessFragment.
                Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                onBackPressed();
            } else {
                //Login was unsuccessful. Don’t switch fragments and inform the user
                RegisterFragment frag =
                        (RegisterFragment) getSupportFragmentManager()
                                .findFragmentByTag(
                                        getString(R.string.keys_fragment_register));
                //frag.setError("Register Unsuccessful");
                Toast.makeText(getApplicationContext(), "Register Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

}
