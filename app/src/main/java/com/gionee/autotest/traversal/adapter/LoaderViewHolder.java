package com.gionee.autotest.traversal.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;


import com.gionee.autotest.traversal.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoaderViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.progressbar)
    ProgressBar mProgressBar;

    public LoaderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}