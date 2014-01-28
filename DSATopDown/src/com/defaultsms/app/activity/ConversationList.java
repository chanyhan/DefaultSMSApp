package com.defaultsms.app.activity;

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
import android.provider.Telephony;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Threads;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.defaultsms.app.LogUtil;
import com.defaultsms.app.R;
import com.defaultsms.app.R.id;
import com.defaultsms.app.R.layout;
import com.defaultsms.app.R.menu;
//import android.provider.;
public class ConversationList extends Activity {

	ListView mListView;
	SimpleCursorAdapter mAdapter;
	
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

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AQuery aq=new AQuery(this);
		AQUtility.setDebug(true);
		
		Uri uri=Threads.CONTENT_URI;
		uri=uri.buildUpon().appendQueryParameter("simple", "true").build();
		Cursor c=getContentResolver().query(uri, null, null, null, "date DESC");

		forDebugging(c);
		
		mListView=(ListView)findViewById(R.id.listview);
		mAdapter=new SimpleCursorAdapter(this, R.layout.conversation_list_item,
				c,
				new String[]{"date", "recipient_ids", "snippet"},
				new int[]{R.id.date,  R.id.from, R.id.subject},
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER){
			@Override
			public View getView(int position, View convertView, ViewGroup parent){
				View v=super.getView(position, convertView, parent);
				Cursor c=getCursor();
				c.moveToPosition(position);
				long l=c.getLong(DATE);
				SimpleDateFormat sdf = new SimpleDateFormat();//"yyyy.MM.dd"
				Date date=new Date(l);
				String s=sdf.format(date);
				TextView tv=(TextView)v.findViewById(R.id.date);
				tv.setText(s);

				tv=(TextView)v.findViewById(R.id.from);
				l=c.getLong(RECIPIENT_IDS);
				s=mCache.get(l);
				tv.setText(s);
				
				Drawable d=getResources().getDrawable(R.drawable.ic_contact_picture);
				QuickContactBadge qcb=(QuickContactBadge)v.findViewById(R.id.avatar);
				qcb.setVisibility(View.VISIBLE);
				qcb.setImageDrawable(d);
				
				long read=c.getLong(READ);
				
				int backgroundId;
				if(read==0){
					backgroundId = R.drawable.conversation_item_background_unread;
				}else{
					backgroundId = R.drawable.conversation_item_background_read;
				}
				v.setBackgroundResource(backgroundId);
				v.setTag(c.getLong(_ID));
				return v;
			}
		};
		
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent=new Intent(ConversationList.this, ComposeMessage.class);
				long threadId=(Long)view.getTag();
				intent.setData(ContentUris.withAppendedId(Threads.CONTENT_URI, threadId));
				startActivity(intent);
			}
			
		});
		makeCanonicalAddr();
//		String defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);
//		Context context=this;
//		Intent intent = new Intent(Sms.Intents.ACTION_CHANGE_DEFAULT);
//		intent.putExtra(Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp);
//		startActivity(intent);
//		Intent intent = new Intent(Sms.Intents.ACTION_CHANGE_DEFAULT);
//		intent.putExtra(Sms.Intents.EXTRA_PACKAGE_NAME, context.getPackageName());
//		startActivity(intent);
		
	}
	
	private void makeCanonicalAddr(){
		Cursor c=getContentResolver().query(sAllCanonical, null, null, null, null);
		if(c!=null && c.getCount()>0){
			String[] s=c.getColumnNames();
			String printL="";
			for(String t:s){
				printL+=(t+",");
			}
			AQUtility.debug("printL="+printL);			
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
}
