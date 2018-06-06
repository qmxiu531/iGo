package com.gionee.autotest.traversal.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CheckBox;

import com.gionee.autotest.traversal.R;
import com.gionee.autotest.traversal.adapter.OutlineResultAdapter;
import com.gionee.autotest.traversal.adapter.RecyclerViewScrollListener;
import com.gionee.autotest.traversal.common.report.ReportDetail;
import com.gionee.autotest.traversal.common.report.ReportSummary;
import com.gionee.autotest.traversal.common.util.Constant;
import com.gionee.autotest.traversal.model.Item;
import com.gionee.autotest.traversal.model.OutlineResult;
import com.gionee.autotest.traversal.widget.EmptyRecyclerView;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements OutlineResultAdapter.OnItemClickListener{

    @Bind(R.id.list_recycler_view)
    EmptyRecyclerView mRecyclerView ;

    @Bind(R.id.list_empty_view)
    View emptyView ;

    OutlineResultAdapter mAdapter ;

    Handler mHandler;

    List<OutlineResult> mItems;

    List<Item> folders ;

    Gson gson ;

    boolean noMoreDataTag = false ;

    boolean addHead = false ;

    @Override
    protected int layoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected int menuResId() {
        return R.menu.menu_main;
    }

    @Override
    protected boolean isDisplayHomeUpEnabled() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        mHandler = new Handler();
        gson = new Gson() ;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(getApplicationContext(), R.anim.layout_animation_fall_down);
        mRecyclerView.setLayoutAnimation(controller);
        // Fetch the empty view from the layout and set it on
        // the new recycler view
        mRecyclerView.setEmptyView(emptyView);
        setupAdapter() ;
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerViewScrollListener() {
            @Override
            public void onScrollUp() {}

            @Override
            public void onScrollDown() {}

            @Override
            public void onLoadMore() {
                if (!noMoreDataTag){
                    addHead = false ;
                    loadMoreData();
                }
            }
        });
        //show notice dialog
        showNoticeDialog() ;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(Constant.TAG, "enter onResume function") ;
        addHead = true ;
        // Load data after delay
        mHandler.postDelayed(fetchRunnable, 500);
    }

    private void showNoticeDialog() {
        SharedPreferences sp = getSharedPreferences(Constant.SHARED_PERF, Context.MODE_PRIVATE) ;
        final SharedPreferences.Editor editor = sp.edit() ;
        boolean show = sp.getBoolean(Constant.SHOW_NOTICE, true) ;
        if (show){
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.layout_notice, null);
            final CheckBox notShowAgain = (CheckBox) dialogView.findViewById(R.id.not_show_again);
            dialogBuilder.setIcon(R.mipmap.ic_launcher) ;
            dialogBuilder.setTitle(R.string.notice_title) ;
            dialogBuilder.setView(dialogView);
            dialogBuilder.setPositiveButton(R.string.read, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (notShowAgain.isChecked()){
                        //set SHOW_NOTICE to false
                        editor.putBoolean(Constant.SHOW_NOTICE, false).apply() ;
                    }
                }
            });
            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    private void loadMoreData() {
        Log.i(Constant.TAG, "enter load more data function...") ;
        mAdapter.showLoading(true);
        mAdapter.notifyDataSetChanged();
        // Load data after delay
        mHandler.postDelayed(fetchRunnable, 500);
    }

    /**
     * time consume process all go here
     */
    Runnable fetchRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(Constant.TAG, "enter fetch runnable...") ;
            if (folders == null){
                Log.i(Constant.TAG, "folders is null, create it.") ;
                //will init first time
                folders = new ArrayList<>() ;
                loadAllFolders() ;
            }else{
                Log.i(Constant.TAG, "folders is not empty, update it") ;
                updateAllFolders();
            }
            //no data or data size lower than ten should set noMoreDataTag to true
            if (folders == null || folders.size() < 10){
                Log.i(Constant.TAG, "folder is null or folder size is less than ten, set no more data to true") ;
                noMoreDataTag = true ;
            }
            //load ten OutlineResult if exist
            List<OutlineResult> newItems = fetchDataFromSdcard();
            //aways set this false first
            if (newItems == null){
                Log.i(Constant.TAG, "fetch data is empty, set no more data to true") ;
                noMoreDataTag = true ;
            }else{
                Log.i(Constant.TAG, "fetch data success, notify adapter.") ;
                if (addHead){
                    mItems.addAll(0, newItems);
                }else{
                    mItems.addAll(newItems);
                }

                mAdapter.setItems(mItems);
                mAdapter.notifyDataSetChanged();
            }
            mAdapter.showLoading(false);
            mAdapter.notifyDataSetChanged();
        }
    } ;

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(Constant.TAG, "enter onStop function.") ;
        //aways do this, may leak memory!!!
        mHandler.removeCallbacks(fetchRunnable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fab)
    public void onFABClicked(View view){
        //startActivity(new Intent(MainActivity.this, CreateTimeActivity.class));
        startActivity(new Intent(MainActivity.this, CreateNewActivity.class));
    }

    private void setupAdapter() {
        Log.i(Constant.TAG, "enter setupAdapter function.") ;
        mAdapter = new OutlineResultAdapter(this, this);
        mAdapter.setHasStableIds(true);
        mItems = new ArrayList<>() ;
        mAdapter.setItems(mItems);
    }

    private List<OutlineResult> fetchDataFromSdcard(){
        Log.i(Constant.TAG, "enter fetchDataFromSdcard function.") ;
        if (folders == null || folders.size() == 0) return null ;
        Iterator<Item> iterator = folders.iterator() ;
        List<OutlineResult> outlines = null;
        int count = 0 ;
        while(iterator.hasNext()){
            Item item = iterator.next() ;
            if (!item.isVisited()){
                if (count >= 10) {
                    Log.i(Constant.TAG, "reached max load count per time, exit") ;
                    break;
                }
                item.setVisited(true);
                String timestamp = item.getTimestamp();
                Log.i(Constant.TAG, "current parsing time stamp : " + timestamp) ;
                OutlineResult result = parseJsonFromSdcard(timestamp) ;
                if (result == null){
                    Log.i(Constant.TAG, "parsing current timestamp null or exception happened.") ;
                    continue;
                }
                if (outlines == null){
                    outlines = new ArrayList<>() ;
                }
                outlines.add(result) ;
                count ++ ;
            }
        }
        return outlines ;
    }

    private OutlineResult parseJsonFromSdcard(String timestamp){
        try {
            String cTimestamp = timestamp.replace(" ", "_") ;
            File json = new File(Environment.getExternalStorageDirectory(), Constant.ROOT_NAME
                    + File.separator + cTimestamp + File.separator + Constant.DIR_REPORT
                    + File.separator + Constant.DIR_REPORT_JSON) ;
            if (!json.exists() || !json.isFile()) return null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(json), "UTF-8")) ;
            ReportSummary reportSummary = gson.fromJson(reader, ReportSummary.class) ;
            ReportDetail detail = reportSummary.detail ;
            if (detail == null) return null;
            ReportDetail.SummaryInfo summary = detail.summaryInfo ;
            ReportDetail.ExceptionDetail exception = detail.exceptionInfo ;
            if (summary == null) return null ;
            String exText = getString(R.string.no_exception) ;
            if (exception != null &&
                    exception.exceptionInfos != null && exception.exceptionInfos.size() != 0){
                exText = exception.exceptionInfos.size() + "" ;
            }
            return new OutlineResult(timestamp, summary.packageName, reportSummary.test_result,
                    summary.testDuration, summary.testConverage, exText) ;
        }catch (Exception e){
            Log.i(Constant.TAG, "MainActivity convert file to json failure." + e.getMessage()) ;
            e.printStackTrace();
        }
        return null ;
    }

    private String[] fetchAllDirectories(){
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return null;
        }
        final DateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.US);
        File root = new File(Environment.getExternalStorageDirectory() + File.separator + Constant.ROOT_NAME) ;
        return root.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
