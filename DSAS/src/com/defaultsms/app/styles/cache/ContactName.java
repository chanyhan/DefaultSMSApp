package com.defaultsms.app.styles.cache;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

public class ContactName{
	
	private final Map<String, String>mCache = new HashMap<String, String>();
	
	public String getNameFromNumber(Context con, String number){
		if(con==null){
			return null;
		}
		
		if(TextUtils.isEmpty(number)){
			return "";
		}
		
		if(mCache.containsKey(number)){
			return mCache.get(number);
		}
		
		String name=loadNameFromNumber(con, number);
		if(TextUtils.isEmpty(name)){
			return "";
		}
		mCache.put(number, name);
		return name;
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
    
    public static String loadNameFromNumber(Context c, String number) {
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
            Cursor cursor = c.getContentResolver().query(Data.CONTENT_URI, CALLER_ID_PROJECTION, selection, args, null);
            if(cursor!=null && cursor.getCount()>0){
            	cursor.moveToFirst();
            	number=cursor.getString(CONTACT_NAME_COLUMN);
            	cursor.close();
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
}