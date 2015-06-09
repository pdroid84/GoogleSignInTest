package com.pdroid84.deb.googlesignintest;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    private static String TAG = "DEB-MainActivity";
    /**
     * True if the sign-in button was clicked.  When true, we know to resolve all
     * issues preventing sign-in without waiting.
     */
    private boolean mSignInClicked;
    /**
     * True if we are in the process of resolving a ConnectionResult
     */
    private boolean mIntentInProgress = false;

    ImageView mImageView;
    Bitmap profImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Create and initialize GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                                .addConnectionCallbacks(this)
                                .addOnConnectionFailedListener(this)
                                .addApi(Plus.API)
                                .addScope(new Scope("profile"))
                                .build();

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart is called");
        //start the GoogleClient
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop is called");
        //stop the GoogleClient
        mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG,"onConnected is called");
        mSignInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            final String personName = currentPerson.getDisplayName();
            Person.Image personPhoto = currentPerson.getImage();
            final String personGooglePlusProfile = currentPerson.getUrl();
            final String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
            Toast.makeText(this, "User name=" + personName + " , email = " + email + " and profile = " + personGooglePlusProfile, Toast.LENGTH_LONG).show();
            Log.d(TAG, "Person Name = " + personName);
            Log.d(TAG, "Person email = " + email);
            Log.d(TAG, "Person Profile= " + personGooglePlusProfile);
            mImageView = (ImageView) findViewById(R.id.prof_pic);
            if (currentPerson.hasImage()) {
                Person.Image image = currentPerson.getImage();
                new AsyncTask<String, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        try {
                            URL url = new URL(params[0]);
                            InputStream in = url.openStream();
                            return BitmapFactory.decodeStream(in);
                        } catch (Exception e) {
                        /* TODO log error */
                            Log.d(TAG, "Error while retrieving the profile image");
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        mImageView.setImageBitmap(bitmap);
                        profImage = bitmap;
                        callProfileDetails(personName,email,personGooglePlusProfile);
                    }
                }.execute(image.getUrl());
            }
        }

    }

    public void callProfileDetails (String pName, String pEmail, String pProfile) {
        Intent mIntent = new Intent(this, ProfileDetails.class);
        Bundle mBundle1 = new Bundle();
        mBundle1.putString("name", pName);
        mBundle1.putString("email", pEmail);
        mBundle1.putString("profile", pProfile);
        //there will be bit delay in displaying in second activity as the pic is retrieved in AsyncTask
        mBundle1.putParcelable("profpic",profImage);
        mIntent.putExtras(mBundle1);
        startActivity(mIntent);
    }
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG,"onConnectionSuspended is called. The cause code is " + cause);
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG,"onClick is called");
        if (view.getId() == R.id.sign_in_button && !mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
        if (view.getId() == R.id.sign_out_button) {
            // Clear the default account so that GoogleApiClient will not automatically
            // connect in the future.
            // [START sign_out_clicked]
            if (mGoogleApiClient.isConnected()) {
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                Toast.makeText(this, "User is signed out now!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        Log.d(TAG,"onConnectionFailed is called");
        if (!mIntentInProgress) {
            if (mSignInClicked && result.hasResolution()) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                try {
                    result.startResolutionForResult(this, RC_SIGN_IN);
                    mIntentInProgress = true;
                } catch (IntentSender.SendIntentException e) {
                    // The intent was canceled before it was sent.  Return to the default
                    // state and attempt to connect to get an updated ConnectionResult.
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    //Because the resolution for the connection failure was started with startIntentSenderForResult
    // and the code RC_SIGN_IN, we can capture the result inside Activity.onActivityResult.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG,"onActivityResult is called");
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.reconnect();
            }
        }
    }
}
