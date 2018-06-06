package com.gionee.autotest.traversal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gionee.autotest.traversal.R;
import com.gionee.autotest.traversal.common.report.ExceptionInfo;
import com.gionee.autotest.traversal.common.report.ReportDetail;
import com.gionee.autotest.traversal.common.util.Constant;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by viking on 9/14/17.
 *
 * show exception listview
 */

public class ExceptionListActivity extends AppCompatActivity {

    @Bind(R.id.recycler_view)
    RecyclerView mRecyclerView ;

    private ExceptionAdapter mAdapter ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_exception_list);
        if(getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ButterKnife.bind(this);
        Intent data = getIntent() ;
        if (data == null || data.getSerializableExtra("exception") == null){
            Log.i(Constant.TAG, "exception detail object is empty, finish it.") ;
            finish();
            return ;
        }
        ReportDetail.ExceptionDetail exceptionDetail = (ReportDetail.ExceptionDetail)
                data.getSerializableExtra("exception");
        if (exceptionDetail.exceptionInfos == null || exceptionDetail.exceptionInfos.size() == 0){
            Log.i(Constant.TAG, "exception list is empty, finish it.") ;
            finish();
//            return ;
        }
/*        List<ExceptionInfo> exceptions = exceptionDetail.exceptionInfos ;
        mAdapter = new ExceptionAdapter(exceptions);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(getApplicationContext(), R.anim.layout_animation_fall_down);
        mRecyclerView.setLayoutAnimation(controller);
        mRecyclerView.setAdapter(mAdapter);*/
    }

    public class ExceptionAdapter extends RecyclerView.Adapter<ExceptionAdapter.MyViewHolder> {

        private List<ExceptionInfo> aList;

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

        public ExceptionAdapter(List<ExceptionInfo> aList) {
            this.aList = aList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_exception_overview, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            ExceptionInfo item = aList.get(position);

        }

        @Override
        public int getItemCount() {
            return aList.size();
        }
    }
}
