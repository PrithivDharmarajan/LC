package com.lidcap.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lidcap.R;
import com.lidcap.model.output.AppointmentDetailsEntity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AppointmentListAdapter extends RecyclerView.Adapter<AppointmentListAdapter.Holder> {

    private ArrayList<AppointmentDetailsEntity> mIssueListArrList;
    private Context mContext;

    public AppointmentListAdapter(ArrayList<AppointmentDetailsEntity> issueListArrList, Context context) {
        mIssueListArrList = issueListArrList;
        mContext = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adap_appointment_list_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {

        holder.mAppointmentNameTxt.setText(mIssueListArrList.get(position).getIssueName());
        String statusStr = mIssueListArrList.get(position).getStatus();
        String statusResultStr = mContext.getString(R.string.canceled);
        if (statusStr.equalsIgnoreCase("2")) {
            statusResultStr = mContext.getString(R.string.accept);
        } else if (statusStr.equalsIgnoreCase("3") || statusStr.equalsIgnoreCase("4")) {
            statusResultStr = mContext.getString(R.string.completed);
        }
        holder.mAppointmentStatusBtn.setText(statusResultStr);

    }

    @Override
    public int getItemCount() {
        return mIssueListArrList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.appointment_name_txt)
        TextView mAppointmentNameTxt;

        @BindView(R.id.appointment_status_btn)
        Button mAppointmentStatusBtn;


        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
