/**
 * 
 */
package com.defaultsms.app;

import android.util.Log;

/**
 * Log Util
 * @author chanyhan
 *
 */
public class LogUtil {
	private static boolean DEBUG=true;
	
	public static void v(String tag,String msg) {
		if(DEBUG)
			Log.v(tag, msg);
	}
	public static void d(String tag,String msg) {
		if(DEBUG)
			Log.d(tag, msg);
	}
	public static void e(String tag,String msg) {
		if(DEBUG)
			Log.e(tag, msg);
	}
	public static void w(String tag,String msg) {
		if(DEBUG)
			Log.w(tag, msg);
	}
	
	public static void v(String msg) {
		if(DEBUG)
			Log.v(getTag(), msg);
	}
	public static void d(String msg) {
		if(DEBUG)
			Log.d(getTag(), msg);
	}
	public static void e(String msg) {
		if(DEBUG)
			Log.e(getTag(), msg);
	}
	public static void w(String msg) {
		if(DEBUG)
			Log.w(getTag(), msg);
	}	
	
	private static String getTag(){
		String tag="";
		StackTraceElement last=null;
		final StackTraceElement[] stes = Thread.currentThread().getStackTrace();
		
		int indexOfGetMethodName = 2;
		for (int i = 0; i < stes.length; i++) {
			StackTraceElement ste = stes[i];
			if (ste.getMethodName().equals("getMethodName")) {
				indexOfGetMethodName = i;
			}
		}
		
		if (indexOfGetMethodName + 1 + 1 < stes.length) {
			last=stes[indexOfGetMethodName + 1 + 1];
		} else {
			last=stes[stes.length - 1];
		}
		if(last!=null){
			tag=last.getFileName()+":"+last.getMethodName()+"():"+last.getLineNumber();
		}
		return tag;
	}
	

	
}
