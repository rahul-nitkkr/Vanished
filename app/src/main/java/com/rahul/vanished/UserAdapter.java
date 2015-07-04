package com.rahul.vanished;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class UserAdapter extends ArrayAdapter<ParseUser> {
	
	protected Context mContext;
	protected List<ParseUser> mUsers;


	public UserAdapter(Context context, List<ParseUser> users) {
		super(context, R.layout.message_item, users);
		mContext = context;
		mUsers = users;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
        ParseUser user = mUsers.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.user_item, null);
			holder = new ViewHolder();
			holder.userImageView = (ProfilePictureView)convertView.findViewById(R.id.userImageView);
			holder.nameLabel = (TextView)convertView.findViewById(R.id.nameLabel);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder)convertView.getTag();

            if (user.has("profile")) {
                JSONObject userProfile = user.getJSONObject("profile");
                try {
                    Log.d("FROM USER ADAPTER -->",userProfile.getString("facebookId"));
                    if (userProfile.has("facebookId")) {
                        holder.userImageView.setProfileId(userProfile.getString("facebookId"));
                    }
                }catch(JSONException e){
                    Log.d("UserAdapter", "Error parsing saved user data.");
                }
            }
            else{

                holder.userImageView.setDefaultProfilePicture(BitmapFactory.decodeResource(getContext().getResources(),R.drawable.avatar_empty));
            }

		}

        holder.nameLabel.setText(user.getUsername());
				
//		if (user.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_IMAGE)) {
//			holder.iconImageView.setImageResource(R.drawable.ic_picture);
//		}
//		else {
//			holder.iconImageView.setImageResource(R.drawable.ic_video);
//		}

		
		return convertView;
	}
	
	private static class ViewHolder {
        ProfilePictureView userImageView;
		TextView nameLabel;
	}
	
	public void refill(List<ParseUser> users) {
		mUsers.clear();
		mUsers.addAll(users);
		notifyDataSetChanged();
	}
}






