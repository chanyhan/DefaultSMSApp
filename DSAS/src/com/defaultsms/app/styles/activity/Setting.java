package com.defaultsms.app.styles.activity;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.auth.FacebookHandle;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class Setting extends Activity implements OnClickListener{
	@Override
	public void onCreate(Bundle b){
		super.onCreate(b);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	private static final List<String> FACEBOOK_PERMISSIONS = Arrays.asList(
			"user_friends",
			"email",
			"user_birthday",
			"user_groups",
			"user_events",
			"friends_birthday",
			"read_friendlists" );
	
	public void auth_facebook(){
    
    FacebookHandle handle = new FacebookHandle(this, "654386217936297", "");
    
    String url = "https://graph.facebook.com/me/feed";
    AQuery aq=new AQuery(this);
    aq.auth(handle).ajax(url, null, JSONObject.class, new AjaxCallback<JSONObject>(){
		@Override
		public void callback(String url, JSONObject j, AjaxStatus status) {
			
		}
    });
        
}	
	
}