package com.rahul.vanished;

import android.app.Application;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;

import java.text.ParseException;

public class VanishedApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.initialize(this, "PGyg33OBVvySUIgAEgrWXQkDNHxygk43V2WPXrLi", "IFSdonn8088rl63yQEnXmlsZafKrGxXgQZX1ZSzL");
        ParseFacebookUtils.initialize(this);

        PushService.setDefaultPushCallback(this,MainActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();

    }

    public static void updateParseInstallation(ParseUser user){
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USERID,user.getObjectId());
        installation.saveInBackground();
    }
}
