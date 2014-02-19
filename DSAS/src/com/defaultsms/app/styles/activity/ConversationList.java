package com.defaultsms.app.styles.activity;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;

import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.Telephony;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Threads;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.Sms.Conversations;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.defaultsms.app.styles.R;
import com.defaultsms.app.styles.LogUtil;
import com.defaultsms.app.styles.cache.ContactName;
import com.defaultsms.app.styles.mms.pdu.CharacterSets;
import com.defaultsms.app.styles.mms.pdu.EncodedStringValue;
//import android.provider.;
public class ConversationList extends Activity {

	ListView mListView;
	SimpleCursorAdapter mAdapter;
	Cursor	mCursor;
	String[] PRJ_SIMPLE={
			"_id","date","message_count","recipient_ids","snippet","snippet_cs","read","type","error","has_attachment",			
	};
	
	private static final int _ID=0;
	private static final int DATE=1;
	private static final int MESSAGE_COUNT=2;
	private static final int RECIPIENT_IDS=3;
	private static final int SNIPPET=4;
	private static final int SNIPPET_CS=5;
	private static final int READ=6;
	private static final int TYPE=7;
	private static final int ERROR=8;
	private static final int HAS_ATTACHMENT=9;
	
    private static Uri sAllCanonical = Uri.parse("content://mms-sms/canonical-addresses");
    private final Map<Long, String>mCache = new HashMap<Long, String>();
    private final Map<String, String>mNameCache = new HashMap<String, String>();

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AQuery aq=new AQuery(this);
		AQUtility.setDebug(true);
		
		Uri uri=Threads.CONTENT_URI;
		uri=uri.buildUpon().appendQueryParameter("simple", "true").build();
		mCursor=getContentResolver().query(uri, null, null, null, "date DESC");

		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_title));
		forDebugging(mCursor);
		
		mListView=(ListView)findViewById(R.id.listview);
		mAdapter=new SimpleCursorAdapter(this, R.layout.conversation_list_item,
				mCursor,
				new String[]{"date", "snippet"},
				new int[]{R.id.date,  R.id.subject},
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER){
			@Override
			public View getView(int position, View convertView, ViewGroup parent){
				View v=super.getView(position, convertView, parent);
				Cursor c=getCursor();
				c.moveToPosition(position);
				// Date
				long l=c.getLong(DATE);
				SimpleDateFormat sdf = new SimpleDateFormat();//"yyyy.MM.dd"
				Date date=new Date(l);
				String s=sdf.format(date);
				TextView tv=(TextView)v.findViewById(R.id.date);
				tv.setText(s);

				tv=(TextView)v.findViewById(R.id.from);
				String recipient=c.getString(RECIPIENT_IDS);
				String combinedRecipient="";
				if(!TextUtils.isEmpty(recipient)){
					String[] split=recipient.split(" ");
					for(String str:split){
						try{
							long recipientId=Long.parseLong(str);
							String tempNumber=mCache.get(recipientId);
							String added="";
							if(!TextUtils.isEmpty(combinedRecipient)){
								combinedRecipient+=",";
							}
							
							if(mNameCache.containsKey(tempNumber)){
								added=mNameCache.get(tempNumber);
							}else{
								String name=ContactName.loadNameFromNumber(mContext, tempNumber);
								if(!TextUtils.isEmpty(name)){
									mNameCache.put(tempNumber, name);
									added=name;
								}
							}
							
							combinedRecipient+=added;
						}catch(Exception e){
							
						}
					}
				}
				tv.setText(combinedRecipient);
				
				Drawable d=getResources().getDrawable(R.drawable.ic_contact_picture);
				QuickContactBadge qcb=(QuickContactBadge)v.findViewById(R.id.avatar);
				qcb.setVisibility(View.VISIBLE);
				qcb.setImageDrawable(d);
				
				long read=c.getLong(READ);
				
				/*
				int backgroundId;
				if(read==0){
					backgroundId = R.drawable.conversation_item_background_unread;
				}else{
					backgroundId = R.drawable.conversation_item_background_read;
				}
				v.setBackgroundResource(backgroundId);
				*/
				v.setTag(c.getLong(_ID));
				
				s=extractEncStrFromCursor(c,SNIPPET, SNIPPET_CS);
				tv=(TextView)v.findViewById(R.id.subject);
				tv.setText(s);
				return v;
			}
		};
		
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mCursor.moveToPosition(position);
				Intent intent=new Intent(ConversationList.this, ComposeMessage.class);
				long threadId=(Long)view.getTag();
				long recipient_ids=mCursor.getLong(RECIPIENT_IDS);
				intent.setData(ContentUris.withAppendedId(Threads.CONTENT_URI, threadId));
				intent.putExtra(Sms.ADDRESS, mCache.get(recipient_ids));
				startActivity(intent);
			}
			
		});
		makeCanonicalAddr();
		
		//justTest();
