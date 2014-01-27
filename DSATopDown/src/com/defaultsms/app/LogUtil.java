/**
 * 
 */
package com.defaultsms.app;

import android.util.Log;

/**
 * Util for logging
 * @author wonjai
 *
 */
public class LogUtil {
	
	private LogUtil() {
		
	}
	
	public static void v(String msg) {
		Log.v(getTag(), msg);
	}
	public static void d(String msg) {
		Log.d(getTag(), msg);
	}
	public static void e(String msg) {
		Log.e(getTag(), msg);
	}
	public static void w(String msg) {
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
