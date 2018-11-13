package com.lipcap.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.lipcap.R;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DialogManager;
import com.lipcap.utils.InterfaceBtnCallback;
import com.lipcap.utils.InterfaceTwoBtnCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class BaseActivity extends AppCompatActivity implements InterfaceTwoBtnCallback {

    private AppCompatActivity mActivity;
    private List<String> mAppPermissionsStrArrList;
    private InterfaceTwoBtnCallback mPermissionCallback = null;
    private int mAskPermissionCountInt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Default Init*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mActivity = this;

        /*Init default font*/
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/OpenSans-Regular.otf").build());
    }


    /*Apply font plugin default class*/
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    /*This method is used to check, if the current view can be focused in the edit text */
    protected void setupUI(View view) {

        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(mActivity);
                    return false;
                }
            });
        }
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View mInnerView = ((ViewGroup) view).getChildAt(i);
                setupUI(mInnerView);
            }
        }
    }

    /*Keypad to be hidden when a touch made outside the edit text*/
    protected void hideSoftKeyboard(Activity activity) {
        try {
            if (activity != null && !activity.isFinishing()) {
                InputMethodManager mInputMethodManager = (InputMethodManager) activity
                        .getSystemService(INPUT_METHOD_SERVICE);

                if (mInputMethodManager != null && activity.getCurrentFocus() != null
                        && activity.getCurrentFocus().getWindowToken() != null) {
                    mInputMethodManager.hideSoftInputFromWindow(activity
                            .getCurrentFocus().getWindowToken(), 0);
                }
            }
        } catch (Exception e) {
            Log.e(activity.getClass().getSimpleName(), e.getMessage());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        LipCapApplication.activityResumed();

    }

    @Override
    protected void onPause() {
        super.onPause();
        LipCapApplication.activityStopped();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /*Application control*/
    protected LipCapApplication app() {
        return ((LipCapApplication) mActivity.getApplication());
    }


    protected int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /*Direct to next activity*/
    public void nextScreen(Class<?> clazz) {
        Intent nextScreenIntent = new Intent(getApplicationContext(), clazz);
        mActivity.startActivity(nextScreenIntent);
        mActivity.overridePendingTransition(R.anim.slide_in_right,
                R.anim.slide_out_left);

        if (clearBackGroundScreen(clazz)) {
            AppConstants.PREVIOUS_SCREEN = new ArrayList<>();
        }
        AppConstants.PREVIOUS_SCREEN.add(clazz.getCanonicalName());
        mActivity.finish();

    }

    /*Direct to previous activity*/
    public void previousScreen(Class<?> clazz) {
        Intent previousScreenIntent = new Intent(getApplicationContext(), clazz);
        mActivity.startActivity(previousScreenIntent);
        mActivity.overridePendingTransition(R.anim.slide_out_right,
                R.anim.slide_in_left);
        if (clearBackGroundScreen(clazz)) {
            AppConstants.PREVIOUS_SCREEN = new ArrayList<>();
        }
        AppConstants.PREVIOUS_SCREEN.add(clazz.getCanonicalName());
        mActivity.finish();
    }

    /*Clear the all background activity*/
    private boolean clearBackGroundScreen(Class<?> clazz) {
        String classStr = clazz.getSimpleName();
        return classStr.equalsIgnoreCase(AppConstants.Login) ||
                classStr.equalsIgnoreCase(AppConstants.CustomerHome);

    }

    public void backScreen() {
        if (AppConstants.PREVIOUS_SCREEN != null && AppConstants.PREVIOUS_SCREEN.size() > 1) {
            AppConstants.PREVIOUS_SCREEN.remove(AppConstants.PREVIOUS_SCREEN.size() - 1);
            Class<?> clazz = null;
            try {
                clazz = Class.forName(AppConstants.PREVIOUS_SCREEN.get(AppConstants.PREVIOUS_SCREEN.size() - 1));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Intent previousScreenIntent = new Intent(getApplicationContext(), clazz);
            mActivity.startActivity(previousScreenIntent);
            mActivity.overridePendingTransition(R.anim.slide_out_right,
                    R.anim.slide_in_left);
            mActivity.finish();
        }
    }


    /*API call back success*/
    public void onRequestSuccess(Object resObj) {

    }

    /*API call back failure*/
    public void onRequestFailure(Object inputModelObj, Throwable t) {
        sysOut("Retrofit onRequestFailure" + t.toString());
        if (t.getMessage() != null && !t.getMessage().isEmpty() && !(t instanceof IOException)) {
            DialogManager.getInstance().showAlertPopup(mActivity, t.getMessage(), new InterfaceBtnCallback() {
                @Override
                public void onPositiveClick() {

                }
            });
        }
    }

    /*Ask permission for device access*/
    public boolean askAccessPermission(List<String> permissionStrList, int askPermissionCountInt, InterfaceTwoBtnCallback permissionCallback) {
        mAppPermissionsStrArrList = new ArrayList<>();
        mAppPermissionsStrArrList.addAll(permissionStrList);
        mAskPermissionCountInt = askPermissionCountInt;
        mPermissionCallback = permissionCallback;

        if (!mAppPermissionsStrArrList.isEmpty()) {
            ActivityCompat.requestPermissions(this, mAppPermissionsStrArrList.toArray(new String[mAppPermissionsStrArrList.size()]), 200);
            return false;
        }

        return true;
    }

    /*Permission call back*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200: {
                Map<String, Integer> perms = new HashMap<>();
                if (grantResults.length > 0) {
                    boolean isGrantAllPermissionBool = true;
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    for (int j = 0; j < mAppPermissionsStrArrList.size(); j++) {
                        if (perms.get(mAppPermissionsStrArrList.get(j)) == PackageManager.PERMISSION_GRANTED) {
                            if (j == mAppPermissionsStrArrList.size() - 1) {
                                if (isGrantAllPermissionBool)
                                    mPermissionCallback.onPositiveClick();
                                else if (mAskPermissionCountInt == 2)
                                    mPermissionCallback.onNegativeClick();
                                else
                                    askAccessPermission(mAppPermissionsStrArrList, mAskPermissionCountInt + 1, mPermissionCallback);
                            }
                        } else {
                            isGrantAllPermissionBool = false;
                            //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, mAppPermissionsStrArrList.get(j))) {
                                if (perms.get(mAppPermissionsStrArrList.get(j)) == PackageManager.PERMISSION_DENIED) {
                                    DialogManager.getInstance().showOptionPopup(mActivity, mActivity.getString(R.string.go_settings_per), getString(R.string.yes), getString(R.string.no), new InterfaceTwoBtnCallback() {
                                        @Override
                                        public void onPositiveClick() {
                                            Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                                            myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                                            myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivityForResult(myAppSettings, 168);
                                        }

                                        @Override
                                        public void onNegativeClick() {
                                            mPermissionCallback.onNegativeClick();
                                        }
                                    });
                                    break;

                                } else if (j == mAppPermissionsStrArrList.size() - 1) {
                                    if (mAskPermissionCountInt == 2)
                                        mPermissionCallback.onNegativeClick();
                                    else
                                        askAccessPermission(mAppPermissionsStrArrList, mAskPermissionCountInt + 1, mPermissionCallback);
                                }

                            } else {
                                if (j == mAppPermissionsStrArrList.size() - 1) {
                                    if (mAskPermissionCountInt == 2)
                                        mPermissionCallback.onNegativeClick();
                                    else
                                        askAccessPermission(mAppPermissionsStrArrList, mAskPermissionCountInt + 1, mPermissionCallback);
                                }
                            }
                        }
                    }

                } else if (mPermissionCallback != null) {
                    mPermissionCallback.onNegativeClick();
                }
            }
        }
    }


    /*Interface default ok click*/
    @Override
    public void onPositiveClick() {

    }

    @Override
    public void onNegativeClick() {

    }


    /*Temp*/
    public void sysOut(String printStr) {
        System.out.println(printStr);
    }

    protected void alertShowing(Dialog dialog) {
        /*To check if the dialog is null or not. if it'border_with_transparent_bg not a null, the dialog will be shown orelse it will not get appeared*/
        if (dialog != null) {
            try {
                dialog.show();
            } catch (Exception e) {
                Log.e(AppConstants.TAG, e.getMessage());
            }
        }
    }

    protected void alertDismiss(Dialog dialog) {
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
