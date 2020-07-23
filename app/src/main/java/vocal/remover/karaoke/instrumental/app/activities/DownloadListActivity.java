package vocal.remover.karaoke.instrumental.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import vocal.remover.karaoke.instrumental.app.R;
import vocal.remover.karaoke.instrumental.app.adapters.DownloadAdapter;
import vocal.remover.karaoke.instrumental.app.databinding.ActivityDownloadListBinding;
import vocal.remover.karaoke.instrumental.app.models.AudioModel;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DownloadListActivity extends AppCompatActivity {
    ActivityDownloadListBinding binding;
    private static final int PERMISSION_STORAGE_CODE = 1000;
    RecyclerView recyclerView;
    List<AudioModel> audioList = new ArrayList<>();
    DownloadAdapter mp3Adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDownloadListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getSupportActionBar().setTitle("Downloads");

        initViews();
        initListeners();
        requestPermission();
        initAds();
    }
    private void initAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);
    }


    private void initRecyclerView() {

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        audioList.clear();
        audioList = getAllAudioFromDevice(this);
        Collections.reverse(audioList);
        mp3Adapter = new DownloadAdapter(this, audioList);

        recyclerView.setAdapter(mp3Adapter);
    }

    private void initListeners() {

        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initRecyclerView();
                binding.swipeContainer.setRefreshing(false);
            }
        });
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);

    }


    public List<AudioModel> getAllAudioFromDevice(final Context context) {

        String DOWNLOAD_FILE_DIR = Environment.DIRECTORY_DOWNLOADS + "/";
        final List<AudioModel> tempAudioList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.ArtistColumns.ARTIST,};
        Cursor c = context.getContentResolver().query(uri,
                projection,
                null,
                null,
                null);

        if (c != null) {
            while (c.moveToNext()) {

                if (c.getString(0).contains(".mp3")) {


                    AudioModel audioModel = new AudioModel();
                    String path = c.getString(0);
                    String album = c.getString(1);
                    String artist = c.getString(2);

                    String name = path.substring(path.lastIndexOf("/") + 1);

                    if (name.contains("INSTRUMENTAL")) {

                        audioModel.setaName(name);
                        audioModel.setaAlbum(album);
                        audioModel.setaArtist(artist);
                        audioModel.setaPath(path);

                        Log.e("Name :" + name, " Album :" + album);
                        Log.e("Path :" + path, " Artist :" + artist);

                        tempAudioList.add(audioModel);
                    }
                }
            }
            c.close();
        }

        return tempAudioList;
    }

    private void requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    AlertDialog.Builder alert_builder = new AlertDialog.Builder(this);
                    alert_builder.setMessage("External Storage Permission is Required.");
                    alert_builder.setTitle("Please Grant Permission.");
                    alert_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                            requestPermissions(permissions, PERMISSION_STORAGE_CODE);
                        }
                    });

                    alert_builder.setNeutralButton("Cancel", null);

                    AlertDialog dialog = alert_builder.create();

                    dialog.show();

                } else {


                    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissions, PERMISSION_STORAGE_CODE);

                }

            } else {
                //permission already grannted
                initRecyclerView();
            }
        } else {
            initRecyclerView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_STORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initRecyclerView();
                } else {
                    //Permission Denied
                    Toast.makeText(this, "Permission Denied...", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}