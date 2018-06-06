package com.gionee.autotest.traversal.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gionee.autotest.traversal.R;
import com.gionee.autotest.traversal.model.OutlineResult;
import com.gionee.autotest.traversal.widget.MarqueeTextView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by viking on 9/14/17.
 *
 * main activity adapter
 */

public class OutlineResultAdapter extends FooterLoaderAdapter<OutlineResult> {

    private Context mContext ;
    private int colorRed ;
    private int colorGreen ;
    public interface OnItemClickListener {
        void onItemClick(OutlineResult item);
    }

    private final OnItemClickListener listener;


    public OutlineResultAdapter(Context context, OnItemClickListener listener) {
        super(context);
        this.mContext = context ;
        colorRed = mContext.getResources().getColor(R.color.colorRed) ;
        colorGreen = mContext.getResources().getColor(R.color.colorGreen) ;
        this.listener = listener ;
    }

    @Override
    public long getYourItemId(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder getYourItemViewHolder(ViewGroup parent) {
        return new OutlineViewHolder(mInflater.inflate(R.layout.layout_outline_item, parent, false));
    }

    @Override
    public void bindYourViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OutlineViewHolder) {
            OutlineViewHolder viewHolder = (OutlineViewHolder)holder;
            final OutlineResult item = mItems.get(position) ;
            viewHolder.mTextStamp.setText(item.getTimestamp());
            viewHolder.mTextPkg.setText(item.getPkg());
            viewHolder.mTextResult.setText(item.getTestresult());
            if (item.getTestresult().equals("不通过")){
                viewHolder.mTextResult.setTextColor(colorRed);
            }else{
                viewHolder.mTextResult.setTextColor(colorGreen);
            }
            viewHolder.mTextDuration.setText(item.getTestDuration());
            viewHolder.mTextCoverage.setText(item.getTestCoverage());
            viewHolder.mTextException.setText(item.getTestException());
            if (item.getTestException().equals("无")){
                viewHolder.mTextException.setTextColor(colorGreen);
            }else{
                viewHolder.mTextException.setTextColor(colorRed);
            }
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    public class OutlineViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.outline_timestamp)
        TextView mTextStamp;

        @Bind(R.id.outline_package)
        MarqueeTextView mTextPkg ;

        @Bind(R.id.outline_result)
        TextView mTextResult ;

        @Bind(R.id.outline_duration)
        TextView mTextDuration ;

        @Bind(R.id.outline_coverage)
        TextView mTextCoverage ;

        @Bind(R.id.outline_exception)
        TextView mTextException ;

        public OutlineViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
