package com.example.smsmmstest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

import com.example.smsmmstest.R;
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
		AQUtility.debug("count="+c.getCount()+"column="+s.toString());
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
		mAdapter=new SimpleCursorAdapter(this, R.layout.listview_item,
				c,
				//new String[]{"date","address"},
				new String[]{"date","message_count", "recipient_ids"},
				new int[]{R.id.text1, R.id.text2, R.id.text3},//
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER){
			@Override
			public View getView(int position, View convertView, ViewGroup parent){
				View v=super.getView(position, convertView, parent);
				Cursor c=getCursor();
				c.moveToPosition(position);
				//long l=c.getLong(2);
				long l=c.getLong(1);
				//Calendar cal=Calendar.getInstance(TimeZone.getDefault());
				//cal.setTimeInMillis(l);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
				Date date=new Date(l);
				String s=sdf.format(date);
				TextView tv=(TextView)v.findViewById(R.id.text1);
				tv.setText(s);

				return v;
			}
		};
		
		mListView.setAdapter(mAdapter);
		
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
