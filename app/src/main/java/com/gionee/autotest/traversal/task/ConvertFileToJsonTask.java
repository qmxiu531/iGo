package com.gionee.autotest.traversal.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gionee.autotest.traversal.R;
import com.gionee.autotest.traversal.common.report.ReportSummary;
import com.gionee.autotest.traversal.common.util.Constant;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

/**
 * Created by viking on 9/11/17.
 *
 * convert file to json task
 */

public class ConvertFileToJsonTask extends AsyncTask<Void, Void, ReportSummary> {

    private WeakReference<Context> mContext ;
    private ProgressDialog dialog ;
    private File json ;

    public ConvertFileToJsonTask(Context mContext, File json){
        this.mContext = new WeakReference<>(mContext) ;
        this.json = json ;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mContext.get() != null){
            dialog = ProgressDialog.show(mContext.get(), mContext.get().getString(R.string.convert_title),
                    mContext.get().getString(R.string.convert_message)) ;
        }
    }

    @Override
    protected ReportSummary doInBackground(Void... voids) {
        Gson gson = new Gson() ;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(json), "UTF-8")) ;
            return gson.fromJson(reader, ReportSummary.class) ;
        }catch (IOException e){
            Log.i(Constant.TAG, "convert file to json failure." + e.getMessage()) ;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(ReportSummary reportSummary) {
        super.onPostExecute(reportSummary);
        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }
}
