package vocal.remover.karaoke.instrumental.app.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import vocal.remover.karaoke.instrumental.app.*
import vocal.remover.karaoke.instrumental.app.activities.DownloadListActivity
import vocal.remover.karaoke.instrumental.app.databinding.FragmentHomeBinding
import vocal.remover.karaoke.instrumental.app.models.AudioResultResponse
import vocal.remover.karaoke.instrumental.app.models.UploadResponse
import vocal.remover.karaoke.instrumental.app.utils_java.AppUtils.showCustomDialog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

public class HomeFragment : Fragment(), UploadRequestBody.UploadCallback {
    lateinit var binding: FragmentHomeBinding
    private var selectedMp3Uri: Uri? = null
    val dialog = CustomProgressDialog.getInstance();
    lateinit var instrumentalLink: String
    lateinit var vocalLink: String
    lateinit var navController: NavController
    var mp: MediaPlayer? = null
    var totalTime: Int = 0
    lateinit var r: Runnable



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding.root
        navController = activity?.findNavController(R.id.nav_host_fragment)!!

        initAds()
        val mInterstitialAd = InterstitialAd(activity)
        mInterstitialAd.adUnitId = "ca-app-pub-9562015878942760/1838746657"
     //   mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712" //test ads
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        initInterStitials(mInterstitialAd)

