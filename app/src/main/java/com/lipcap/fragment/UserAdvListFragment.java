package com.lipcap.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.lipcap.R;
import com.lipcap.adapter.AdvListAdapter;
import com.lipcap.main.BaseFragment;
import com.lipcap.model.input.AdvInputEntity;
import com.lipcap.model.output.AdvDetailsEntity;
import com.lipcap.model.output.AdvResponse;
import com.lipcap.services.APIRequestHandler;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DialogManager;
import com.lipcap.utils.InterfaceBtnCallback;
import com.lipcap.utils.PreferenceUtil;

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
        advInputEntity.setProviderId(AppConstants.PROVIDER_ID);
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

