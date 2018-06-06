package com.gionee.autotest.traversal.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gionee.autotest.traversal.R;
import com.gionee.autotest.traversal.common.model.AppEntry;
import com.gionee.autotest.traversal.widget.MarqueeTextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ApplicationRecyclerAdapter extends RecyclerView.Adapter<ApplicationRecyclerAdapter.ViewHolder> {

    private List<AppEntry> items;
    private int itemLayout;

    // Define listener member variable
    private OnItemClickListener listener;

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private Context mContext ;

    private PackageManager mPm ;

    public ApplicationRecyclerAdapter(Context mContext, List<AppEntry> items, int itemLayout) {
        this.mContext = mContext ;
        this.mPm = this.mContext.getPackageManager() ;
        this.items = items;
        this.itemLayout = itemLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppEntry item = items.get(position);
        holder.app_icon.setImageDrawable(item.info.loadIcon(mPm));
//        holder.app_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.launcher_icon));
        holder.app_name.setText(item.label);
        holder.app_pkg.setText(item.packageName);
        holder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.app_icon)
        ImageView app_icon;
        @Bind(R.id.app_name)
        MarqueeTextView app_name;
        @Bind(R.id.app_pkg)
        TextView app_pkg;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            // Setup the click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });
        }
    }
}