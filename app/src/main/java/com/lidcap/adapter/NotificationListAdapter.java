package com.lidcap.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lidcap.R;
import com.lidcap.model.output.NotificationDetailsEntity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.Holder> {

    private ArrayList<NotificationDetailsEntity> mIssueListArrList;

    public NotificationListAdapter(ArrayList<NotificationDetailsEntity> issueListArrList) {
        mIssueListArrList = issueListArrList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adap_notification_list_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {

        holder.mNotificationNameTxt.setText(mIssueListArrList.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return mIssueListArrList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.notification_name_txt)
        TextView mNotificationNameTxt;


        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
