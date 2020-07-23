package vocal.remover.karaoke.instrumental.app.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import vocal.remover.karaoke.instrumental.app.R;
import vocal.remover.karaoke.instrumental.app.models.AudioModel;


public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.AudioModelViewHolder> {


    private Activity mCtx;
    private List<AudioModel> AudioModelList;

    public DownloadAdapter(Activity mCtx, List<AudioModel> AudioModelList) {
        this.mCtx = mCtx;
        this.AudioModelList = AudioModelList;
    }

    @Override
    public AudioModelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.row_mp3_file, null);
        return new AudioModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AudioModelViewHolder holder, int position) {
        AudioModel audioModel = AudioModelList.get(position);
        holder.itemView.setTag(audioModel);
        holder.tvName.setText(audioModel.getaName());
        holder.tvArtist.setText(audioModel.getaArtist());

        Log.e("TAG", "onBindViewHolder: "+ audioModel.getaName()+audioModel.getaArtist()+audioModel.getaAlbum()+audioModel.getaPath());


        //holder.imageView.setImageDrawable(mCtx.getResources().getDrawable(AudioModel.getImage()));

    }


    @Override
    public int getItemCount() {
        return AudioModelList.size();
    }


    class AudioModelViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvArtist;


        public AudioModelViewHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_title);
            tvArtist = itemView.findViewById(R.id.tv_artist);


            itemView.setOnClickListener(v-> {
                AudioModel audioModel =(AudioModel) v.getTag();

                if(Build.VERSION.SDK_INT>=24){
                    try{
                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        m.invoke(null);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                File file = new File(audioModel.getaPath());// path where your while is placed.For me like /storage/sdcard0/Media/audio/%2F1506580826442?alt=media&token=0e22f657-743c-4aed-9fed-48de69aced73.mp3
                intent.setDataAndType(Uri.fromFile(file), "audio/*");
                mCtx.startActivity(intent);
            });

        }
    }
}