package vocal.remover.karaoke.instrumental.app.utils_java;

import android.app.Application;

import androidx.multidex.MultiDexApplication;

import com.downloader.PRDownloader;

public class MyApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        // Required initialization logic here!
        PRDownloader.initialize(getApplicationContext());
    }
}
