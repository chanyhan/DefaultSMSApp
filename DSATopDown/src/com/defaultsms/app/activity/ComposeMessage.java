package com.defaultsms.app.activity;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import com.defaultsms.app.LogUtil;
import com.defaultsms.app.R;
import com.defaultsms.app.mms.pdu.CharacterSets;
import com.defaultsms.app.mms.pdu.EncodedStringValue;

import android.app.ActionBar;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.Threads;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.Sms.Conversations;
import android.support.v4.widget.SimpleCursorAdapter;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class ComposeMessage extends Activity implements OnClickListener{

	private static Uri sAllCanonical = Uri.parse("content://mms-sms/canonical-addresses");
	
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
    
    long	mThreadId=-1;
    private String	mNumber;
    Cursor mCursor=null;
    ListView	mListView;
    SimpleCursorAdapter mAdapter;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.compose_message);
    	
    	mNumber=getIntent().getStringExtra(Sms.ADDRESS);
    	if(mNumber!=null){
    		LogUtil.d("Number="+mNumber);
    	}
    	Uri uri=getIntent().getData();
    	if(uri!=null){
    		// show previous conversation
    		mCursor=getContentResolver().query(uri, PROJECTION, null, null, null);
    		if(mCursor!=null){
    			try{
    			mThreadId=Long.parseLong(uri.getLastPathSegment());
    			}catch(Exception e){
    				e.printStackTrace();
    			}
    			LogUtil.d("ThreadId="+mThreadId+", mCursor="+mCursor.getCount());
    		}
    	}else{
    		//create new
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
    				tv.setText(extractEncStrFromCursor(c, COLUMN_MMS_SUBJECT, COLUMN_MMS_SUBJECT_CHARSET));
    				tv.setGravity(Gravity.LEFT);
    				Cursor temp=getContentResolver().query(Uri.parse("content://mms/"+c.getInt(COLUMN_ID)+"/part"), null, "ct=\'text/plain\'", null, null);
    				if(temp!=null && temp.getCount()>0){
    					temp.moveToFirst();
    					String text=temp.getString(temp.getColumnIndex("text"));
    					tv.setText(tv.getText()+"\n"+text);
    					temp.close();
    				}
    				
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
    				//String sub=extractEncStrFromCursor(c, COLUMN_MMS_SUBJECT, COLUMN_MMS_SUBJECT_CHARSET);
    				tv.setText(extractEncStrFromCursor(c, COLUMN_MMS_SUBJECT, COLUMN_MMS_SUBJECT_CHARSET));
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
    	updateTitle();
    }
   
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	if(mCursor!=null){
    		mCursor.close();
    	}
    	mCursor=null;
    }
    private void updateTitle() {
        String title = null;
        String subTitle = null;

        for(int i=0;i<mCursor.getCount();i++){
        	mCursor.moveToPosition(i);
        	LogUtil.d("position="+i+", string="+mCursor.getString(COLUMN_SMS_ADDRESS));
        }
        
        // Update with name
        title=loadNameFromNumber(mNumber);
        if(TextUtils.isEmpty(title)){
        	title=mNumber;
        }
        if(title!=null && !title.equals(mNumber)){
        	subTitle=mNumber;
        }

        ActionBar actionBar = getActionBar();
        actionBar.setTitle(title);
        actionBar.setSubtitle(subTitle);
        
        actionBar.setHomeButtonEnabled(true);
    }
    
    private static final String[] CALLER_ID_PROJECTION = new String[] {
        Phone._ID,                      // 0
        Phone.NUMBER,                   // 1
        Phone.LABEL,                    // 2
        Phone.DISPLAY_NAME,             // 3
        Phone.CONTACT_ID,               // 4
        Phone.CONTACT_PRESENCE,         // 5
        Phone.CONTACT_STATUS,           // 6
        Phone.NORMALIZED_NUMBER,        // 7
        //Contacts.SEND_TO_VOICEMAIL      // 8
    };
    //private static final int PHONE_ID_COLUMN = 0;
    //private static final int PHONE_NUMBER_COLUMN = 1;
    //private static final int PHONE_LABEL_COLUMN = 2;
    private static final int CONTACT_NAME_COLUMN = 3;
    //private static final int CONTACT_ID_COLUMN = 4;
    //private static final int CONTACT_PRESENCE_COLUMN = 5;
    //private static final int CONTACT_STATUS_COLUMN = 6;
    //private static final int PHONE_NORMALIZED_NUMBER = 7;
    //private static final int SEND_TO_VOICEMAIL = 8;    
    
    private static final String CALLER_ID_SELECTION_WITHOUT_E164 =  " Data._ID IN "
            + " (SELECT DISTINCT lookup.data_id "
            + " FROM "
                + " (SELECT data_id, normalized_number, length(normalized_number) as len "
                + " FROM phone_lookup "
                + " WHERE min_match = ?) AS lookup "
            + " WHERE "
                + " (lookup.len <= ? AND "
                    + " substr(?, ? - lookup.len + 1) = lookup.normalized_number))";
    
    private String loadNameFromNumber(String number) {
    	if(TextUtils.isEmpty(number)){
    		return "";
    	}
        String normalizedNumber = normalizeNumber(number);
        String minMatch = PhoneNumberUtils.toCallerIDMinMatch(normalizedNumber);
        if (!TextUtils.isEmpty(normalizedNumber) && !TextUtils.isEmpty(minMatch)) {
            String numberLen = String.valueOf(normalizedNumber.length());
            String selection;
            String[] args;
            selection = CALLER_ID_SELECTION_WITHOUT_E164;
            args = new String[] {minMatch, numberLen, normalizedNumber, numberLen};
            Cursor cursor = getContentResolver().query(Data.CONTENT_URI, CALLER_ID_PROJECTION, selection, args, null);
            if(cursor!=null && cursor.getCount()>0){
            	cursor.moveToFirst();
            	String name= cursor.getString(CONTACT_NAME_COLUMN);
            	cursor.close();
            	return name;
            }
        }
        return number;
    }
    
    public static String normalizeNumber(String phoneNumber) {
        StringBuilder sb = new StringBuilder();
        int len = phoneNumber.length();
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            // Character.digit() supports ASCII and Unicode digits (fullwidth, Arabic-Indic, etc.)
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                sb.append(digit);
            } else if (i == 0 && c == '+') {
                sb.append(c);
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                return normalizeNumber(PhoneNumberUtils.convertKeypadLettersToDigits(phoneNumber));
            }
        }
        return sb.toString();
    }
    
    public static byte[] getBytes(String data) {
        try {
            return data.getBytes(CharacterSets.MIMENAME_ISO_8859_1);
        } catch (UnsupportedEncodingException e) {
            // Impossible to reach here!
            return new byte[0];
        }
    }
    
    public static String extractEncStrFromCursor(Cursor cursor,  int columnRawBytes, int columnCharset) {
        String rawBytes = cursor.getString(columnRawBytes);
        int charset = cursor.getInt(columnCharset);
        if (TextUtils.isEmpty(rawBytes)) {
            return "";
        } else if (charset == CharacterSets.ANY_CHARSET) {
            return rawBytes;
        } else {
            return new EncodedStringValue(charset, getBytes(rawBytes)).getString();
        }
    }     
    
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}    
    
}