package com.lidcap.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lidcap.R;
import com.lidcap.adapter.SelectIssueListAdapter;
import com.lidcap.model.output.IssueListEntity;
import com.lidcap.ui.common.Login;

import java.util.ArrayList;


public class DialogManager {

    /*Init dialog instance*/
    private static final DialogManager sDialogInstance = new DialogManager();
    /*Init variable*/
    private Dialog mProgressDialog, mNetworkErrorDialog, mAlertDialog, mOptionDialog, mIssueListDialog, mReasonForCancelDialog, mCommentsDialog, mSearchDialog, mProviderNotificationDialog, mCustomerNotificationDialog, mRequestDialog, mRequestCompletedDialog, mPictureAlertDialog, mAdvPictureAlertDialog, mOriginalImgDialog;
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

    public void showIssuesListPopup(final Context context, ArrayList<IssueListEntity> agentListArrList,
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
        issueListRecyclerView = mIssueListDialog.findViewById(R.id.recycler_view);
        positiveBtn = mIssueListDialog.findViewById(R.id.alert_positive_btn);
        negativeBtn = mIssueListDialog.findViewById(R.id.alert_negative_btn);


        /*Set Adapter*/
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        issueListRecyclerView.setLayoutManager(linearLayoutManager);
        issueListRecyclerView.setAdapter(new SelectIssueListAdapter(agentListArrList));


        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIssueListDialog.dismiss();
                dialogAlertInterface.onPositiveClick(AppConstants.ISSUE_ID);
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIssueListDialog.dismiss();
                dialogAlertInterface.onNegativeClick();
            }
        });

        alertShowing(mIssueListDialog);
    }


    public void showReasonForCancelPopup(final Context context,
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
        final EditText mTypeEdt;

        /*Init view*/

        expectShortWaitTimeBtn = mReasonForCancelDialog.findViewById(R.id.expect_short_wait_time_btn);
        unableContactBtn = mReasonForCancelDialog.findViewById(R.id.unable_contact_btn);
        cobblerDeniedWorkBtn = mReasonForCancelDialog.findViewById(R.id.cobbler_denied_work_btn);

        positiveBtn = mReasonForCancelDialog.findViewById(R.id.alert_positive_btn);
        negativeBtn = mReasonForCancelDialog.findViewById(R.id.alert_negative_btn);
        mTypeEdt = mCommentsDialog.findViewById(R.id.type_edt);


        expectShortWaitTimeBtn.setChecked(true);
        AppConstants.CUSTOMER_CANCEL_REASON = context.getString(R.string.expect_short_wait_time);
        expectShortWaitTimeBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    AppConstants.CUSTOMER_CANCEL_REASON = context.getString(R.string.expect_short_wait_time);
                    unableContactBtn.setChecked(false);
                    cobblerDeniedWorkBtn.setChecked(false);
                }

            }
        });

        unableContactBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    AppConstants.CUSTOMER_CANCEL_REASON = context.getString(R.string.unable_contact);
                    expectShortWaitTimeBtn.setChecked(false);
                    cobblerDeniedWorkBtn.setChecked(false);
                }

            }
        });
        cobblerDeniedWorkBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    AppConstants.CUSTOMER_CANCEL_REASON = context.getString(R.string.cobbler_denied_work);
                    expectShortWaitTimeBtn.setChecked(false);
                    unableContactBtn.setChecked(false);
                }

            }
        });


        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTypeEdt.getText().toString().trim().isEmpty()) {
                    DialogManager.getInstance().showAlertPopup(context, "Please Enter Valid Comments", new InterfaceBtnCallback() {
                        @Override
                        public void onPositiveClick() {
                            mTypeEdt.requestFocus();
                        }
                    });
                } else {
                    mReasonForCancelDialog.dismiss();
                    dialogAlertInterface.onPositiveClick("1");
                }
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReasonForCancelDialog.dismiss();
                dialogAlertInterface.onNegativeClick();
            }
        });

        alertShowing(mReasonForCancelDialog);
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
        final RatingBar friendlinessRatingBar;
        final EditText mTypeEdt;

        /*Init view*/

        workCompletedOnTimeBtn = mCommentsDialog.findViewById(R.id.work_completed_on_time_btn);
        workedProperlyBtn = mCommentsDialog.findViewById(R.id.worked_properly_btn);

        positiveBtn = mCommentsDialog.findViewById(R.id.alert_positive_btn);
        negativeBtn = mCommentsDialog.findViewById(R.id.alert_negative_btn);
        mTypeEdt = mCommentsDialog.findViewById(R.id.type_edt);
        friendlinessRatingBar = mCommentsDialog.findViewById(R.id.friendliness_rating_bar);


        workCompletedOnTimeBtn.setChecked(true);
        AppConstants.CUSTOMER_REVIEW = context.getString(R.string.work_completed_on_time);
        workCompletedOnTimeBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    AppConstants.CUSTOMER_REVIEW = context.getString(R.string.work_completed_on_time);
                    workedProperlyBtn.setChecked(false);
                }

            }
        });

        workedProperlyBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    AppConstants.CUSTOMER_REVIEW = context.getString(R.string.worked_properly);
                    workCompletedOnTimeBtn.setChecked(false);
                }

            }
        });


        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCommentsDialog.dismiss();
                AppConstants.CUSTOMER_RATING = String.valueOf(friendlinessRatingBar.getRating());
                AppConstants.CUSTOMER_COMMENTS = mTypeEdt.getText().toString().trim();
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


    public void showProviderAmtDialogPopup(final Context context, String issuesTypeStr,
                                           final InterfaceEdtBtnCallback dialogAlertInterface) {
        alertDismiss(mRequestDialog);
        mRequestDialog = getDialog(context, R.layout.popup_request_duration);

        WindowManager.LayoutParams LayoutParams = new WindowManager.LayoutParams();
        Window window = mRequestDialog.getWindow();

        if (window != null) {
            LayoutParams.copyFrom(window.getAttributes());
            LayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            LayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(LayoutParams);
            window.setGravity(Gravity.CENTER);
        }

        Button positiveBtn, negativeBtn;
        final TextView mIssuesTypeTxt;
        final EditText mSetDurationEdt;

        /*Init view*/

        mIssuesTypeTxt = mRequestDialog.findViewById(R.id.issues_type_txt);
        mSetDurationEdt = mRequestDialog.findViewById(R.id.set_duration_edt);

        positiveBtn = mRequestDialog.findViewById(R.id.alert_positive_btn);
        negativeBtn = mRequestDialog.findViewById(R.id.alert_negative_btn);

        mIssuesTypeTxt.setText(issuesTypeStr);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mSetDurationEdt.getText().toString().isEmpty()) {
                    mRequestDialog.dismiss();
                    dialogAlertInterface.onPositiveClick(mSetDurationEdt.getText().toString());
                } else {
                    DialogManager.getInstance().showToast(context, "Enter Valid Time");
                }
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestDialog.dismiss();
                dialogAlertInterface.onNegativeClick();
            }
        });

        alertShowing(mRequestDialog);
    }

    public void showProviderCompletedDialogPopup(final Context context, String issuesTypeStr,
                                                 final InterfaceEdtBtnCallback dialogAlertInterface) {
        alertDismiss(mRequestCompletedDialog);
        mRequestCompletedDialog = getDialog(context, R.layout.popup_request_completed);

        WindowManager.LayoutParams LayoutParams = new WindowManager.LayoutParams();
        Window window = mRequestCompletedDialog.getWindow();

        if (window != null) {
            LayoutParams.copyFrom(window.getAttributes());
            LayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            LayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(LayoutParams);
            window.setGravity(Gravity.CENTER);
        }

        Button positiveBtn, negativeBtn;
        final TextView mIssuesTypeTxt;
        final EditText mAmountEdt;

        /*Init view*/

        mIssuesTypeTxt = mRequestCompletedDialog.findViewById(R.id.issues_type_txt);
        mAmountEdt = mRequestCompletedDialog.findViewById(R.id.amount_edt);

        positiveBtn = mRequestCompletedDialog.findViewById(R.id.alert_positive_btn);
        negativeBtn = mRequestCompletedDialog.findViewById(R.id.alert_negative_btn);

        mIssuesTypeTxt.setText(issuesTypeStr);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mAmountEdt.getText().toString().isEmpty()) {
                    mRequestCompletedDialog.dismiss();
                    dialogAlertInterface.onPositiveClick(mAmountEdt.getText().toString().trim());
                } else {
                    DialogManager.getInstance().showToast(context, "Enter Valid Amount");
                }
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestCompletedDialog.dismiss();
                dialogAlertInterface.onNegativeClick();
            }
        });

        alertShowing(mRequestCompletedDialog);
    }


    public Dialog showSearchPopup(final Context context) {
        alertDismiss(mSearchDialog);
        mSearchDialog = getDialog(context, R.layout.popup_provier_search_alert);

        WindowManager.LayoutParams LayoutParams = new WindowManager.LayoutParams();
        Window window = mSearchDialog.getWindow();

        if (window != null) {
            LayoutParams.copyFrom(window.getAttributes());
            LayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            LayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(LayoutParams);
            window.setGravity(Gravity.CENTER);
        }
        alertShowing(mSearchDialog);
        return mSearchDialog;
    }

    public Dialog showProviderNotificationPopup(final Context context,
                                                final InterfaceTwoBtnCallback dialogAlertInterface) {
        alertDismiss(mProviderNotificationDialog);
        mProviderNotificationDialog = getDialog(context, R.layout.popup_notification_received);

        WindowManager.LayoutParams LayoutParams = new WindowManager.LayoutParams();
        Window window = mProviderNotificationDialog.getWindow();

        if (window != null) {
            LayoutParams.copyFrom(window.getAttributes());
            LayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            LayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(LayoutParams);
            window.setGravity(Gravity.CENTER);
        }

        Button positiveBtn, negativeBtn;

        /*Init view*/
        positiveBtn = mProviderNotificationDialog.findViewById(R.id.alert_positive_btn);
        negativeBtn = mProviderNotificationDialog.findViewById(R.id.alert_negative_btn);


        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProviderNotificationDialog.dismiss();
                dialogAlertInterface.onPositiveClick();
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogManager.getInstance().showOptionPopup(context, context.getString(R.string.cancel_appointment), context.getString(R.string.yes), context.getString(R.string.no), new InterfaceTwoBtnCallback() {
                    @Override
                    public void onNegativeClick() {

                    }

                    @Override
                    public void onPositiveClick() {
                        mProviderNotificationDialog.dismiss();
                        dialogAlertInterface.onNegativeClick();

                    }
                });
            }
        });

        alertShowing(mProviderNotificationDialog);

        return mProviderNotificationDialog;
    }

    public Dialog showCustomerNotificationPopup(final Context context, String msgStr,
                                                final InterfaceTwoBtnCallback dialogAlertInterface) {
        alertDismiss(mCustomerNotificationDialog);
        mCustomerNotificationDialog = getDialog(context, R.layout.popup_notification_received);

        WindowManager.LayoutParams LayoutParams = new WindowManager.LayoutParams();
        Window window = mCustomerNotificationDialog.getWindow();

        if (window != null) {
            LayoutParams.copyFrom(window.getAttributes());
            LayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            LayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(LayoutParams);
            window.setGravity(Gravity.CENTER);
        }

        Button positiveBtn, negativeBtn;
        TextView msgTxt;

        /*Init view*/
        msgTxt = mCustomerNotificationDialog.findViewById(R.id.alert_msg_txt);

        positiveBtn = mCustomerNotificationDialog.findViewById(R.id.alert_positive_btn);
        negativeBtn = mCustomerNotificationDialog.findViewById(R.id.alert_negative_btn);

        msgTxt.setText(String.format(context.getString(R.string.approximate_arrival_time), msgStr));
        positiveBtn.setBackgroundColor(ContextCompat.getColor(context, R.color.orange));


        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomerNotificationDialog.dismiss();
                dialogAlertInterface.onPositiveClick();
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogManager.getInstance().showOptionPopup(context, context.getString(R.string.cancel_appointment), context.getString(R.string.yes), context.getString(R.string.no), new InterfaceTwoBtnCallback() {
                    @Override
                    public void onNegativeClick() {

                    }

                    @Override
                    public void onPositiveClick() {
                        mCustomerNotificationDialog.dismiss();
                        dialogAlertInterface.onNegativeClick();

                    }
                });
            }
        });

        alertShowing(mCustomerNotificationDialog);

        return mCustomerNotificationDialog;
    }

    public Dialog showPictureUploadPopup(Context context, final InterfaceTwoBtnCallback interfaceTwoBtnCallback) {

        alertDismiss(mPictureAlertDialog);
        mPictureAlertDialog = getDialog(context, R.layout.popup_photo_selection);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = mPictureAlertDialog.getWindow();

        if (window != null) {
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
            window.setGravity(Gravity.CENTER);
        }
        Button cameraBtn, galleryBtn, cancelBtn;

        cameraBtn = mPictureAlertDialog.findViewById(R.id.camera_btn);
        galleryBtn = mPictureAlertDialog.findViewById(R.id.gallery_btn);
        cancelBtn = mPictureAlertDialog.findViewById(R.id.cancel_btn);


        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPictureAlertDialog.dismiss();
                interfaceTwoBtnCallback.onPositiveClick();
            }
        });
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPictureAlertDialog.dismiss();
                interfaceTwoBtnCallback.onNegativeClick();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPictureAlertDialog.dismiss();
            }
        });

        alertShowing(mPictureAlertDialog);
        return mPictureAlertDialog;
    }


    public void showImagePreviewDialogPopup(final Context context, Uri imageURI,
                                            final InterfaceEdtBtnCallback dialogAlertInterface) {
        alertDismiss(mAdvPictureAlertDialog);
        mAdvPictureAlertDialog = getDialog(context, R.layout.popup_adv_upload);

        WindowManager.LayoutParams LayoutParams = new WindowManager.LayoutParams();
        Window window = mAdvPictureAlertDialog.getWindow();

        if (window != null) {
            LayoutParams.copyFrom(window.getAttributes());
            LayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            LayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(LayoutParams);
            window.setGravity(Gravity.CENTER);
        }

        Button positiveBtn, negativeBtn;
        ImageView uploadImage;
        final EditText mAmountEdt;

        /*Init view*/

        uploadImage = mAdvPictureAlertDialog.findViewById(R.id.upload_img);
        mAmountEdt = mAdvPictureAlertDialog.findViewById(R.id.amount_edt);

        positiveBtn = mAdvPictureAlertDialog.findViewById(R.id.alert_positive_btn);
        negativeBtn = mAdvPictureAlertDialog.findViewById(R.id.alert_negative_btn);
        Glide.with(context)
                .load(imageURI)
                .into(uploadImage);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mAmountEdt.getText().toString().isEmpty()) {
                    mAdvPictureAlertDialog.dismiss();
                    dialogAlertInterface.onPositiveClick(mAmountEdt.getText().toString().trim());
                } else {
                    DialogManager.getInstance().showToast(context, "Enter Valid Amount");
                }
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdvPictureAlertDialog.dismiss();
                dialogAlertInterface.onNegativeClick();
            }
        });

        alertShowing(mAdvPictureAlertDialog);
    }

    public Dialog showOriginalImgPopup(final Context context, final String imgUrl, final InterfaceTwoBtnCallback dialogAlertInterface) {

        alertDismiss(mOriginalImgDialog);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View layoutView = inflater.inflate(R.layout.popup_image_viewer, null, false);

        mOriginalImgDialog = new Dialog(context,
                android.R.style.Theme_Translucent_NoTitleBar);
        mOriginalImgDialog = getDialog(context, R.layout.popup_image_viewer);
        mOriginalImgDialog.setContentView(layoutView);


        if (mOriginalImgDialog.getWindow() != null) {
            mOriginalImgDialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            mOriginalImgDialog.getWindow().setGravity(Gravity.CENTER);
            mOriginalImgDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams layoutParams = mOriginalImgDialog.getWindow().getAttributes();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }


        final RelativeLayout mainView;
        ImageView originalImg, closeImg;
        Button positiveBtn, negativeBtn;

        mainView = mOriginalImgDialog
                .findViewById(R.id.main_view);
        originalImg = mOriginalImgDialog
                .findViewById(R.id.original_img);
        closeImg = mOriginalImgDialog
                .findViewById(R.id.close_img);

        positiveBtn = mOriginalImgDialog.findViewById(R.id.alert_positive_btn);
        negativeBtn = mOriginalImgDialog.findViewById(R.id.alert_negative_btn);
//        ZoomImageView imageView = new ZoomImageView(context, mOriginalImgDialog.getWindow()

        negativeBtn.setVisibility(PreferenceUtil.getBoolPreferenceValue(context, AppConstants.CURRENT_USER_IS_PROVIDER) ? View.VISIBLE : View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mainView.post(new Runnable() {
                @Override
                public void run() {
                    mainView.setPadding(0, getStatusBarHeight(context), 0, 0);

                }
            });
        }
        if (imgUrl.isEmpty()) {
            originalImg.setImageResource(R.mipmap.ic_launcher);
        } else {
            try {
                Glide.with(context)
                        .asBitmap().load(imgUrl).into(originalImg);
            } catch (Exception e) {
                Log.e(AppConstants.TAG, e.toString());
                originalImg.setImageResource(R.mipmap.ic_launcher);
            }
        }


        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOriginalImgDialog.dismiss();
            }
        });

        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOriginalImgDialog.dismiss();
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogManager.getInstance().showOptionPopup(context, context.getString(R.string.delete_msg), context.getString(R.string.yes), context.getString(R.string.no), new InterfaceTwoBtnCallback() {
                    @Override
                    public void onNegativeClick() {
                    }

                    @Override
                    public void onPositiveClick() {
                        mOriginalImgDialog.dismiss();
                        dialogAlertInterface.onNegativeClick();

                    }
                });

            }
        });


        alertShowing(mOriginalImgDialog);
        return mOriginalImgDialog;
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

    private int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
