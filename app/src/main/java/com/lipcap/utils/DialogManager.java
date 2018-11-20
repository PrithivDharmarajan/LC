package com.lipcap.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lipcap.R;
import com.lipcap.adapter.SelectIssueListAdapter;
import com.lipcap.model.output.IssueListEntity;

import java.util.ArrayList;


public class DialogManager {

    /*Init dialog instance*/
    private static final DialogManager sDialogInstance = new DialogManager();
    /*Init variable*/
    private Dialog mProgressDialog, mNetworkErrorDialog, mAlertDialog, mOptionDialog, mIssueListDialog, mReasonForCancelDialog,mCommentsDialog;
    private Toast mToast;

    public static DialogManager getInstance() {
        return sDialogInstance;
    }

    /*Init toast*/
    public void showToast(Context context, String message) {

        try {
            /*To check if the toast is projected, if projected it will be cancelled */
            if (mToast != null && mToast.getView().isShown()) {
                mToast.cancel();
            }

            mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            TextView toastTxt = mToast.getView().findViewById(
                    android.R.id.message);
            toastTxt.setTypeface(Typeface.SANS_SERIF);
            mToast.show();


        } catch (Exception e) {
            Log.e(AppConstants.TAG, e.toString());
        }
    }

    /*Default dialog init method*/
    private Dialog getDialog(Context context, int layout) {

        Dialog mCommonDialog;
        mCommonDialog = new Dialog(context);
        mCommonDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (mCommonDialog.getWindow() != null) {
            mCommonDialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            mCommonDialog.setContentView(layout);
            mCommonDialog.getWindow().setGravity(Gravity.CENTER);
            mCommonDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
        }
        mCommonDialog.setCancelable(false);
        mCommonDialog.setCanceledOnTouchOutside(false);

        return mCommonDialog;
    }


    public void showAlertPopup(Context context, String messageStr, final InterfaceBtnCallback dialogAlertInterface) {

        alertDismiss(mAlertDialog);
        mAlertDialog = getDialog(context, R.layout.popup_basic_alert);

        WindowManager.LayoutParams LayoutParams = new WindowManager.LayoutParams();
        Window window = mAlertDialog.getWindow();

        if (window != null) {
            LayoutParams.copyFrom(window.getAttributes());
            LayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            LayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(LayoutParams);
            window.setGravity(Gravity.CENTER);
        }

        TextView alertMsgTxt;
        Button alertPositiveBtn;

        /*Init view*/
        alertMsgTxt = mAlertDialog.findViewById(R.id.alert_msg_txt);
        alertPositiveBtn = mAlertDialog.findViewById(R.id.alert_positive_btn);

        /*Set data*/
        alertMsgTxt.setText(messageStr);
        alertPositiveBtn.setText(context.getString(R.string.ok));

        //Check and set button visibility
        alertPositiveBtn.setVisibility(View.VISIBLE);

        alertPositiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
                dialogAlertInterface.onPositiveClick();
            }
        });

        alertShowing(mAlertDialog);

    }


    public void showOptionPopup(Context context, String messageStr, String firstBtnName, String secondBtnName,
                                final InterfaceTwoBtnCallback dialogAlertInterface) {
        alertDismiss(mOptionDialog);
        mOptionDialog = getDialog(context, R.layout.popup_basic_alert);

        WindowManager.LayoutParams LayoutParams = new WindowManager.LayoutParams();
        Window window = mOptionDialog.getWindow();

        if (window != null) {
            LayoutParams.copyFrom(window.getAttributes());
            LayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            LayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(LayoutParams);
            window.setGravity(Gravity.CENTER);
        }

        TextView msgTxt;
        Button positiveBtn, negativeBtn;

        /*Init view*/
        msgTxt = mOptionDialog.findViewById(R.id.alert_msg_txt);
        positiveBtn = mOptionDialog.findViewById(R.id.alert_positive_btn);
        negativeBtn = mOptionDialog.findViewById(R.id.alert_negative_btn);

        negativeBtn.setVisibility(View.VISIBLE);

        /*Set data*/
        msgTxt.setText(messageStr);
        positiveBtn.setText(firstBtnName);
        negativeBtn.setText(secondBtnName);

        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOptionDialog.dismiss();
                dialogAlertInterface.onPositiveClick();
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOptionDialog.dismiss();
                dialogAlertInterface.onNegativeClick();
            }
        });

        alertShowing(mOptionDialog);

    }

    public Dialog showIssuesListPopup(final Context context, ArrayList<IssueListEntity> agentListArrList,
                                      final InterfaceEdtBtnCallback dialogAlertInterface) {
        alertDismiss(mIssueListDialog);
        mIssueListDialog = getDialog(context, R.layout.popup_select_issue_list_view);

        WindowManager.LayoutParams LayoutParams = new WindowManager.LayoutParams();
        Window window = mIssueListDialog.getWindow();

        if (window != null) {
            LayoutParams.copyFrom(window.getAttributes());
            LayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            LayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(LayoutParams);
            window.setGravity(Gravity.CENTER);
        }

        RecyclerView issueListRecyclerView;
        Button positiveBtn, negativeBtn;
//        LinearLayout agentParentLay;

        /*Init view*/
        issueListRecyclerView = mIssueListDialog.findViewById(R.id.issue_list_recycler_view);
        positiveBtn = mIssueListDialog.findViewById(R.id.alert_positive_btn);
        negativeBtn = mIssueListDialog.findViewById(R.id.alert_negative_btn);
//        agentParentLay = mAgentDialog.findViewById(R.id.agent_parent_lay);


        /*Set Adapter*/
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        issueListRecyclerView.setLayoutManager(linearLayoutManager);
        issueListRecyclerView.setAdapter(new SelectIssueListAdapter(agentListArrList, dialogAlertInterface, context));


        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIssueListDialog.dismiss();
                dialogAlertInterface.onPositiveClick("1");
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIssueListDialog.dismiss();
                dialogAlertInterface.onNegativeClick();
            }
        });

