package com.pdroid84.deb.googlesignintest;

import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;


public class ProfileDetails extends ActionBarActivity {

    EditText nameText, emailText, profText;
    ImageView mImageView;
    Bundle mBundle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);
        nameText = (EditText) findViewById(R.id.editTextName);
        emailText = (EditText) findViewById(R.id.editTextEmail);
        profText = (EditText) findViewById(R.id.editTextProfile);
        mImageView = (ImageView) findViewById(R.id.prof_pic1);

        mBundle = getIntent().getExtras();
        nameText.setText(mBundle.getString("name"));
        emailText.setText(mBundle.getString("email"));
        profText.setText(mBundle.getString("profile"));
        mImageView.setImageBitmap((Bitmap) mBundle.getParcelable("profpic"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_details, menu);
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
}
