package com.liskovsoft.sharedutils.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class SharedPreferencesBase {
    private final SharedPreferences mPrefs;
    protected Context mContext;

    public SharedPreferencesBase(Context context, String prefName) {
        mContext = context;
        mPrefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    public SharedPreferencesBase(Context context, String prefName, int defValResId) {
        mContext = context;
        mPrefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        PreferenceManager.setDefaultValues(context, prefName, Context.MODE_PRIVATE, defValResId, true);
    }

    public SharedPreferencesBase(Context context, int defValResId) {
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String defaultPrefsName = context.getPackageName() + "_preferences";
        PreferenceManager.setDefaultValues(context, defaultPrefsName, Context.MODE_PRIVATE, defValResId, true);
    }

    public SharedPreferencesBase(Context context) {
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
    }

    public void putLong(String key, long val) {
        mPrefs.edit()
                .putLong(key, val)
                .apply();
    }

    public long getLong(String key, long defVal) {
        return mPrefs.getLong(key, defVal);
    }

    public void putInt(String key, int val) {
        mPrefs.edit()
                .putInt(key, val)
                .apply();
    }

    public int getInt(String key, int defVal) {
        return mPrefs.getInt(key, defVal);
    }

    public void putBoolean(String key, boolean val) {
        mPrefs.edit()
                .putBoolean(key, val)
                .apply();
    }

    public boolean getBoolean(String key, boolean defVal) {
        return mPrefs.getBoolean(key, defVal);
    }

    public void putString(String key, String  val) {
        mPrefs.edit()
                .putString(key, val)
                .apply();
    }

    public String getString(String key, String defVal) {
        return mPrefs.getString(key, defVal);
    }
}
