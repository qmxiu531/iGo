package com.gionee.autotest.traversal.receiver;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;

import com.gionee.autotest.traversal.R;
import com.gionee.autotest.traversal.common.util.Constant;
import com.gionee.autotest.traversal.service.FireUiAutomatorTestService;
import com.gionee.autotest.traversal.ui.ResultActivity;

/**
 * Created by viking on 9/8/17.
 *
 * monitor test finish state
 *
 * why we need this? because we want to display a system dialog first, then start result activity
 *
 * just given user a choose option!!!
 */

public class TestFinishReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Constant.ACTION_TEST_FINSIHED.equals(intent.getAction())){
            Log.i(Constant.TAG, "received test finished broadcast...") ;
            String timestamp = intent.getStringExtra(Constant.EXTRA_TIMESTAMP) ;
            Log.i(Constant.TAG, "time stamp is : " + timestamp) ;
            //stop service first
            Intent stopService = new Intent(context, FireUiAutomatorTestService.class) ;
            context.stopService(stopService) ;
            //show a system dialog
            showFinishedDialog(context, timestamp);
        }
    }

    private void showFinishedDialog(final Context context, final String timestamp){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(R.string.test_finished_title)
                .setMessage(R.string.test_finished_desc)
                .setPositiveButton(R.string.test_finished_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent iResult = new Intent(context, ResultActivity.class) ;
                        iResult.putExtra(Constant.EXTRA_TIMESTAMP, timestamp) ;
                        iResult.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
                        context.startActivity(iResult);
                    }
                })
                .setNegativeButton(R.string.test_finished_cancel,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing, dialog will dismiss automatic
                    }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        alertDialog.show();
    }
}