//        mAgentDialog.setCancelable(true);
//        mAgentDialog.setCanceledOnTouchOutside(true);
        alertShowing(mIssueListDialog);
        return mIssueListDialog;
    }


    public Dialog showReasonForCancelPopup(final Context context,
                                           final InterfaceEdtBtnCallback dialogAlertInterface) {
        alertDismiss(mReasonForCancelDialog);
        mReasonForCancelDialog = getDialog(context, R.layout.popup_reason_for_cancellation);

        WindowManager.LayoutParams LayoutParams = new WindowManager.LayoutParams();
        Window window = mReasonForCancelDialog.getWindow();

        if (window != null) {
            LayoutParams.copyFrom(window.getAttributes());
            LayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            LayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(LayoutParams);
            window.setGravity(Gravity.CENTER);
        }

        Button positiveBtn, negativeBtn;
        final RadioButton expectShortWaitTimeBtn, unableContactBtn, cobblerDeniedWorkBtn;

        /*Init view*/

        expectShortWaitTimeBtn = mReasonForCancelDialog.findViewById(R.id.expect_short_wait_time_btn);
        unableContactBtn = mReasonForCancelDialog.findViewById(R.id.unable_contact_btn);
        cobblerDeniedWorkBtn = mReasonForCancelDialog.findViewById(R.id.cobbler_denied_work_btn);

        positiveBtn = mReasonForCancelDialog.findViewById(R.id.alert_positive_btn);
        negativeBtn = mReasonForCancelDialog.findViewById(R.id.alert_negative_btn);


        expectShortWaitTimeBtn.setChecked(true);
        expectShortWaitTimeBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    unableContactBtn.setChecked(false);
                    cobblerDeniedWorkBtn.setChecked(false);
                }

            }
        });

        unableContactBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    expectShortWaitTimeBtn.setChecked(false);
                    cobblerDeniedWorkBtn.setChecked(false);
                }

            }
        });
        cobblerDeniedWorkBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    expectShortWaitTimeBtn.setChecked(false);
                    unableContactBtn.setChecked(false);
                }

            }
        });


        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReasonForCancelDialog.dismiss();
                dialogAlertInterface.onPositiveClick("1");
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReasonForCancelDialog.dismiss();
                dialogAlertInterface.onNegativeClick();
            }
        });

