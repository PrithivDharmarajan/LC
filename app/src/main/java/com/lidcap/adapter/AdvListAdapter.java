package com.lidcap.adapter;

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
import com.lidcap.R;
import com.lidcap.main.BaseFragment;
import com.lidcap.model.input.AdvDeleteInputEntity;
import com.lidcap.model.output.AdvDetailsEntity;
import com.lidcap.services.APIRequestHandler;
import com.lidcap.utils.AppConstants;
import com.lidcap.utils.DialogManager;
import com.lidcap.utils.InterfaceTwoBtnCallback;
import com.lidcap.utils.PreferenceUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AdvListAdapter extends RecyclerView.Adapter<AdvListAdapter.Holder> {

    private ArrayList<AdvDetailsEntity> mAdvDetailsArrList;
    private Context mContext;
    private BaseFragment mBaseFragment;

    public AdvListAdapter(ArrayList<AdvDetailsEntity> advDetailsArrList, BaseFragment baseFragment, Context context) {
        mAdvDetailsArrList = advDetailsArrList;
        mBaseFragment = baseFragment;
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogManager.getInstance().showOriginalImgPopup(mContext, mAdvDetailsArrList.get(holder.getAdapterPosition()).getUrl(), new InterfaceTwoBtnCallback() {
                    @Override
                    public void onNegativeClick() {
                        AdvDeleteInputEntity advDeleteInputEntity = new AdvDeleteInputEntity();
                        advDeleteInputEntity.setUserId(PreferenceUtil.getUserId(mContext));
                        advDeleteInputEntity.setAdvId(mAdvDetailsArrList.get(holder.getAdapterPosition()).getAdvId());

                        APIRequestHandler.getInstance().advDeleteAPICall(advDeleteInputEntity, mBaseFragment);
                    }

                    @Override
                    public void onPositiveClick() {

                    }
                });
            }
        });
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
