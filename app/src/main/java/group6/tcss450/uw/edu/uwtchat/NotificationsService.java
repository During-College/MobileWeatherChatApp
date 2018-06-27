package group6.tcss450.uw.edu.uwtchat;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import group6.tcss450.uw.edu.uwtchat.model.Notification;

public class NotificationsService extends IntentService {
    public static final String RECEIVED_UPDATE = "Received String";

    public IBinder onBind(Intent intent) {
        return null;
    }


    //60 seconds - 1 minute is the minimum...
    private static final int POLL_INTERVAL = 10_000;
    private static final String GROUP_KEY = "group6.tcss450.uwtchatnote";

    private static final String TAG = "NotificationsService";
    private List<Notification> mNotifications = new ArrayList<>();
    private List<Notification> mPrevNotes = new ArrayList<>();

    public NotificationsService() {
        super("NotificationsService");
    }

    /**
     * This method gets called periodically by the Alarm Manager)
     * @param intent The arguments from the Alarm.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d(TAG, "Performing the service for " + intent.getStringExtra("username"));
            checkWebservice(intent.getBooleanExtra("createNotification", true), intent.getStringExtra("username"));
        }
    }

    /**
     * This method creates the Alarm to run every minute to check for new notifications.
     * @param context The context the alarm is attached to.
     * @param createNotification Whether a notification should be created or not.
     * @param username the username to provide the webserver with to get the notifications.
     */
    public static void startServiceAlarm(Context context, boolean createNotification, String username) {
        Intent i = new Intent(context, NotificationsService.class);
        i.putExtra("createNotification", createNotification);
        i.putExtra("username", username);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP
                , POLL_INTERVAL
                , POLL_INTERVAL, pendingIntent);
    }

    /**
     * This method stops the alarm so that notifications don't occur while the app is in the
     * foreground.
     * @param context
     */
    public static void stopServiceAlarm(Context context) {
        Intent i = new Intent(context, NotificationsService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    /**
     * This method contains the code to check the webservice for notifications.
     * @param createNotification flag to determine if a notification should be created.
     * @param username The username the webservice should check notifications for.
     */
    private void checkWebservice(boolean createNotification, String username) {

        Uri retrieve = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_unread_notifications))
                .appendQueryParameter("username", username)
                //.appendQueryParameter("type", "message")
                .build();

        StringBuilder response = new StringBuilder();
        HttpURLConnection urlConnection = null;
        response = new StringBuilder();
        try {
            URL urlObject = new URL(retrieve.toString());
            urlConnection = (HttpURLConnection) urlObject.openConnection();
            InputStream content = urlConnection.getInputStream();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String s;
            while ((s = buffer.readLine()) != null) {
                response.append(s);
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        // response = the json
        if (createNotification) {
            String responseString = response.toString();
            try {
                JSONObject resultObject = new JSONObject(responseString);
                if(resultObject.has(getString(R.string.keys_json_notifications)))
                {
                    mNotifications.clear();
                    JSONArray resultArray = resultObject.getJSONArray(getString(R.string.keys_json_notifications));
                    for (int i = 0; i < resultArray.length(); i++) {
                        JSONObject theNotification = resultArray.getJSONObject(i);
                        //String primaryKey = theNotification.getString(getString(R.string.keys_json_primary_key));
                        String sender = theNotification.getString(getString(R.string.keys_json_sender));
                        String message = theNotification.getString(getString(R.string.keys_json_message));
                        String chatid = theNotification.getString(getString(R.string.keys_json_chatid));
                        String timestamp = theNotification.getString(getString(R.string.keys_json_timestamp));
                        mNotifications.add(new Notification(sender,message,chatid,timestamp));
                    }

                    Intent i = new Intent(RECEIVED_UPDATE);
                    //add bundle to send the response to any receivers
                    if(mNotifications.isEmpty())
                        i.putExtra(getString(R.string.keys_json_notifications), "");
                    else
                        i.putExtra(getString(R.string.keys_json_notifications), response.toString());
                    
                    sendBroadcast(i);
                    buildNotification(username);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Log.e("test", response.toString()); shows its running
        }
    }

    /**
     * This method creates the UI for the notification.
     * @param theUsername username of the current user.
     */
    public void buildNotification(String theUsername) {
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
        //mNotificationManager.cancelAll();
            for(Notification n: mNotifications)
            {
                if(mPrevNotes.contains(n))
                    continue;

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_badge)
                        .setContentTitle(n.getSender())
                        //.setStyle(style)
                        .setContentText(n.getMessage())
                        .setGroup(GROUP_KEY);

                Intent notifyIntent =
                        new Intent(this, MainActivity.class);

                if(n.isConnection())
                {
                    notifyIntent.putExtra(MainActivity.KEY, getString(R.string.keys_fragment_received_connections));
                }
                else
                {
                    notifyIntent.putExtra(MainActivity.KEY, getString(R.string.keys_fragment_individual_chat));
                    notifyIntent.putExtra(MainActivity.CHAT_ID_KEY, n.getChatID());
                }

                notifyIntent.putExtra(MainActivity.USER_KEY, theUsername);

                // Sets the Activity to start in a new, empty task
                notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                // Creates the PendingIntent
                PendingIntent notifyPendingIntent =
                        PendingIntent.getActivity(
                                this,
                                0,
                                notifyIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                // Puts the PendingIntent into the notification builder
                mBuilder.setContentIntent(notifyPendingIntent);
                mBuilder.setAutoCancel(true);

                if(n.isConnection())
                    mNotificationManager.notify(n.getSender().hashCode(), mBuilder.build());
                else
                    mNotificationManager.notify(Integer.parseInt(n.getChatID()), mBuilder.build());
            }

            mPrevNotes = mNotifications;

    }
}
