package com.lipcap.main;

import android.content.Context;

import com.crashlytics.android.BuildConfig;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import java.lang.Thread.UncaughtExceptionHandler;

import io.fabric.sdk.android.Fabric;


public class LipCapApplication extends android.app.Application {

    private static boolean activityVisible;
    private static LipCapApplication mInstance;

    public static synchronized LipCapApplication getInstance() {
        return mInstance;
    }

    public static Context getContext() {
        return mInstance;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityStopped() {
        activityVisible = false;
    }

    public static void activityFinished() {
        activityVisible = false;
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlytics = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

       // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlytics);

        /*init UncaughtException*/
        Thread.setDefaultUncaughtExceptionHandler(new unCaughtException());

    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }


    @Override
    public void registerActivityLifecycleCallbacks(
            ActivityLifecycleCallbacks callback) {
        super.registerActivityLifecycleCallbacks(callback);
    }

    /*unCaughtException*/
    private class unCaughtException implements UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Crashlytics.logException(ex);

            /*Restart application*/
//            if (activityVisible) {
//                Class<?> nextScreenClass = GeneralWelcome.class;
//                if (PreferenceUtil.getBoolPreferenceValue(mInstance, AppConstants.LOGIN_STATUS)) {
//                    nextScreenClass = PreferenceUtil.getBoolPreferenceValue(mInstance, AppConstants.CURRENT_USER_ADMIN) ? BeltList.class : UserDashboard.class;
//                }
//
//                /*for back screen process*/
//                AppConstants.PREVIOUS_SCREEN = new ArrayList<>();
//                AppConstants.PREVIOUS_SCREEN.add(nextScreenClass.getCanonicalName());
//
//                Intent intent = new Intent(mInstance, nextScreenClass);
//                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
//                getContext().startActivity(intent);
//
//                if (getContext() instanceof Activity) {
//                    ((Activity) getContext()).finish();
//                }
//
//                Runtime.getRuntime().exit(0);
//            }
        }
    }
}
