package vocal.remover.karaoke.instrumental.app.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnBufferingUpdateListener
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.chibde.visualizer.LineBarVisualizer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import vocal.remover.karaoke.instrumental.app.R
import vocal.remover.karaoke.instrumental.app.databinding.FragmentPlayerBinding
import vocal.remover.karaoke.instrumental.app.utils_java.AppUtils
import java.io.IOException


class PlayerFragment : Fragment() {

    lateinit var binding: FragmentPlayerBinding
    lateinit var mp3Name: String
    val PERMISSION_REQUEST_CODE: Int = 111

    //  val player = MediaPlayer()
    var instrumentalPlayer: MediaPlayer? = MediaPlayer()
    val vocalPlayer = MediaPlayer()
    lateinit var instrumentalRunnable: Runnable


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        val view: View = binding.root

        initAds()

        val vocalLink: String? = arguments?.getString("vocal")
        val instrumentalLink: String? = arguments?.getString("instrumental")
        mp3Name = arguments?.getString("mp3_name").toString()

        initInstrumentalPlayer(instrumentalLink)
        initVocalPlayer(vocalLink)

        binding.btnInstrumentalPlay.setOnClickListener { playInstrumental() }
        binding.btnVocalPlay.setOnClickListener { playVocal() }


//        binding.btnPlayInstrumental.setOnClickListener { playMedialink(instrumentalLink) }
//        binding.btnPlayVocal.setOnClickListener { playMedialink(vocalLink) }

        // (String url, String dirPath, String fileName)

        binding.btnInstrumentalDownload.setOnClickListener {
            if (instrumentalLink != null) {
                startDownloading(instrumentalLink, "INSTRUMENTAL")
            }
        }

        binding.btnVocalDownload.setOnClickListener {
            if (vocalLink != null) {
                startDownloading(vocalLink, "VOCAL")
            }
        }

//        binding.btnDownloadInstrumental.setOnClickListener {
//            if (instrumentalLink != null) {
//                startDownloading(instrumentalLink)
//            }
//        }
//        binding.btnDownloadVocal.setOnClickListener {
//            if (vocalLink != null) {
//                startDownloading(vocalLink)
//            }
//        }


        requestPermission()



