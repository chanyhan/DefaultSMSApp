package com.defaultsms.app.activity;

//import com.android.mms.MmsApp;
//import com.android.mms.data.ContactList;
import com.defaultsms.app.LogUtil;
import com.defaultsms.app.R;

import android.app.ActionBar;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.Sms.Conversations;
import android.support.v4.widget.SimpleCursorAdapter;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ComposeMessage extends Activity{

    static final String[] PROJECTION = new String[] {
        // TODO: should move this symbol into com.android.mms.telephony.Telephony.
        MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        BaseColumns._ID,
        Conversations.THREAD_ID,
        // For SMS
        Sms.ADDRESS,
        Sms.BODY,
        Sms.DATE,
        Sms.DATE_SENT,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.LOCKED,
        Sms.ERROR_CODE,
        // For MMS
        Mms.SUBJECT,
        Mms.SUBJECT_CHARSET,
        Mms.DATE,
        Mms.DATE_SENT,
        Mms.READ,
        Mms.MESSAGE_TYPE,
        Mms.MESSAGE_BOX,
        Mms.DELIVERY_REPORT,
        Mms.READ_REPORT,
        PendingMessages.ERROR_TYPE,
        Mms.LOCKED,
        Mms.STATUS,
        Mms.TEXT_ONLY
    };

    // The indexes of the default columns which must be consistent
    // with above PROJECTION.
    static final int COLUMN_MSG_TYPE            = 0;
    static final int COLUMN_ID                  = 1;
    static final int COLUMN_THREAD_ID           = 2;
    static final int COLUMN_SMS_ADDRESS         = 3;
    static final int COLUMN_SMS_BODY            = 4;
    static final int COLUMN_SMS_DATE            = 5;
    static final int COLUMN_SMS_DATE_SENT       = 6;
    static final int COLUMN_SMS_READ            = 7;
    static final int COLUMN_SMS_TYPE            = 8;
    static final int COLUMN_SMS_STATUS          = 9;
    static final int COLUMN_SMS_LOCKED          = 10;
    static final int COLUMN_SMS_ERROR_CODE      = 11;
    static final int COLUMN_MMS_SUBJECT         = 12;
    static final int COLUMN_MMS_SUBJECT_CHARSET = 13;
    static final int COLUMN_MMS_DATE            = 14;
    static final int COLUMN_MMS_DATE_SENT       = 15;
    static final int COLUMN_MMS_READ            = 16;
    static final int COLUMN_MMS_MESSAGE_TYPE    = 17;
    static final int COLUMN_MMS_MESSAGE_BOX     = 18;
    static final int COLUMN_MMS_DELIVERY_REPORT = 19;
    static final int COLUMN_MMS_READ_REPORT     = 20;
    static final int COLUMN_MMS_ERROR_TYPE      = 21;
    static final int COLUMN_MMS_LOCKED          = 22;
    static final int COLUMN_MMS_STATUS          = 23;
    static final int COLUMN_MMS_TEXT_ONLY       = 24;

    public static final int INCOMING_ITEM_TYPE_SMS = 0;
    public static final int OUTGOING_ITEM_TYPE_SMS = 1;
    public static final int INCOMING_ITEM_TYPE_MMS = 2;
    public static final int OUTGOING_ITEM_TYPE_MMS = 3;
    
    Cursor mCursor=null;
    ListView	mListView;
    SimpleCursorAdapter mAdapter;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.compose_message);
    	
    	Uri uri=getIntent().getData();
    	if(uri!=null){
    		mCursor=getContentResolver().query(uri, PROJECTION, null, null, null);
    		if(mCursor!=null){
    			LogUtil.d("ThreadId="+uri.getLastPathSegment()+", mCursor="+mCursor.getCount());
    		}
    	}
    	
    	mListView=(ListView)findViewById(R.id.listview);
    	mAdapter=new SimpleCursorAdapter(this,
    			R.layout.compose_message_list_item,
    			mCursor,
    			new String[]{},
    			new int[]{},
    			SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER){
    		@Override
    		public View getView(int position, View convertView, ViewGroup parent){
    			View v=super.getView(position, convertView, parent);
    			Cursor c=getCursor();
    			c.moveToPosition(position);
    			int type=getItemViewType(c);
    			
    			TextView tv=(TextView)v.findViewById(R.id.text_view);
    			View left=v.findViewById(R.id.avatar_left);
    			View right=v.findViewById(R.id.avatar_right);
    				
    			switch(type){
    			case INCOMING_ITEM_TYPE_SMS:
    				tv.setText(c.getString(COLUMN_SMS_BODY));
    				tv.setGravity(Gravity.LEFT);
    				left.setVisibility(View.VISIBLE);
    				right.setVisibility(View.GONE);
    				break;
    			case INCOMING_ITEM_TYPE_MMS:
    				tv.setText(c.getString(COLUMN_MMS_TEXT_ONLY));
    				tv.setGravity(Gravity.LEFT);
    				left.setVisibility(View.VISIBLE);
    				right.setVisibility(View.GONE);
    				break;
    			case OUTGOING_ITEM_TYPE_SMS:
    				tv.setText(c.getString(COLUMN_SMS_BODY));
    				tv.setGravity(Gravity.RIGHT);
    				left.setVisibility(View.GONE);
    				right.setVisibility(View.VISIBLE);    				
    				break;
    			case OUTGOING_ITEM_TYPE_MMS:
    				tv.setText(c.getString(COLUMN_MMS_TEXT_ONLY));
    				tv.setGravity(Gravity.RIGHT);
    				left.setVisibility(View.GONE);
    				right.setVisibility(View.VISIBLE);
    				break;
    			}
    			
    			return v;
    		}
    	    
    	    public int getItemViewType(Cursor cursor) {
    	        String type = cursor.getString(COLUMN_MSG_TYPE);
    	        int boxId;
    	        if ("sms".equals(type)) {
    	            boxId = cursor.getInt(COLUMN_SMS_TYPE);
    	            // Note that messages from the SIM card all have a boxId of zero.
    	            return (boxId == TextBasedSmsColumns.MESSAGE_TYPE_INBOX ||
    	                    boxId == TextBasedSmsColumns.MESSAGE_TYPE_ALL) ?
    	                    INCOMING_ITEM_TYPE_SMS : OUTGOING_ITEM_TYPE_SMS;
    	        } else {
    	            boxId = cursor.getInt(COLUMN_MMS_MESSAGE_BOX);
    	            // Note that messages from the SIM card all have a boxId of zero: Mms.MESSAGE_BOX_ALL
    	            return (boxId == Mms.MESSAGE_BOX_INBOX || boxId == Mms.MESSAGE_BOX_ALL) ?
    	                    INCOMING_ITEM_TYPE_MMS : OUTGOING_ITEM_TYPE_MMS;
    	        }
    	    }     		
    	};
    	
    	mListView.setAdapter(mAdapter);
    	
    }
   
    
    private void updateTitle() {
        String title = null;
        String subTitle = null;


        ActionBar actionBar = getActionBar();
        actionBar.setTitle(title);
        actionBar.setSubtitle(subTitle);
    }    
    
}