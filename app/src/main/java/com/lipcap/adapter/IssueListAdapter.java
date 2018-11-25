package com.lipcap.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lipcap.R;
import com.lipcap.model.output.IssueListEntity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class IssueListAdapter extends RecyclerView.Adapter<IssueListAdapter.Holder> {

    private ArrayList<IssueListEntity> mIssueListArrList;
    private Context mContext;

    public IssueListAdapter(ArrayList<IssueListEntity> issueListArrList, Context context) {
        mIssueListArrList = issueListArrList;
        mContext = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adap_issue_list_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {

        holder.mIssueNameTxt.setText(mIssueListArrList.get(position).getIssueName());
        holder.mIssueStatusBtn.setText(mContext.getString(mIssueListArrList.get(position).getAppointmentStatus().trim().equalsIgnoreCase("1") ? R.string.completed : R.string.in_progress));


    }

    @Override
    public int getItemCount() {
        return mIssueListArrList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.issue_name_txt)
        TextView mIssueNameTxt;

        @BindView(R.id.issue_status_btn)
        Button mIssueStatusBtn;


        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
