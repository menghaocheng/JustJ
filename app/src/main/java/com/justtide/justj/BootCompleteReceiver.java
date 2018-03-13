package com.justtide.justj;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
         if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            Toast.makeText(context, "Boot Complete", Toast.LENGTH_LONG).show();
            Intent bootSerIntent = new Intent(context, JustjService.class);
            bootSerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            bootSerIntent.setAction("intent.com.justtide.action.START_JUSTJ");
            context.startService(bootSerIntent);
        }
    }
}