        binding.btnSelectMp3.setOnClickListener { selectMp3File() }
        binding.btnExtractMp3.setOnClickListener { uploadMp3() }
        binding.btnProcessMp3.setOnClickListener { processMp3() }
        binding.btnViewResults.setOnClickListener {

            if (mInterstitialAd == null) {
                viewResults()
            } else {
                if (mInterstitialAd.isLoaded) {
                    mInterstitialAd.show()
                } else {
                    viewResults()
                    Log.d("TAG", "The interstitial wasn't loaded yet.")
                }
            }
        }
        binding.btnPlay.setOnClickListener { playSelectedSong() }
        binding.btnDownload.setOnClickListener {
            if (mp != null) {
                if (mp!!.isPlaying) {
                    mp?.pause()
                    binding.btnPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24)
                }
            }
            startActivity(Intent(activity, DownloadListActivity::class.java))

        }

        return view
    }

    private fun initInterStitials(mInterstitialAd: InterstitialAd) {
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                // Code to be executed when an ad request fails.
                Log.e("TAG", "onAdFailedToLoad: Ad Failed to load")
            }

            override fun onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the interstitial ad is closed.
                viewResults()
            }
        }
    }


    private fun playSelectedSong() {

        if (mp!!.isPlaying) {
            //pause
            mp?.pause()
            binding.btnPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24)
        } else {
            //start
            mp?.start()
            binding.btnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24)
        }

    }

    private fun viewResults() {

        stopAndReleaseMediaPlayer()

        val bundle = Bundle()
        Log.e("TAG", "i am putting " + instrumentalLink)
        Log.e("TAG", "i am putting " + vocalLink)
        bundle.putString("instrumental", instrumentalLink)
        bundle.putString("vocal", vocalLink)
        bundle.putString("mp3_name", getMp3FileName(selectedMp3Uri))

        //(String url, String dirPath, String fileName)
        navController.navigate(R.id.action_homeFragment_to_playerFragment, bundle)
    }

    private fun stopAndReleaseMediaPlayer() {
        if (this::r.isInitialized) {
            handler.removeCallbacks(r)
        }
        if (mp != null) {
            mp?.stop()
            mp?.release()
            mp = null
        }


    }

    private fun processMp3() {
        MyAPI().processMp3(getFormattedMp3FileName(selectedMp3Uri)).enqueue(object : Callback<AudioResultResponse> {
            override fun onFailure(call: Call<AudioResultResponse>, t: Throwable) {
                Toast.makeText(activity, "Failed; " + t.message, Toast.LENGTH_LONG).show();
                dialog.hideProgress()
                showCustomDialog(activity, "Error: " + t.message)
            }

            override fun onResponse(call: Call<AudioResultResponse>, response: Response<AudioResultResponse>) {
                Log.e("TAG", "onResponse: " + getFormattedMp3FileName(selectedMp3Uri))
                // Toast.makeText(activity, "Successful" + response.body()?.message, Toast.LENGTH_LONG).show();
                //  Toast.makeText(activity, "Successful" + response.body()?.message, Toast.LENGTH_LONG).show();
                binding?.layoutRoot?.snackbar("Mp3 Extracted Successfully!!")
                binding.tvmessage.text = response.body()?.file_path
                instrumentalLink = response.body()?.instrumental_path.toString()
                vocalLink = response.body()?.vocal_path.toString()
                binding.btnViewResults.visibility = View.VISIBLE
                dialog.hideProgress()


            }
        })
    }

    private fun selectMp3File() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("TAG", "onActivityResult: ghgff")
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PICK_IMAGE -> {
                    selectedMp3Uri = data?.data
                    binding.tvFileName.text = getMp3FileName(selectedMp3Uri)
                    binding.btnExtractMp3.visibility = View.VISIBLE
                    binding.tvFileName.visibility = View.VISIBLE
                    binding.tvFileNameTitle.visibility = View.VISIBLE
                    binding.lyMp3Details.visibility = View.VISIBLE

                    initMediaPlayer(selectedMp3Uri)
                    playSelectedSong()
                }
            }
        }

    }

    private fun initMediaPlayer(selectedMp3Uri: Uri?) {
        stopPlaying()
        mp = MediaPlayer.create(activity, selectedMp3Uri)
        mp?.setVolume(0.5f, 0.5f)
        totalTime = mp?.duration!!

        binding.playerSeekbar.max = totalTime
        binding.playerSeekbar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            mp?.seekTo(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {

                    }

                }
        )

        r = Runnable {
            while (mp != null) {
                try {
                    var msg = Message()
                    msg.what = mp?.currentPosition!!
                    handler.sendMessage(msg)
                    Thread.sleep(1000)
                } catch (e: Exception) {
                    Log.e("TAG", "initMediaPlayer: " + e.message)
                }
            }
        }


        Thread(r).start()


    }

    @SuppressLint("HandlerLeak")
    var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            var currentPosition = msg.what

            //update the seekbar
            binding.playerSeekbar.progress = currentPosition
        }
    }


    private fun uploadMp3() {
        if (selectedMp3Uri == null) {
            binding?.layoutRoot?.snackbar("Select an Mp3 File First")
            return
        }
        dialog.ShowProgress(activity, "Uploading Mp3..", false);

        val parcelFileDescriptor: ParcelFileDescriptor =
                activity?.contentResolver?.openFileDescriptor(selectedMp3Uri!!, "r", null) ?: return

        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(activity?.cacheDir, activity?.contentResolver?.getFileName(selectedMp3Uri!!))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        binding?.progressBar?.progress = 0
        val body = UploadRequestBody(file, "audio", this)
        MyAPI().uploadImage(
                MultipartBody.Part.createFormData(
                        "fileName",
                        file.name,
                        body
                ),
                RequestBody.create(MediaType.parse("multipart/form-data"), "json")
        ).enqueue(object : Callback<UploadResponse> {
            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {

                if (t.message?.contains("Failed To Connect", true)!!) {
                    Toast.makeText(activity, "Upload Error: Internet Connection not Available", Toast.LENGTH_LONG).show();
                    showTimeOutDialog("Error: Internet Connection not Available")
                } else {
                    Toast.makeText(activity, "Upload Error: ${t.message}", Toast.LENGTH_LONG).show();
                    showTimeOutDialog("Error: " + t.message)
                }

                //  binding?.layoutRoot?.snackbar(t.message!!)
                binding?.progressBar?.progress = 0
                dialog.hideProgress()

            }

            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                //  Toast.makeText(this@MainActivity, "Hello ${response.body()?.message}", Toast.LENGTH_LONG).show();
                binding?.tvmessage?.setText(response.body()?.file_path)
                response.body()?.let {
                    //    binding?.layoutRoot?.snackbar(it.file_path)
                    binding?.layoutRoot?.snackbar("Upload Complete!!")
                    binding?.progressBar?.progress = 100
                    dialog.hideProgress()
                    dialog.ShowProgress(activity, "Processing Mp3..  ", false);
                    processMp3()
                }
            }
        })

    }

    private fun getMp3FileName(selectedMp3Uri: Uri?): String? {
        val file = File(activity?.cacheDir, activity?.contentResolver?.getFileName(selectedMp3Uri!!))
        return file.name
    }

    private fun getFormattedMp3FileName(selectedMp3Uri: Uri?): String? {

        var file = File(activity?.cacheDir, activity?.contentResolver?.getFileName(selectedMp3Uri!!))
        return file.name.replace("[^a-zA-Z]".toRegex(), "")

    }


    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 101
    }

    override fun onProgressUpdate(percentage: Int) {
        binding?.progressBar?.progress = percentage
    }


    fun showTimeOutDialog(msg: String?) {
        val dialog = this!!.activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setContentView(R.layout.custom_dialog)
        val text = dialog?.findViewById<View>(R.id.text_dialog) as TextView
        text.text = msg
        val dialogButton = dialog.findViewById<View>(R.id.btn_dialog) as Button
        dialogButton.setText("Retry")
        dialogButton.setOnClickListener {
            uploadMp3()
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun stopPlaying() {
        if (mp != null) {
            mp?.stop()
            mp?.release()
            mp = null
        }
    }

    private fun initAds() {
        MobileAds.initialize(activity) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)


    }

}