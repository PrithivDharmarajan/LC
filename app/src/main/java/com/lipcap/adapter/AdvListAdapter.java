package com.lipcap.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lipcap.R;
import com.lipcap.model.output.AdvDetailsEntity;
import com.lipcap.utils.AppConstants;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AdvListAdapter extends RecyclerView.Adapter<AdvListAdapter.Holder> {

    private ArrayList<AdvDetailsEntity> mAdvDetailsArrList;
    private Context mContext;

    public AdvListAdapter(ArrayList<AdvDetailsEntity> advDetailsArrList, Context context) {
        mAdvDetailsArrList = advDetailsArrList;
        mContext = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adap_adv_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {

        if (mAdvDetailsArrList.get(position).getUrl().isEmpty()) {
            holder.mAdvImg.setImageResource(R.color.blue);
        } else {
            try {
                Glide.with(mContext)
                        .load(mAdvDetailsArrList.get(position).getUrl())
                        .apply(new RequestOptions().placeholder(R.color.blue).error(R.color.blue))
                        .into(holder.mAdvImg);

            } catch (Exception ex) {
                holder.mAdvImg.setImageResource(R.color.blue);
                Log.e(AppConstants.TAG, ex.getMessage());
            }
        }
        holder.mAmtTxt.setText(mAdvDetailsArrList.get(position).getAmount());
    }

    @Override
    public int getItemCount() {
        return mAdvDetailsArrList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.adv_img)
        ImageView mAdvImg;

        @BindView(R.id.amt_txt)
        TextView mAmtTxt;


        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
