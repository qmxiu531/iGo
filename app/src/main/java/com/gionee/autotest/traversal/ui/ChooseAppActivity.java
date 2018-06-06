package com.gionee.autotest.traversal.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.gionee.autotest.traversal.R;
import com.gionee.autotest.traversal.adapter.ApplicationRecyclerAdapter;
import com.gionee.autotest.traversal.common.model.AppEntry;
import com.gionee.autotest.traversal.task.FetchAllAppsTask;
import com.gionee.autotest.traversal.common.util.Constant;
import com.gionee.autotest.traversal.widget.EmptyRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * Created by viking on 9/7/17.
 *
 * list all launcher application and single choose
 */

public class ChooseAppActivity extends BaseActivity{

    @Bind(R.id.list_recycler_view)
    EmptyRecyclerView recyclerView ;

    FetchAllAppsTask fetchTask ;

    PackageManager mPm ;

    List<AppEntry> apps ;

    ApplicationRecyclerAdapter mAdapter ;

    @Override
    protected int layoutResId() {
        return R.layout.layout_choose_application;
    }

    @Override
    protected int menuResId() {
        return 0;
    }

    @Bind(R.id.list_empty_view)
    View emptyView ;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recyclerView.setEmptyView(emptyView);
        apps = new ArrayList<>() ;
        mAdapter = new ApplicationRecyclerAdapter(getApplicationContext(), apps, R.layout.layout_choose_app_item) ;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
/*        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(getApplicationContext(), R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(controller);*/
        recyclerView.setAdapter(mAdapter);

        mPm = getPackageManager() ;
        fetchTask = new FetchAllAppsTask(getApplicationContext()){
            @Override
            protected void onPostExecute(final List<AppEntry> appEntries) {
                super.onPostExecute(appEntries);
                Log.i(Constant.TAG, "enter onPostExecute : ") ;
                if (appEntries != null && appEntries.size() > 0){
                    Log.i(Constant.TAG, "enter onPostExecute in ") ;
                    apps = appEntries ;
                    Log.i(Constant.TAG,"apps size="+apps.size());
                    mAdapter = new ApplicationRecyclerAdapter(getApplicationContext(), apps, R.layout.layout_choose_app_item) ;
                    mAdapter.setOnItemClickListener(new ApplicationRecyclerAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View itemView, int position) {
                            //return it to choose activity
                            sendChooseResult(apps.get(position)) ;
                        }
                    });
                    recyclerView.setAdapter(mAdapter);
                    Log.i(Constant.TAG,"apps size1="+apps.size());
                }
                Log.i(Constant.TAG, "enter onPostExecute end ") ;
            }
        } ;
        fetchTask.execute() ;
    }

    private void sendChooseResult(AppEntry entry){
        Intent result = new Intent() ;
        result.putExtra(Constant.EXTRA_CHOOSE_APP_NAME, entry.label) ;
        result.putExtra(Constant.EXTRA_CHOOSE_APP_PKG, entry.packageName) ;
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fetchTask != null){
            fetchTask.cancel(true) ;
        }
    }
}