//        mAgentDialog.setCancelable(true);
//        mAgentDialog.setCanceledOnTouchOutside(true);
        alertShowing(mReasonForCancelDialog);
        return mReasonForCancelDialog;
    }


    public Dialog showCommentsPopup(final Context context,
                                           final InterfaceEdtBtnCallback dialogAlertInterface) {
        alertDismiss(mCommentsDialog);
        mCommentsDialog = getDialog(context, R.layout.popup_comments);

        WindowManager.LayoutParams LayoutParams = new WindowManager.LayoutParams();
        Window window = mCommentsDialog.getWindow();

        if (window != null) {
            LayoutParams.copyFrom(window.getAttributes());
            LayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            LayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(LayoutParams);
            window.setGravity(Gravity.CENTER);
        }

        Button positiveBtn, negativeBtn;
        final RadioButton workCompletedOnTimeBtn, workedProperlyBtn;

        /*Init view*/

        workCompletedOnTimeBtn = mCommentsDialog.findViewById(R.id.work_completed_on_time_btn);
        workedProperlyBtn = mCommentsDialog.findViewById(R.id.worked_properly_btn);

        positiveBtn = mCommentsDialog.findViewById(R.id.alert_positive_btn);
        negativeBtn = mCommentsDialog.findViewById(R.id.alert_negative_btn);

        workCompletedOnTimeBtn.setChecked(true);
        workCompletedOnTimeBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    workedProperlyBtn.setChecked(false);
                }

            }
        });

        workedProperlyBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    workCompletedOnTimeBtn.setChecked(false);
                }

            }
        });


        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommentsDialog.dismiss();
                dialogAlertInterface.onPositiveClick("1");
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommentsDialog.dismiss();
                dialogAlertInterface.onNegativeClick();
            }
        });

        alertShowing(mCommentsDialog);
        return mCommentsDialog;
    }


    public void showNetworkErrorPopup(Context context, String errorStr, final InterfaceBtnCallback dialogAlertInterface) {

        alertDismiss(mNetworkErrorDialog);

        mNetworkErrorDialog = getDialog(context, R.layout.popup_network_alert);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = mNetworkErrorDialog.getWindow();

        if (window != null) {
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            window.setGravity(Gravity.BOTTOM);
            window.getAttributes().windowAnimations = R.style.PopupBottomAnimation;
        }

        Button retryBtn;
        TextView errorMsgTxt;

        //Init View
        retryBtn = mNetworkErrorDialog.findViewById(R.id.retry_btn);
        errorMsgTxt = mNetworkErrorDialog.findViewById(R.id.error_msg_txt);

        /*Set data*/
        errorMsgTxt.setText(errorStr);

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNetworkErrorDialog.dismiss();
                dialogAlertInterface.onPositiveClick();
            }
        });

        alertShowing(mNetworkErrorDialog);
    }

    public void showProgress(Context context) {

        try {
            /*To check if the progressbar is shown, if the progressbar is shown it will be cancelled */
            hideProgress();

            /*Init progress dialog*/
            mProgressDialog = new Dialog(context);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            if (mProgressDialog.getWindow() != null) {
                mProgressDialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                mProgressDialog.setContentView(R.layout.popup_progress);
                mProgressDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                mProgressDialog.getWindow().setGravity(Gravity.CENTER);
                mProgressDialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.TRANSPARENT));
            }
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);

            /*To check if the dialog is null or not. if it'border_with_transparent_bg not a null, the dialog will be shown orelse it will not get appeared*/
            if (mProgressDialog != null) {
                mProgressDialog.show();
            }
        } catch (Exception e) {
            Log.e(AppConstants.TAG, e.getMessage());
        }
    }

    public void hideProgress() {
        /*hide progress*/
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            try {
                mProgressDialog.dismiss();
            } catch (Exception e) {
                Log.e(AppConstants.TAG, e.getMessage());
            }
        }
    }

    private void alertShowing(Dialog dialog) {
        /*To check if the dialog is null or not. if it'border_with_transparent_bg not a null, the dialog will be shown orelse it will not get appeared*/
        if (dialog != null) {
            try {
                dialog.show();
            } catch (Exception e) {
                Log.e(AppConstants.TAG, e.getMessage());
            }
        }
    }

    private void alertDismiss(Dialog dialog) {
        /*To check if the dialog is shown, if the dialog is shown it will be cancelled */
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                Log.e(AppConstants.TAG, e.getMessage());
            }
        }

    }


}
