package com.lidcap.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.lidcap.R;
import com.lidcap.model.output.IssueListEntity;
import com.lidcap.utils.AppConstants;
import com.lidcap.utils.InterfaceEdtBtnCallback;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SelectIssueListAdapter extends RecyclerView.Adapter<SelectIssueListAdapter.Holder> {

    private ArrayList<IssueListEntity> mIssueListArrList;
    private InterfaceEdtBtnCallback mInterfaceEdtBtnCallback;
    private CompoundButton mLastRadioBtn;
    private int mLastSelectedPosInt = 0;

    public SelectIssueListAdapter(ArrayList<IssueListEntity> issueListArrList, InterfaceEdtBtnCallback interfaceEdtBtnCallback, Context context) {
        mIssueListArrList = issueListArrList;
        mInterfaceEdtBtnCallback = interfaceEdtBtnCallback;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adap_selected_issue_list_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {

        holder.mAgentListTxt.setText(mIssueListArrList.get(position).getIssueName());

        if (mLastSelectedPosInt == position) {
            mLastRadioBtn = holder.mIssueRadioBtn;
            holder.mIssueRadioBtn.setChecked(true);
            AppConstants.ISSUE_ID=mIssueListArrList.get(position).getIssueId();
            AppConstants.ISSUE_NAME=mIssueListArrList.get(position).getIssueName();
        }
        holder.mIssueRadioBtn.setChecked(mLastSelectedPosInt == position);



    }

    @Override
    public int getItemCount() {
        return mIssueListArrList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.spinner_txt)
        TextView mAgentListTxt;

        @BindView(R.id.issue_radio_btn)
        RadioButton mIssueRadioBtn;


        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mIssueRadioBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLastSelectedPosInt = getAdapterPosition();
                    notifyDataSetChanged();
                }
            });
        }
    }
}