//		String defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);
//		Context context=this;
//		Intent intent = new Intent(Sms.Intents.ACTION_CHANGE_DEFAULT);
//		intent.putExtra(Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp);
//		startActivity(intent);
//		Intent intent = new Intent(Sms.Intents.ACTION_CHANGE_DEFAULT);
//		intent.putExtra(Sms.Intents.EXTRA_PACKAGE_NAME, context.getPackageName());
//		startActivity(intent);
		
	}
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	if(mCursor!=null){
    		mCursor.close();
    	}
    	mCursor=null;
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
        //PendingMessages.ERROR_TYPE,
        Mms.LOCKED,
        Mms.STATUS,
        Mms.TEXT_ONLY
    };    
    
    
	
	private void makeCanonicalAddr(){
		Cursor c=getContentResolver().query(sAllCanonical, null, null, null, null);
		if(c!=null && c.getCount()>0){
			c.moveToFirst();
	       try {
                while (c.moveToNext()) {
                    // TODO: don't hardcode the column indices
                    long id = c.getLong(0);
                    String number = c.getString(1);
                    mCache.put(id, number);
                }
	        } finally {
	            c.close();
	        }			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	
	
	/**
	 * for debug
	 * @param c
	 */
	private void forDebugging(Cursor c){
		for(int i=0;i<c.getCount();i++){
			c.moveToPosition(i);
			String print="";
			for(String  t:PRJ_SIMPLE){
				int columnIndex=c.getColumnIndex(t);
				int type=c.getType(columnIndex);
				switch(type){
				case Cursor.FIELD_TYPE_INTEGER:
					print+=c.getInt(columnIndex);
					break;
				case Cursor.FIELD_TYPE_STRING:
					String temp=c.getString(columnIndex);
					if(t.equals("body")){
						if(temp.length()>20){
							temp=temp.substring(0, 20);
						}
					}
					print+=temp;
					break;
				case Cursor.FIELD_TYPE_FLOAT:
					print+=c.getFloat(columnIndex);
					break;
				default:
					print+="null";
					break;
				}
				print+=",";
			}
			LogUtil.v("Cursor Pos="+i+", content="+print);
			
		}
		LogUtil.d("Telephony.MmsSms.CONTENT_CONVERSATIONS_URI:"+c.getCount());
		LogUtil.d("Projection:"+c.getColumnNames().toString());		
	}
	
	private void justTest(){
		Uri uri=Uri.parse("content://mms-sms/complete-conversations");//ContentUris.withAppendedId(Threads.CONTENT_URI, 0);
		LogUtil.d("uri= "+uri.toString());
		Cursor c=getContentResolver().query(uri, PROJECTION, null, null, null);
		String[] column=c.getColumnNames();
		String col="";
		for(String s:column){
			col+=(s+",");
		}
		//LogUtil.d("col= "+col);
		for(int i=0;i<c.getCount();i++){
			c.moveToPosition(i);
			String print="";
			int count=c.getColumnCount();
			for(int j=0;j<count;j++){
				int type=c.getType(j);
				Object o;
				
				switch(type){
				case Cursor.FIELD_TYPE_STRING:
					o=c.getString(j);
					o=j+":"+"s:"+o+",\t";
					break;
				case Cursor.FIELD_TYPE_FLOAT:
					o=c.getFloat(j);
					o=j+":"+"f:"+o+",\t";
					break;
				case Cursor.FIELD_TYPE_INTEGER:
					o=c.getInt(j);
					o=j+":"+"i:"+o+",\t";
					break;
				default:
				case Cursor.FIELD_TYPE_NULL:
					o="";
					break;
				}
				print+=o;
			}
			LogUtil.d(print);
		}
	}	
}
