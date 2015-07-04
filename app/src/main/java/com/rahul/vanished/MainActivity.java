package com.rahul.vanished;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements
        ActionBar.TabListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;
    public static final int CHOOSE_PHOTO_REQUEST = 2;
    public static final int CHOOSE_VIDEO_REQUEST = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    public static final int FILE_SIZE_LIMIT = 1024*1024*10;
    protected Uri mMediaUri;


    protected DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0:
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    if(mMediaUri == null){
                        Toast.makeText(MainActivity.this,R.string.error_external_storage,Toast.LENGTH_LONG).show();
                    }else {
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }
                    break;
                case 1:
                    Intent takeVideoIntent= new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    if(mMediaUri == null){
                        Toast.makeText(MainActivity.this,R.string.error_external_storage,Toast.LENGTH_LONG).show();
                    }else {
                        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,mMediaUri);
                        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,10);
                        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0);
                        startActivityForResult(takeVideoIntent, TAKE_VIDEO_REQUEST);
                    }
                    break;
                case 2:
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("image/*");
                    startActivityForResult(choosePhotoIntent,CHOOSE_PHOTO_REQUEST);
                    break;
                case 3:
                    Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseVideoIntent.setType("video/*");
                    Toast.makeText(MainActivity.this,"Video File size should be less than 10MB.",Toast.LENGTH_LONG).show();
                    startActivityForResult(chooseVideoIntent, CHOOSE_VIDEO_REQUEST);
                    break;
            }
        }

        private Uri getOutputMediaFileUri(int mediaType) {
            if(isExternalStorageAvailable()){
                String appName = MainActivity.this.getString(R.string.app_name);
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),appName);
                if(!mediaStorageDir.exists()){
                    if(! mediaStorageDir.mkdirs()){
                        Log.e(TAG,"Failed to create Directory.");
                        return null;
                    }
                }

                File mediaFile;
                Date now = new Date();
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);
                String path = mediaStorageDir.getPath() + File.separator;
                if(mediaType == MEDIA_TYPE_IMAGE){
                    mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
                }else if(mediaType == MEDIA_TYPE_VIDEO){
                    mediaFile = new File(path + "VID_" + timestamp + ".mp4");
                }else{
                    return null;
                }

                return Uri.fromFile(mediaFile);
            }
           return null;
        }

        private boolean isExternalStorageAvailable(){
            String state = Environment.getExternalStorageState();
            if(state.equals(Environment.MEDIA_MOUNTED)){
                return true;
            }else{
                return false;
            }
        }
    };

    SectionsPagerAdapter mSectionsPagerAdapter;


    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_main);

        ParseAnalytics.trackAppOpened(getIntent());

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
        } else {
            if ((currentUser != null) && currentUser.isAuthenticated()) {
                makeMeRequest();

            }

            Log.i(TAG, currentUser.getUsername());
        }

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this,
                getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            if(requestCode == CHOOSE_PHOTO_REQUEST || requestCode == CHOOSE_VIDEO_REQUEST){
                if(data == null){
                    Toast.makeText(this,"Sorry! There was an error!",Toast.LENGTH_LONG).show();
                }else{
                    mMediaUri = data.getData();
                    Log.d(TAG,mMediaUri.toString());
                }

                if(requestCode == CHOOSE_VIDEO_REQUEST){
                    int fileSize = 0;
                    InputStream inputStream = null;
                    try{
                    inputStream = getContentResolver().openInputStream(mMediaUri);
                    fileSize = inputStream.available();
                }catch(FileNotFoundException e){
                        Toast.makeText(this,"There was a problem with opening the file!",Toast.LENGTH_LONG).show();
                        return;
                    }
                    catch(IOException e){
                        Toast.makeText(this,"There was a problem with opening the file!",Toast.LENGTH_LONG).show();
                        return;
                    }
finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(fileSize >= FILE_SIZE_LIMIT){
                        Toast.makeText(this,"This Video is too large, please select a smaller video.",Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
                Log.d(TAG,mediaScanIntent.getDataString());
            }

            Intent recipientsIntent = new Intent(this,RecepientsActivity.class);
            recipientsIntent.setData(mMediaUri);

            String mFileType;
            if(requestCode == CHOOSE_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST){
                mFileType = ParseConstants.KEY_FILE_IMAGE;
            }else if(requestCode == CHOOSE_VIDEO_REQUEST || requestCode == TAKE_VIDEO_REQUEST){
                mFileType = ParseConstants.KEY_FILE_VIDEO;
            }else{
                mFileType = "UNDEFINED";
            }
            recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE,mFileType);
            Log.d(TAG,mFileType + "----->" + mMediaUri);
            startActivity(recipientsIntent);

        }else if(resultCode == RESULT_CANCELED){
            Toast.makeText(this,"Sorry! There was an error!",Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_logout:
                ParseUser.logOut();
                navigateToLogin();
                break;

            case R.id.action_edit_friends:
                Intent intent = new Intent(this, EditFriendsActivity.class);
                startActivity(intent);
                break;

            case R.id.action_camera:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_choices, mDialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab,
                              FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }


    private void makeMeRequest() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                        if (jsonObject != null) {
                            JSONObject userProfile = new JSONObject();
                            ParseUser currentUser = ParseUser.getCurrentUser();
                            try {
                                userProfile.put("facebookId", jsonObject.getLong("id"));
                                currentUser.put(ParseConstants.KEY_USERNAME,jsonObject.getString("name"));

                                if (jsonObject.getString("gender") != null)
                                    userProfile.put("gender", jsonObject.getString("gender"));

                                if (jsonObject.getString("email") != null)
                                    currentUser.put("email",jsonObject.getString("email"));
                                Log.d(TAG,jsonObject.getString("email"));

                                // Save the user profile info in a user property
                                currentUser.put("profile", userProfile);
                                currentUser.saveInBackground();

                                // Show the user info
                                //updateViewsWithProfileInfo();
                            } catch (JSONException e) {
                                Log.d(TAG,
                                        "Error parsing returned user data. " + e);
                            }
                        } else if (graphResponse.getError() != null) {
                            switch (graphResponse.getError().getCategory()) {
                                case LOGIN_RECOVERABLE:
                                    Log.d(TAG,
                                            "Authentication error: " + graphResponse.getError());
                                    break;

                                case TRANSIENT:
                                    Log.d(TAG,
                                            "Transient error. Try again. " + graphResponse.getError());
                                    break;

                                case OTHER:
                                    Log.d(TAG,
                                            "Some other error: " + graphResponse.getError());
                                    break;
                            }
                        }
                    }
                });

        request.executeAsync();
    }


}