        return view
    }

    private fun initAds() {
        MobileAds.initialize(activity) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    private fun initVocalPlayer(vocalLink: String?) {

        try {
            context?.let { vocalPlayer.setDataSource(it, Uri.parse(vocalLink)) }
            vocalPlayer.prepareAsync()
            vocalPlayer.setOnPreparedListener(MediaPlayer.OnPreparedListener { //mp.start();
                // vocalPlayer.start()
                Log.e("TAG", "playMedialink: Vocal Player is Ready")
                Log.e("TAG", "playMedialink: size is " + vocalPlayer.duration)
            })
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            Log.e("TAG", "onResponse: Error1" + e.message)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            Log.e("TAG", "onResponse: Error2" + e.message)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("TAG", "onResponse: Error3" + e.message)
        }
    }

    private fun initInstrumentalPlayer(instrumentalLink: String?) {

        try {
            context?.let { instrumentalPlayer?.setDataSource(it, Uri.parse(instrumentalLink)) }
            instrumentalPlayer?.prepareAsync()
            instrumentalPlayer?.setOnPreparedListener(MediaPlayer.OnPreparedListener { //mp.start();
                instrumentalPlayer?.start()
                Log.e("TAG", "initInstrumentalPlayer: Playing now" )
                binding.tvBuffering.setText("PLAYING")
                initVisualizer(instrumentalPlayer!!)
                binding.btnInstrumentalPlay.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24)
                val totalTime = instrumentalPlayer?.duration!!
                binding.instrumentalSeekbar.max = totalTime
                binding.instrumentalSeekbar.setOnSeekBarChangeListener(
                        object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                                if (fromUser) {
                                    instrumentalPlayer?.seekTo(progress)
                                }
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) {

                            }

                            override fun onStopTrackingTouch(seekBar: SeekBar?) {

                            }

                        }
                )

                instrumentalRunnable = Runnable {
                    while (instrumentalPlayer != null) {
                        try {
                            var msg = Message()
                            msg.what = instrumentalPlayer?.currentPosition!!
                            handler.sendMessage(msg)
                            Thread.sleep(1000)
                        } catch (e: java.lang.IllegalStateException) {

                        }
                    }

                }


                Thread(instrumentalRunnable).start()
                instrumentalPlayer?.setOnBufferingUpdateListener(OnBufferingUpdateListener { mp, percent ->
                    binding.tvBuffering.setText("BUFFERING") })


                Log.e("TAG", "playMedialink: start playing")
                Log.e("TAG", "playMedialink: size is " + instrumentalPlayer?.duration)
            })
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            Log.e("TAG", "onResponse: Error1" + e.message)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            Log.e("TAG", "onResponse: Error2" + e.message)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("TAG", "onResponse: Error3" + e.message)
        }
    }



    private fun requestPermission() {
        when {
            activity?.let {
                ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.RECORD_AUDIO
                )
            } == PackageManager.PERMISSION_GRANTED -> {

            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
//            showInContextUI(...)
                val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
                alertBuilder.setCancelable(true)
                alertBuilder.setMessage("Write calendar permission is necessary to write event!!!")
                alertBuilder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which -> ActivityCompat.requestPermissions((context as Activity?)!!, arrayOf(Manifest.permission.WRITE_CALENDAR), PERMISSION_REQUEST_CODE) })
            }
            else -> {
                // You can directly ask for the permission.
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE);
            }
        }

    }

    private fun initVisualizer(player: MediaPlayer) {
        val lineBarVisualizer: LineBarVisualizer = binding.visualizer
        context?.let { ContextCompat.getColor(it, R.color.colorPrimary) }?.let { lineBarVisualizer.setColor(it) };
        lineBarVisualizer.setDensity(70f);
        try {
            lineBarVisualizer.setPlayer(player?.getAudioSessionId()!!);
        } catch (e : Exception){
            Log.e("TAG", "initVisualizer: UnsupportedOperationException Error: "+ e.message )
        }

    }

    private fun getMusicDirectoryPath(): String? {
        val path: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();
        Log.e("TAG", "getMusicDirectoryPath: Path is " + path)
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();
    }

    private fun playMedialink(mediaLink: String?) {

        try {
//            if (player.isPlaying) {
//                player.stop()
//                player = null
//            }
            //change with setDataSource(Context,Uri);
//            context?.let { player.setDataSource(it, Uri.parse(mediaLink)) }
//            player.prepareAsync()
//            player.setOnPreparedListener(MediaPlayer.OnPreparedListener { //mp.start();
//                player.start()
//                Log.e("TAG", "playMedialink: start playing" )
//                Log.e("TAG", "playMedialink: size is " +player.duration  )
//            })
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            Log.e("TAG", "onResponse: Error1" + e.message)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            Log.e("TAG", "onResponse: Error2" + e.message)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("TAG", "onResponse: Error3" + e.message)
        }

    }


    private fun startDownloading(link: String, type: String) {
        if (link.toString().equals("")) {
            Toast.makeText(activity, "Link Not Found", Toast.LENGTH_LONG).show()
            return
        }
        var downloadReference: Long
        Toast.makeText(activity, "File Downloading..", Toast.LENGTH_LONG).show()
        //create download request
        val request = DownloadManager.Request(Uri.parse(link))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or
                DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("AI Vocal Remover Download" )
        request.setDescription("Downloading "+ type + " of "+ mp3Name)
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/aivocalremover/" + type+ "_" + mp3Name + ".mp3")

        //get Download service and enque file
        val manager = activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadReference = manager!!.enqueue(request)

        //  showCustomDialog()
        Log.e("TAG", "startDownloading: Finished  downloading")
        AppUtils.showDownloadingDialog(activity, "File Downloading \n  ")
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }


    private fun playInstrumental() {
        if (instrumentalPlayer?.isPlaying!!) {
            //pause
            instrumentalPlayer?.pause()
            binding.btnInstrumentalPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24)
        } else {
            stopPlayingVocal()
            initVisualizer(instrumentalPlayer!!)
            //start
            instrumentalPlayer!!.start()
            binding.btnInstrumentalPlay.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24)
        }
    }

    private fun playVocal() {
        if (vocalPlayer.isPlaying) {
            //pause
            vocalPlayer.pause()
            binding.btnVocalPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24)
        } else {
            stopPlayingInstrumental()
            initVisualizer(vocalPlayer!!)
            //start
            vocalPlayer.start()
            binding.btnVocalPlay.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24)
        }
    }

    @SuppressLint("HandlerLeak")
    var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            var currentPosition = msg.what

            //update the seekbar
            binding.instrumentalSeekbar.progress = currentPosition
        }
    }

    override fun onStop() {
        super.onStop()
        stopPlayingVocal()
        stopPlayingInstrumental()
    }


    private fun stopPlayingInstrumental() {
        if (instrumentalPlayer != null) {
            if (instrumentalPlayer!!.isPlaying) {
                instrumentalPlayer?.pause()
                binding.btnInstrumentalPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24)
//                instrumentalPlayer?.release()
            }
         // instrumentalPlayer = null
        }
    }
    private fun stopPlayingVocal() {
        if (vocalPlayer != null) {
            if (vocalPlayer.isPlaying) {
                vocalPlayer?.pause()
                binding.btnVocalPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24)
//                vocalPlayer?.release()
            }

            // instrumentalPlayer = null
        }
    }
}