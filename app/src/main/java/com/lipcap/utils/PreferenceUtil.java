package com.lipcap.utils;

import android.content.Context;
import android.content.SharedPreferences;



public class PreferenceUtil {

    private static int STRING_PREFERENCE = 1;
    private static int INT_PREFERENCE = 2;
    private static int BOOLEAN_PREFERENCE = 3;

    /*store preference*/
    private static void storeValueToPreference(Context context, int preference, String key, Object value) {
        if (context != null) {
            SharedPreferences sharedPreference = context.getSharedPreferences(
                    AppConstants.SHARE_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreference.edit();

            if (preference == STRING_PREFERENCE) {
                edit.putString(key, (String) value);
            }
            if (preference == INT_PREFERENCE) {
                edit.putInt(key, (int) value);
            }
            if (preference == BOOLEAN_PREFERENCE) {
                edit.putBoolean(key, (boolean) value);
            }

            if (android.os.Build.VERSION.SDK_INT >= 23) {
                edit.apply();
            } else {
                edit.commit();
            }

        }
    }

    /*get preference value*/
    private static Object getValueFromPreference(Context context, int preference, String key) {
        if (context != null) {
            SharedPreferences sharedPreference = context.getSharedPreferences(
                    AppConstants.SHARE_PREFERENCE, Context.MODE_PRIVATE);

            if (preference == STRING_PREFERENCE) {
                return sharedPreference.getString(key, "");
            }
            if (preference == INT_PREFERENCE) {
                return sharedPreference.getInt(key, 0);
            }
            if (preference == BOOLEAN_PREFERENCE) {
                return sharedPreference.getBoolean(key, false);
            }
        }

        return null;
    }


    /*Store bool value to preference*/
    public static void storeBoolPreferenceValue(Context context, String appConstantsStr, boolean keyBool) {
        if (context != null) {
            storeValueToPreference(context, BOOLEAN_PREFERENCE, appConstantsStr, keyBool);
        }
    }

    /*Get bool value from preference*/
    public static boolean getBoolPreferenceValue(Context context, String appConstantsStr) {
        return (boolean) getValueFromPreference(context,
                BOOLEAN_PREFERENCE, appConstantsStr);
    }


    /*Store String value to preference*/
    public static void storeStringPreferenceValue(Context context, String appConstantsVal, String key) {
        if (context != null) {
            storeValueToPreference(context, STRING_PREFERENCE, appConstantsVal, key);
        }
    }

    /*Get String value from preference*/
    public static String getStringPreferenceValue(Context context, String key) {
        return (String) getValueFromPreference(context, STRING_PREFERENCE, key);
    }

}