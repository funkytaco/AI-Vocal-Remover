package vocal.remover.karaoke.instrumental.app.utils_java;

import android.content.SharedPreferences;

import static vocal.remover.karaoke.instrumental.app.utils_java.MyApplication.getSharedPreferencesCustomer;

public class SessionManager {

    private static SessionManager INSTANCE = null;

    public static SessionManager getSessionManagerInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager();
        }
        return(INSTANCE);
    }


    private final SharedPreferences pref = getSharedPreferencesCustomer();
    private static final String NAME = "NAME";
    private static final String COUNTER = "COUNTER";
    private static final String RETURNING_STATUS = "RETURNING_STATUS";
    private static final String COINS = "COINS";

    private void setIntPreference(String name, int value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(name, value);
        editor.apply();
    }

    private void setBooleanPreference(String name, boolean value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    private long getLongPreference(String name) {
        if (pref.contains(name)) {
            return pref.getLong(name, 0);
        } else {
            return 0;
        }
    }

    private void setLongPreference(String name, long value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(name, value);
        editor.apply();
    }


    private void setStringPreference(String name, String value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(name, value);
        editor.apply();
    }

    private void setFloatPreference(String name, float value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat(name, value);
        editor.apply();
    }

    private Integer getIntPreference(String name) {
        if (pref.contains(name)) {
            return pref.getInt(name, 0);
        } else {
            return 0;
        }
    }

    private boolean getBooleanPreference(String name) {
        return pref.contains(name) && pref.getBoolean(name, false);
    }

    private float getFloatPreference(String name) {
        if (pref.contains(name)) {
            return pref.getFloat(name, 0);
        } else {
            return 0;
        }
    }

    private String getStringPreference(String name) {
        if (pref.contains(name)) {
            return pref.getString(name, "");
        } else {
            return null;
        }
    }



    public void setName(String name) {
        setStringPreference(NAME, name);
    }

    public String getName() {
        return getStringPreference(NAME);
    }

    public void setCounter(String counter) {
        setStringPreference(COUNTER, counter);
    }


    public String getCounter() {
        return getStringPreference(COUNTER);
    }

    public void setReturningStatus(boolean returningStatus) {
        setBooleanPreference(RETURNING_STATUS, returningStatus);
    }


    public boolean getReturningStatus() {
        return getBooleanPreference(RETURNING_STATUS);
    }

    public void setCoins(int coins) {
        setIntPreference(COINS, coins);
    }


    public int getCoins() {
        return getIntPreference(COINS);
    }

}
