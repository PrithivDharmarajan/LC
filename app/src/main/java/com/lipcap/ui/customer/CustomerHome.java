package com.lipcap.ui.customer;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lipcap.R;
import com.lipcap.fragment.AboutFragment;
import com.lipcap.fragment.AppointmentListFragment;
import com.lipcap.fragment.CustomerMapFragment;
import com.lipcap.fragment.CustomerProfileFragment;
import com.lipcap.fragment.NotificationFragment;
import com.lipcap.main.BaseActivity;
import com.lipcap.main.BaseFragment;
import com.lipcap.ui.common.Login;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DialogManager;
import com.lipcap.utils.InterfaceTwoBtnCallback;
import com.lipcap.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomerHome extends BaseActivity implements View.OnClickListener {

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
        setContentView(R.layout.ui_customer_home_screen);
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
                            mDrawerLayout.setPadding(0, getStatusBarHeight(CustomerHome.this), 0, 0);
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

                    setHeaderTxt(mFragment);
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
                            hideSoftKeyboard(CustomerHome.this);
                            mFragment = currFrag;
                            setHeaderTxt(mFragment);
                            currFrag.onFragmentResume();
                        } else {
                            Log.e(AppConstants.TAG, getString(R.string.err_create_frag));
                        }
                    }
                }
            }
        };
    }

    private void setHeaderTxt(BaseFragment baseFragment) {
        String headerStr = getString(R.string.app_name);
        if (baseFragment instanceof CustomerProfileFragment) {
            headerStr = getString(R.string.profile);
        } else if (baseFragment instanceof AppointmentListFragment) {
            headerStr = getString(R.string.issue_list);
        } else if (baseFragment instanceof NotificationFragment) {
            headerStr = getString(R.string.notification);
        } else if (baseFragment instanceof AboutFragment) {
            headerStr = getString(R.string.about_the_app);
        }
        mHeaderTxt.setText(headerStr);
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
    @OnClick({R.id.profile_lay, R.id.appointment_list_lay, R.id.notification_lay, R.id.rate_app_lay, R.id.contact_lay
            , R.id.support_lay, R.id.about_the_app_lay, R.id.logout_lay})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                if (!(mFragment instanceof CustomerProfileFragment))
                    addFragment(new CustomerProfileFragment());
                break;
            case R.id.appointment_list_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                if (!(mFragment instanceof AppointmentListFragment))
                    addFragment(new AppointmentListFragment());
                break;
            case R.id.notification_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                if (!(mFragment instanceof NotificationFragment))
                    addFragment(new NotificationFragment());
                break;
            case R.id.rate_app_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                final String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException an) {
                    startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id="
                                    + appPackageName)));
                }
                break;
            case R.id.contact_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                if (askPermissions())
                    contactFlow();
                break;
            case R.id.support_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", AppConstants.SUPPORT_EMAIL, null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.lip_cap_support));
                startActivity(Intent.createChooser(emailIntent, getString(R.string.lip_cap_support)));
                break;
            case R.id.about_the_app_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                if (!(mFragment instanceof AboutFragment))
                    addFragment(new AboutFragment());
                break;
            case R.id.logout_lay:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                /*Logout popup*/
                DialogManager.getInstance().showOptionPopup(this, getString(R.string.logout_msg), getString(R.string.yes), getString(R.string.no), new InterfaceTwoBtnCallback() {
                    @Override
                    public void onPositiveClick() {
                        PreferenceUtil.storeBoolPreferenceValue(CustomerHome.this, AppConstants.LOGIN_STATUS, false);
                        previousScreen(Login.class);
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                });
                break;
        }
    }


    private void contactFlow() {
        Intent intent = new Intent(Intent.ACTION_CALL);

        intent.setData(Uri.parse("tel:" + AppConstants.CONTACT_PHONE_NUMBER));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);
    }

    /*Default back button action*/
    @Override
    public void onBackPressed() {
        hideSoftKeyboard(CustomerHome.this);
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


    /*To get permission for access image camera and storage*/
    private boolean askPermissions() {
        boolean addPermission = true;
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            addPermission = askAccessPermission(listPermissionsNeeded, 1, new InterfaceTwoBtnCallback() {
                @Override
                public void onPositiveClick() {
                    contactFlow();
                }

                public void onNegativeClick() {
                }
            });
        }

        return addPermission;
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

    @Override
    protected void onResume() {
        super.onResume();
        if(AppConstants.IS_FROM_PUSH){
            cancelNotification();
            AppConstants.IS_FROM_PUSH=false;
        }
    }

    public void cancelNotification() {
        /*0 is notification Id*/
        ((NotificationManager) Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE))).cancel(0);
    }
}
