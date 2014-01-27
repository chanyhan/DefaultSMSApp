package com.defaultsms.app;

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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.defaultsms.app.R;
//import android.provider.;
public class MainActivity extends Activity {

	ListView mListView;
	SimpleCursorAdapter mAdapter;
	
	String[] PROJECTION=new String[]{
			"_id",
			"body",
			"date",
			"thread_id",
			"address",
			"m_type",
			};
	
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
	//"msg_box"
	//"person",
	//"subject",
	//"m_id",
	/*
	 * 
		body,person,text_only,sub,subject,retr_st,type,date,ct_cls,sub_cs,_id,read,ct_l,tr_id,
		st,msg_box,thread_id,reply_path_present,m_cls,read_status,ct_t,status,retr_txt_cs,d_rpt,
		error_code,m_id,date_sent,m_type,v,exp,pri,service_center,address,rr,rpt_a,resp_txt,locked,resp_st,m_size,

		_id,date,message_count,recipient_ids,snippet,snippet_cs,read,type,error,has_attachment,
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AQuery aq=new AQuery(this);
		AQUtility.setDebug(true);
		
		Uri uri=Threads.CONTENT_URI;
		uri=uri.buildUpon().appendQueryParameter("simple", "true").build();
		Cursor c=getContentResolver().query(uri, null, null, null, "date DESC");
		/*
		MatrixCursor m=new MatrixCursor(PROJECTION);
		for(int i=0;i<c.getCount();i++){
			c.moveToPosition(i);
			RowBuilder r=m.newRow();
			for(String s:PRJECTION){
				r.add(c.g)
			}
		}
		*/
		String[] s=c.getColumnNames();
		String printL="";
		for(String t:s){
			printL+=(t+",");
		}
		AQUtility.debug("printL="+printL);
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
		
		mListView=(ListView)findViewById(R.id.listview);
		mAdapter=new SimpleCursorAdapter(this, R.layout.conversation_list_item,
				c,
				//new String[]{"date","address"},
				new String[]{"date", "recipient_ids", "snippet"},//"message_count",
				new int[]{R.id.date,  R.id.from, R.id.subject},//R.id.text2,
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER){
			@Override
			public View getView(int position, View convertView, ViewGroup parent){
				View v=super.getView(position, convertView, parent);
				Cursor c=getCursor();
				c.moveToPosition(position);
				long l=c.getLong(1);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
				Date date=new Date(l);
				String s=sdf.format(date);
				TextView tv=(TextView)v.findViewById(R.id.date);
				tv.setText(s);

				tv=(TextView)v.findViewById(R.id.from);
				l=c.getLong(RECIPIENT_IDS);
				s=mCache.get(l);
				tv.setText(s);
				return v;
			}
		};
		
		mListView.setAdapter(mAdapter);
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

}
