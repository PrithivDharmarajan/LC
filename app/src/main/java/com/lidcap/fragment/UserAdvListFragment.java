package com.lidcap.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.lidcap.R;
import com.lidcap.adapter.AdvListAdapter;
import com.lidcap.main.BaseFragment;
import com.lidcap.model.input.AdvInputEntity;
import com.lidcap.model.output.AdvDetailsEntity;
import com.lidcap.model.output.AdvResponse;
import com.lidcap.services.APIRequestHandler;
import com.lidcap.utils.AppConstants;
import com.lidcap.utils.DialogManager;
import com.lidcap.utils.InterfaceBtnCallback;
import com.lidcap.utils.PreferenceUtil;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserAdvListFragment extends BaseFragment {


    @BindView(R.id.adv_list_recycler_view)
    RecyclerView mAdvListRecyclerView;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_adv_list, container, false);
        /*ButterKnife for variable initialization*/
        ButterKnife.bind(this, rootView);

        /*Keypad to be hidden when a touch made outside the edit text*/
        setupUI(rootView);

        /*For focus current fragment*/
        rootView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    default:
                        v.performClick();
                }
                return true;
            }
        });


        return rootView;
    }


    /*Fragment manual onResume*/
    @Override
    public void onFragmentResume() {
        if (getActivity() != null) {
            initView();
        }
    }

    /*InitViews*/
    private void initView() {

        AppConstants.TAG = this.getClass().getSimpleName();
         advListAPICall();

    }

    private void advListAPICall() {
        AdvInputEntity advInputEntity = new AdvInputEntity();
        advInputEntity.setUserId(PreferenceUtil.getUserId(getActivity()));
        advInputEntity.setProviderId(PreferenceUtil.getStringPreferenceValue(getActivity(),AppConstants.PROVIDER_ID));
        APIRequestHandler.getInstance().getUserAdvListAPICall(advInputEntity, this);
    }


    private void setAdapter(final ArrayList<AdvDetailsEntity> AdvDetailsRes) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdvListRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
                    mAdvListRecyclerView.setAdapter(new AdvListAdapter(AdvDetailsRes,getActivity()));
                }
            });
        }
    }


    /*API request success and failure*/
    @Override
    public void onRequestSuccess(Object resObj) {
        super.onRequestSuccess(resObj);
        if (resObj instanceof AdvResponse) {
            AdvResponse issuesListResponse = (AdvResponse) resObj;
            if (issuesListResponse.getStatusCode().equals(AppConstants.SUCCESS_CODE)) {
                if (issuesListResponse.getResult().size() > 0) {
                    setAdapter(issuesListResponse.getResult());
                }
            } else {
                DialogManager.getInstance().showAlertPopup(getActivity(), issuesListResponse.getMessage(), this);
            }

        }

    }

    @Override
    public void onRequestFailure(final Object resObj, Throwable t) {
        super.onRequestFailure(resObj, t);
        if (t instanceof IOException) {
            DialogManager.getInstance().showAlertPopup(getActivity(),
                    (t instanceof java.net.ConnectException || t instanceof java.net.UnknownHostException ? getString(R.string.no_internet) : getString(R.string
                            .connect_time_out)), new InterfaceBtnCallback() {
                        @Override
                        public void onPositiveClick() {
                        }
                    });
        }
    }









}

