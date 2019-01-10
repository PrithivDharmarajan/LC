package com.lidcap.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.lidcap.R;
import com.lidcap.adapter.IssueListAdapter;
import com.lidcap.main.BaseFragment;
import com.lidcap.model.input.IssuesInputEntity;
import com.lidcap.model.output.AppointmentDetailsEntity;
import com.lidcap.model.output.IssuesListResponse;
import com.lidcap.services.APIRequestHandler;
import com.lidcap.utils.AppConstants;
import com.lidcap.utils.DialogManager;
import com.lidcap.utils.InterfaceBtnCallback;
import com.lidcap.utils.PreferenceUtil;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppointmentListFragment extends BaseFragment {


    @BindView(R.id.issue_list_recycler_view)
    RecyclerView mIssueListRecyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_issue_list, container, false);
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
         issueListAPICall();

    }

    private void issueListAPICall() {
        IssuesInputEntity issuesInputEntity =new IssuesInputEntity();
        issuesInputEntity.setUserId(PreferenceUtil.getUserId(getActivity()));
        APIRequestHandler.getInstance().issueListAPICall(issuesInputEntity,this);
    }


    private void setAdapter(final ArrayList<AppointmentDetailsEntity> issueArrListRes) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mIssueListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    mIssueListRecyclerView.setAdapter(new IssueListAdapter(issueArrListRes, getActivity()));
                }
            });
        }
    }


    /*API request success and failure*/
    @Override
    public void onRequestSuccess(Object resObj) {
        super.onRequestSuccess(resObj);
        if (resObj instanceof IssuesListResponse) {
            IssuesListResponse issuesListResponse = (IssuesListResponse) resObj;
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

