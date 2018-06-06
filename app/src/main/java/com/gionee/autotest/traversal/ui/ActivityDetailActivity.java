package com.gionee.autotest.traversal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gionee.autotest.traversal.R;
import com.gionee.autotest.traversal.common.report.ReportDetail;
import com.gionee.autotest.traversal.common.util.Constant;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by viking on 9/13/17.
 *
 * show detail activities information
 */

public class ActivityDetailActivity extends BaseActivity{

    @Bind(R.id.recycler_view)
    RecyclerView mRecyclerView ;

    @Override
    protected int layoutResId() {
        return R.layout.layout_detail_activities;
    }

    @Override
    protected int menuResId() {
        return 0;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = getIntent() ;
        if (data == null || data.getSerializableExtra("ainfo") == null){
            Log.i(Constant.TAG, "activities is empty, finish it") ;
            finish();
            return ;
        }

        ReportDetail.ActivityDetail detail = (ReportDetail.ActivityDetail) data.getSerializableExtra("ainfo");
        List<ReportDetail.ActivityItem> nActivities = detail.nActivities ;
        List<ReportDetail.ActivityItem> tActivities = detail.tActivities ;
        List<ActivityInfo> items = new ArrayList<>() ;
        if (tActivities != null && tActivities.size() > 0){
            for (ReportDetail.ActivityItem item : tActivities){
                items.add(new ActivityInfo(item.activityName, item.activityLabel, true)) ;
            }
        }
        if (nActivities != null && nActivities.size() > 0){
            for (ReportDetail.ActivityItem item : nActivities){
                items.add(new ActivityInfo(item.activityName, item.activityLabel, false)) ;
            }
        }

        ActivityAdapter mAdapter = new ActivityAdapter(items);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
/*        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(getApplicationContext(), R.anim.layout_animation_fall_down);
        mRecyclerView.setLayoutAnimation(controller);*/
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.MyViewHolder> {

        private List<ActivityInfo> aList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView info;
            public TextView image ;
            public LinearLayout container ;

            public MyViewHolder(View view) {
                super(view);
                info = (TextView) view.findViewById(R.id.activity_info);
                image = (TextView) view.findViewById(R.id.activity_image);
                container = (LinearLayout) view.findViewById(R.id.layout_container);
            }
        }

        public ActivityAdapter(List<ActivityInfo> aList) {
            this.aList = aList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_detail_activities_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            ActivityInfo item = aList.get(position);
            int backgroundResource = R.drawable.test_fail_gradient;
            int iconResource = R.drawable.fs_error;
            if (item.isVisited()){
                backgroundResource = R.drawable.test_pass_gradient;
                iconResource = R.drawable.fs_good;
            }
            StringBuilder sb = new StringBuilder() ;
            sb.append(item.getName()) ;
            sb.append("(") ;
            sb.append(item.getLabel()) ;
            sb.append(")") ;
            holder.container.setBackgroundResource(backgroundResource);
            holder.info.setText(sb.toString());
            holder.image.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconResource, 0);
        }

        @Override
        public int getItemCount() {
            return aList.size();
        }
    }

    public class ActivityInfo{

        private String name ;

        private String label ;

        private boolean visited ;

        public ActivityInfo(String name, String label, boolean visited) {
            this.name = name;
            this.label = label;
            this.visited = visited;
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }

        public boolean isVisited() {
            return visited;
        }
    }
}
