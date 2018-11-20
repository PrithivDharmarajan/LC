package com.lipcap.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.lipcap.R;
import com.lipcap.model.output.IssueListEntity;
import com.lipcap.utils.InterfaceEdtBtnCallback;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SelectIssueListAdapter extends RecyclerView.Adapter<SelectIssueListAdapter.Holder> {

    private ArrayList<IssueListEntity> mIssueListArrList;
    private InterfaceEdtBtnCallback mInterfaceEdtBtnCallback;
    private CompoundButton mLastRadioBtn;

    public SelectIssueListAdapter(ArrayList<IssueListEntity> agentListArrList, InterfaceEdtBtnCallback interfaceEdtBtnCallback, Context context) {
        mIssueListArrList = agentListArrList;
        mInterfaceEdtBtnCallback = interfaceEdtBtnCallback;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adap_issue_list_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {

        holder.mAgentListTxt.setText(mIssueListArrList.get(position).getIssueName());

        if (position == 0) {
            mLastRadioBtn = holder.mIssueRadioBtn;
            holder.mIssueRadioBtn.setChecked(true);
        }
//        else {
//            holder.mIssueRadioBtn.setChecked(mLastSelectedInt == position);
//        }


        holder.mIssueRadioBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if ( mLastRadioBtn.getId() != buttonView.getId()) {
                    holder.mIssueRadioBtn.setOnCheckedChangeListener(null);
                    mLastRadioBtn.setChecked(false);
                    mLastRadioBtn = buttonView;
                    holder.mIssueRadioBtn.setOnCheckedChangeListener(this);
                }
//                if (isChecked) {
//                notifyItemChanged(mLastSelectedInt);
//                mLastSelectedInt = holder.getAdapterPosition();
//                    notifyDataSetChanged();
//                }
            }
        });
//   mInterfaceEdtBtnCallback.onPositiveClick(String.valueOf(mIssueListArrList.get(holder.getAdapterPosition()).getId()));


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
        }
    }
}