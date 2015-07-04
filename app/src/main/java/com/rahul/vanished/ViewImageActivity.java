package com.rahul.vanished;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;


public class ViewImageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        setupActionBar();

        ImageView mImageView = (ImageView)findViewById(R.id.imageView);
        Uri imageUri = getIntent().getData();

        Picasso.with(this).load(imageUri.toString()).into(mImageView);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        },30*1000);
    }

    private void setupActionBar(){
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }



}
