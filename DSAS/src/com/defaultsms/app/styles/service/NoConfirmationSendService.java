/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.defaultsms.app.styles.service;

import com.defaultsms.app.styles.mms.transaction.SmsSingleRecipientSender;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

//import com.android.mms.data.Conversation;
//import com.android.mms.transaction.SmsMessageSender;

/**
 * Respond to a special intent and send an SMS message without the user's intervention.
 */
public class NoConfirmationSendService extends IntentService {
    public NoConfirmationSendService() {
        // Class name will be the thread name.
        super(NoConfirmationSendService.class.getName());

        // Intent should be redelivered if the process gets killed before completing the job.
        setIntentRedelivery(true);
    }

    private static final String TAG = "Mms/NoConfirmationSendService";

    @Override
    protected void onHandleIntent(Intent intent) {

        String action = intent.getAction();
        if (!TelephonyManager.ACTION_RESPOND_VIA_MESSAGE.equals(action)) {
            return;
        }
        
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }

        String message = extras.getString(Intent.EXTRA_TEXT);

        Uri intentUri = intent.getData();
        String recipients = getRecipients(intentUri);

        if (TextUtils.isEmpty(recipients)) {
            return;
        }
        /*
        if (extras.getBoolean("showUI", false)) {
            intent.setClassName(this, "com.android.mms.ui.ComposeMessageActivityNoLockScreen");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else*/ 
        {
            if (TextUtils.isEmpty(message)) {
                return;
            }
            String[] dests = TextUtils.split(recipients, ";");

            // Using invalid threadId 0 here. When the message is inserted into the db, the
            // provider looks up the threadId based on the recipient(s).
            long threadId = 0;
         // temporary comment
            //SmsSingleRecipientSender smsMessageSender = new SmsSingleRecipientSender(this, dests, message, threadId);
            try {
                // This call simply puts the message on a queue and sends a broadcast to start
                // a service to send the message. In queing up the message, however, it does
                // insert the message into the DB.
            	// temporary comment
                //smsMessageSender.sendMessage(threadId);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send SMS message, threadId=" + threadId, e);
            }
        }
    }
    
    public static String getRecipients(Uri uri) {
        String base = uri.getSchemeSpecificPart();
        int pos = base.indexOf('?');
        return (pos == -1) ? base : base.substring(0, pos);
    }    
    
}
