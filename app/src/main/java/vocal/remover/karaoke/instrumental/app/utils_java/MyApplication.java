package vocal.remover.karaoke.instrumental.app.utils_java;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.multidex.MultiDexApplication;


public class MyApplication extends MultiDexApplication {

    private static final String PREF_TITLE = "AIVocalRemoverpref";
    private static MyApplication myApplication;

    public static SharedPreferences getSharedPreferencesCustomer() {
        return myApplication.getSharedPreferences(PREF_TITLE, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
    }
}
