package com.lipcap.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.lipcap.R;
import com.lipcap.adapter.IssueListAdapter;
import com.lipcap.main.BaseFragment;
import com.lipcap.model.output.IssueListEntity;
import com.lipcap.model.output.IssuesListResponse;
import com.lipcap.services.APIRequestHandler;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DialogManager;
import com.lipcap.utils.InterfaceBtnCallback;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IssueListFragment extends BaseFragment {


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
        APIRequestHandler.getInstance().issueListAPICall(this);
    }


    private void setAdapter(final ArrayList<IssueListEntity> issueArrListRes) {
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
            if (issuesListResponse.getMsgCode().equals(AppConstants.SUCCESS_CODE)) {
                if (issuesListResponse.getUserDetail().size() > 0) {
                    setAdapter(issuesListResponse.getUserDetail());
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

