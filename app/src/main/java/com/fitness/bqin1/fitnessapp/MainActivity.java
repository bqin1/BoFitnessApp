package com.fitness.bqin1.fitnessapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author: Bo Qin
 *
 * This is a sample activity that simulates a fitness app, the goal is to demonstrate
 * my Android abilities relating to app building, encryption, android preferences, notifications.
 * Thus alot of features in the app are simulated.
 *
 * There are 2 users, user1/password1 and user2/password2
 * You will notice that step distance between user1 and user2 are persistent.
 * There is display update when logging in
 * There is a feedback for every 1000 feet
 * There is a periodic notification to stand up and walk
 */
public class MainActivity extends AppCompatActivity {

    Button buttonLogin;
    Button buttonWalk;
    TextView textviewName;
    TextView textviewDistance;

    String inputUsername;
    String inputUsernamedisplay;
    String inputPassword;
    String username; //user1 SHA256 Hashed
    String password; //password1 SHA256 Hashed
    String username2; //user2 SHA256 Hashed
    String password2; //password2 SHA256 Hashed
    String user_distance; //for shared preference use

    // User1 and User2 are stored in shared preferences. For a real world situation
    // I would create a SQLLite database and store their information.
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Grab the already hashed values from strings.xml, to be used
        // later when comparing login hashed info
        username = getResources().getString(R.string.username1);
        password = getResources().getString(R.string.password1);
        username2 = getResources().getString(R.string.username2);
        password2 = getResources().getString(R.string.password2);

        buttonLogin = (Button) this.findViewById(R.id.login_button);
        buttonWalk = (Button) this.findViewById(R.id.walk_button);
        textviewName = (TextView) this.findViewById(R.id.name_textview);
        textviewDistance = (TextView) this.findViewById(R.id.distance_textview);

        //Every 20 seconds reminder to walk
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(MainActivity.this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("20 second reminder")
                                .setContentText("Get up and take a walk!");

                Intent resultIntent = new Intent(MainActivity.this, MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                mNotificationManager.notify(0, mBuilder.build());
            }
        }, 0, 20000);

        // Handle walk 100 feet button
        buttonWalk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (sharedPref == null)
                {
                    Toast.makeText(getApplicationContext(),
                            "please login first",
                            Toast.LENGTH_SHORT)
                            .show();
                }else
                {
                    // Gets steps from user and increment 100
                    int distanceValue = sharedPref.getInt(user_distance, 0);
                    distanceValue = distanceValue + 100;
                    if (distanceValue % 1000 == 0)
                    {
                        Toast.makeText(getApplicationContext(),
                                "congrats on making " + distanceValue + " steps!",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                    // Updates steps for user
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(user_distance, distanceValue);
                    editor.commit();
                    distanceValue=sharedPref.getInt(user_distance, 0);
                    textviewDistance.setText(distanceValue + "");
                }
            }
        });

        // Handle Button click
        buttonLogin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                LayoutInflater li = LayoutInflater.from(MainActivity.this);
                View promptsView = li.inflate(R.layout.misc_lockprompt,
                        null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        MainActivity.this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.misc_lockprompt_edittext_username);
                final EditText passInput = (EditText) promptsView
                        .findViewById(R.id.misc_lockprompt_edittext_password);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        // get user input and set it to
                                        // result
                                        // edit text
                                        inputUsername = userInput.getText()
                                                .toString();
                                        inputUsernamedisplay = inputUsername;
                                        inputPassword = passInput.getText()
                                                .toString();

                                        MessageDigest digest;
                                        try {
                                            digest = MessageDigest
                                                    .getInstance("SHA-256");
                                            byte[] hashUser = digest.digest(inputUsername
                                                    .getBytes("UTF-8"));
                                            byte[] hashPass = digest.digest(inputPassword
                                                    .getBytes("UTF-8"));

                                            inputUsername = bytesToHexString(hashUser);
                                            inputPassword = bytesToHexString(hashPass);
                                        } catch (NoSuchAlgorithmException e) {
                                            e.printStackTrace();
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }

                                        if ((inputUsername
                                                .equalsIgnoreCase(username) && inputPassword
                                                .equalsIgnoreCase(password))
                                                || inputUsername
                                                .equalsIgnoreCase(username2)
                                                && inputPassword
                                                .equalsIgnoreCase(password2)) {

                                            Toast.makeText(getApplicationContext(),
                                                    "logged in",
                                                    Toast.LENGTH_SHORT)
                                                    .show();
                                            textviewName.setText(inputUsernamedisplay);

                                            // Reads the user's steps
                                            sharedPref = MainActivity.this.getSharedPreferences(
                                                    inputUsernamedisplay, Context.MODE_PRIVATE);
                                            user_distance = inputUsernamedisplay+"distance";

                                            int distanceValue = sharedPref.getInt(user_distance, 0);
                                            textviewDistance.setText(distanceValue + "");
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    "not logged in",
                                                    Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