/*                Log.i(Constant.TAG, "dir : " + dir.getAbsolutePath()) ;
                Log.i(Constant.TAG, "name : " + name) ;*/
                return isValidDate(format, name);
            }
        });
    }

    private void updateAllFolders(){
        String[] dirs = fetchAllDirectories() ;
        if (dirs != null && dirs.length > 0){
            List<String> dirList = new ArrayList<>(Arrays.asList(dirs)) ;
            Collections.sort(dirList);
/*            for (Item item : folders){
                Log.i(Constant.TAG, "exist directory : " + item.getTimestamp()) ;
            }*/
            for (String dir : dirList){
                Item curITem = new Item(dir.replace("_", " "));
                if(!folders.contains(curITem)){
                    Log.i(Constant.TAG, "add new directory : " + dir) ;
                    folders.add(0, curITem) ;
                }
            }
        }
    }

    /**
     * load all result directories
     */
    private void loadAllFolders(){
        String[] dirs = fetchAllDirectories() ;
        if (dirs != null && dirs.length > 0){
            List<String> dirList = new ArrayList<>(Arrays.asList(dirs)) ;
            Collections.sort(dirList);
            Collections.reverse(dirList);
            for (String dir : dirList){
                folders.add(new Item(dir.replace("_", " "))) ;
            }
        }
    }

    /**
     * check current directory is result directory or not
     */
    public boolean isValidDate(DateFormat format, String name) {
        try{
            format.parse(name);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    @Override
    public void onItemClick(OutlineResult item) {
        String timestamp = item.getTimestamp().replace(" ", "_") ;
        Log.i(Constant.TAG, "current clicked outline timestamp : " + timestamp) ;
        Intent intent = new Intent(MainActivity.this, ResultActivity.class) ;
        intent.putExtra(Constant.EXTRA_TIMESTAMP, timestamp) ;
//                intent.putExtra(Constant.EXTRA_TIMESTAMP, "2017-09-13_18:01:02") ;
//                intent.putExtra(Constant.EXTRA_TIMESTAMP, "2017-09-13_18:29:33") ;
        startActivity(intent);
    }
}
