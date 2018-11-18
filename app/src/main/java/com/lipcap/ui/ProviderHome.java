package com.lipcap.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lipcap.R;
import com.lipcap.fragment.CustomerMapFragment;
import com.lipcap.main.BaseActivity;
import com.lipcap.main.BaseFragment;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DialogManager;
import com.lipcap.utils.InterfaceTwoBtnCallback;
import com.lipcap.utils.PreferenceUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProviderHome extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    /*Header*/
    @BindView(R.id.header_txt)
    TextView mHeaderTxt;


    @BindView(R.id.header_start_img_lay)
    RelativeLayout mHeaderLeftFirstImgLay;

    @BindView(R.id.header_start_img)
    ImageView mHeaderLeftFirstImg;


    /*Current Fragment*/
    private BaseFragment mFragment;


    private boolean mIsDoubleBackToExitAppBool = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_home_screen);
        initView();
    }

    private void initView() {
        /*For error track purpose - log with class name*/
        AppConstants.TAG = this.getClass().getSimpleName();

        /*ButterKnife for variable initialization*/
        ButterKnife.bind(this);

        /*Keypad to be hidden when a touch made outside the edit text*/
        setupUI(mDrawerLayout);

        setHeaderAdjustmentView();

        /*Add Back stack*/
        getSupportFragmentManager().addOnBackStackChangedListener(getListener());

        /*set drawer action*/
        setDrawerAction(true);

        setHeaderTxt(getString(R.string.app_name));
        /*Add default fragment screen*/
        addFragment(new CustomerMapFragment());


    }


    /*Set header view*/
    private void setHeaderAdjustmentView() {
        /*Set header adjustment - status bar we applied transparent color so header tack full view*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mDrawerLayout.post(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDrawerLayout.setPadding(0, getStatusBarHeight(ProviderHome.this), 0, 0);
                        }
                    });
                }
            });
        }
    }

    /*Screen orientation Changes*/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setHeaderAdjustmentView();
    }

    /*Fragment addFragment*/
    public void addFragment(final BaseFragment fmt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fmt != null) {
                    mFragment = fmt;
                    FragmentManager fragmentManager = getSupportFragmentManager();
//                        if ((mFragment instanceof CustomerMapFragment && AppConstants.MAP_CURRENT_BACK_FRAGMENT instanceof CustomerMapFragment)
//                               || (mFragment instanceof NotificationFragment && AppConstants.NOTIFICATION_CURRENT_BACK_FRAGMENT instanceof NotificationFragment)) {
//                            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//                        }
                    fragmentManager.beginTransaction()
                            .addToBackStack(fmt.getClass().getSimpleName())
                            .setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left, R.anim.slide_out_right,
                                    R.anim.slide_in_left)
//                                .add(R.id.content_frame_lay, fmt)
                            .replace(R.id.content_frame_lay, fmt, fmt.getClass().getSimpleName())
                            .commit();

                } else {
                    Log.e(AppConstants.TAG, getString(R.string.err_create_frag));
                }
            }
        });

    }


    /*Fragment popBackStack Listener*/
    private FragmentManager.OnBackStackChangedListener getListener() {
        return new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                FragmentManager manager = getSupportFragmentManager();
                if (manager != null) {
                    int backStackEntryCount = manager.getBackStackEntryCount();
                    if (backStackEntryCount > 0) {
                        BaseFragment currFrag = (BaseFragment) manager.findFragmentById(R.id.content_frame_lay);
                        if (currFrag != null) {
                            hideSoftKeyboard(ProviderHome.this);
                            mFragment = currFrag;
                            currFrag.onFragmentResume();
                        } else {
                            Log.e(AppConstants.TAG, getString(R.string.err_create_frag));
                        }
                    }
                }
            }
        };
    }


    /*Slide drawer action*/
    public void setDrawerAction(boolean isMenuScreenBool) {

        /*set drawer mode*/
        mDrawerLayout.setDrawerLockMode(isMenuScreenBool ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (isMenuScreenBool) {
            /*set header left menu icon*/
            mHeaderLeftFirstImg.setImageResource(R.drawable.menu_white);
            mHeaderLeftFirstImgLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            });
        } else {
            /*set header left back icon*/
            mHeaderLeftFirstImg.setImageResource(R.drawable.back_black);
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
            mHeaderLeftFirstImgLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }


    /*set header text */
    public void setHeaderTxt(String headerTxtStr) {
        mHeaderTxt.setText(headerTxtStr);
    }


    /*Slide menu onClick*/
    @OnClick({R.id.profile_lay, R.id.issue_list_lay, R.id.notification_lay, R.id.rate_service_provider_lay, R.id.contact_lay
            , R.id.support_lay, R.id.about_the_app_lay, R.id.logout_lay})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
//                 if (!(mFragment instanceof NotificationFragment)) {
//                     addFragment(new NotificationFragment());
//                 }
                break;
            case R.id.issue_list_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.notification_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.rate_service_provider_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.contact_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.support_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.about_the_app_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.logout_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                /*Logout popup*/
                DialogManager.getInstance().showOptionPopup(this, getString(R.string.logout_msg), getString(R.string.yes), getString(R.string.no), new InterfaceTwoBtnCallback() {
                    @Override
                    public void onPositiveClick() {
                        PreferenceUtil.storeBoolPreferenceValue(ProviderHome.this, AppConstants.LOGIN_STATUS, false);
                        previousScreen(Login.class);
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                });
                break;
        }
    }


    /*Default back button action*/
    @Override
    public void onBackPressed() {
        hideSoftKeyboard(ProviderHome.this);
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            backButtonClick();
        }
    }

    /*get pr class from popBackStack*/
    private void backButtonClick() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (getSupportFragmentManager() != null) {
            if (count == 1) {
                exitFromApp();
            } else {
                /*get previous class from popBackStack*/
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    /*App exit process*/
    private void exitFromApp() {
        if (mIsDoubleBackToExitAppBool) {
            finishAffinity();
            return;
        }

        mIsDoubleBackToExitAppBool = true;
        DialogManager.getInstance().showToast(this, getString(R.string.exit_msg));
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mIsDoubleBackToExitAppBool = false;
            }
        }, 2000);

    }


    /*activity result callback */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mFragment != null)
            mFragment.onActivityResult(requestCode, resultCode, data);
    }

    /*Request access permission callback*/
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (mFragment != null)
            mFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
