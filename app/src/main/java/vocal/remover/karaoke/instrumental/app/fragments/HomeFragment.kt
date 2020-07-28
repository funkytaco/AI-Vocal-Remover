package vocal.remover.karaoke.instrumental.app.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
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
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.*
import com.karumi.dexter.listener.single.PermissionListener
import vocal.remover.karaoke.instrumental.app.activities.PurchaseActivity
import vocal.remover.karaoke.instrumental.app.utils_java.AppUtils
import vocal.remover.karaoke.instrumental.app.utils_java.SessionManager
import vocal.remover.karaoke.instrumental.app.utils_java.SessionManager.getSessionManagerInstance
import java.lang.IllegalArgumentException

public class HomeFragment : Fragment(), UploadRequestBody.UploadCallback, RewardedVideoAdListener {
    lateinit var binding: FragmentHomeBinding
    private var selectedMp3Uri: Uri? = null
    val dialog = CustomProgressDialog.getInstance();
    lateinit var instrumentalLink: String
    lateinit var vocalLink: String
    lateinit var navController: NavController
    var mp: MediaPlayer? = null
    var totalTime: Int = 0
    lateinit var r: Runnable
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    val sessionManager: SessionManager = getSessionManagerInstance()
    val STORAGE_PERMISSION_REQUEST_CODE: Int = 222



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding.root
        navController = activity?.findNavController(R.id.nav_host_fragment)!!

        initCustomerCoins()
        requestStoragePermission()

        initAds()
        val mInterstitialAd = InterstitialAd(activity)
        mInterstitialAd.adUnitId = "ca-app-pub-9562015878942760/1838746657"
      //     mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712" //test ads
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        initInterStitials(mInterstitialAd)

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity)
        mRewardedVideoAd.rewardedVideoAdListener = this
       // mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", AdRequest.Builder().build()) //test ads
        mRewardedVideoAd.loadAd("ca-app-pub-9562015878942760/7452189506", AdRequest.Builder().build())

        binding.btnSelectMp3.setOnClickListener {
            when {
                activity?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) } == PackageManager.PERMISSION_GRANTED -> {
                    selectMp3File()
                    Log.e(TAG, "onPermissionDenieda1: Permission denied once" )
                }shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {

                val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
                alertBuilder.setCancelable(true)
                alertBuilder.setMessage("Storage permission is Needed!")
                alertBuilder.setPositiveButton("Allow Permission!", DialogInterface.OnClickListener { dialog, which -> requestStoragePermission() })
                val dialog: AlertDialog = alertBuilder.create()
                dialog.show()

            } else -> {
                    // You can directly ask for the permission.
//                    Log.e(TAG, "onPermissionDenieda2: Permission denied once" )
//                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE);
                    requestStoragePermission()

                }
            }
           }
        binding.btnExtractMp3.setOnClickListener { uploadMp3() }
        binding.btnProcessMp3.setOnClickListener { processMp3() }
        binding.btnViewResults.setOnClickListener {
            viewResults()
//            if (mInterstitialAd == null) {
//                viewResults()
//            } else {
//                if (mInterstitialAd.isLoaded) {
//                    mInterstitialAd.show()
//                } else {
//                    viewResults()
//                    Log.d("TAG", "The interstitial wasn't loaded yet.")
//                }
//            }
        }
        binding.btnPlay.setOnClickListener { playSelectedSong() }
        binding.btnDownloadPage.setOnClickListener {
           pauseMpPlayer()
            startActivity(Intent(activity, DownloadListActivity::class.java))
        }

        binding.btnCoins.setOnClickListener {
            pauseMpPlayer()
            if (sessionManager.coins >0) {
                showRewardedVideoDialog("TO GET MORE CREDITS, WATCH AN AD FOR 1 CREDIT OR SUBSCRIBE TO PRO VERSION")
            } else {
                showRewardedVideoDialog("SORRY, YOU NEED MORE CREDITS, WATCH AN AD FOR 1 CREDIT OR SUBSCRIBE TO PRO VERSION")
            }
        }


        return view
    }

    private fun pauseMpPlayer() {
        if (mp != null) {
            if (mp!!.isPlaying) {
                mp?.pause()
                binding.btnPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24)
            }
        }
    }

    private fun initCustomerCoins() {

        if (sessionManager.returningStatus == true) {
        binding.tvCoins.setText(""+sessionManager.coins)
        } else { //new suser
            sessionManager.returningStatus = true
            sessionManager.coins = 5
            binding.tvCoins.setText(""+sessionManager.coins)

        }

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
       if (processCoins() == false) {
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
        try {
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
        } catch (e: IllegalArgumentException) {
            AppUtils.showCustomDialog(activity, "Invalid characters found in this file Name. Kindly rename the mp3 file before extracting")
        }


    }

    private fun processCoins(): Boolean {
        if (sessionManager.coins >0) {
            sessionManager.coins--
            binding.tvCoins.setText("" + sessionManager.coins)
            return true
        } else {
            showRewardedVideoDialog("SORRY, YOU NEED MORE CREDITS, WATCH AN AD FOR 1 CREDIT OR SUBSCRIBE TO PRO VERSION")
            return false
        }
    }

    private fun showRewardedVideoDialog(text: String) {
        val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertBuilder.setCancelable(true)
        alertBuilder.setMessage(text)
        alertBuilder.setPositiveButton("WATCH AD", DialogInterface.OnClickListener { dialog, which -> if (mRewardedVideoAd.isLoaded) {
            pauseMpPlayer()
            mRewardedVideoAd.show()
        } })
        alertBuilder.setNegativeButton("SUBSCRIBE", DialogInterface.OnClickListener { dialog, which ->
           pauseMpPlayer()
            startActivity(Intent(activity, PurchaseActivity::class.java))
        })

        val dialog: AlertDialog = alertBuilder.create()
        dialog.show()
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
        private const val TAG = "HomeFragment"
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

    override fun onRewardedVideoAdClosed() {
        TODO("Not yet implemented")
    }

    override fun onRewardedVideoAdLeftApplication() {
        Log.e(TAG, "onRewardedVideoAdLeftApplication: " )
    }

    override fun onRewardedVideoAdLoaded() {
        Log.e(TAG, "onRewardedVideoAdLoaded: " )
    }

    override fun onRewardedVideoAdOpened() {
        Log.e(TAG, "onRewardedVideoAdOpened: " )
    }

    override fun onRewardedVideoCompleted() {
        Log.e(TAG, "onRewardedVideoCompleted: " )
    }

    override fun onRewarded(p0: RewardItem?) {
        sessionManager.coins++
        binding.tvCoins.setText(""+sessionManager.coins)
        Log.e(TAG, "onRewarded: " )
    }

    override fun onRewardedVideoStarted() {
        Log.e("TAG", "onRewardedVideoStarted: " )
    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
        Log.e(TAG, "onRewardedVideoAdFailedToLoad: " )
    }

    private fun requestStoragePermission() {

        Dexter.withActivity(activity)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object: PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                      //  selectMp3File()
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken?) {
                        token?.continuePermissionRequest()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        if (response.isPermanentlyDenied()) {
                            // navigate user to app settings

                            val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
                            alertBuilder.setCancelable(true)
                            alertBuilder.setMessage("Storage permission is Needed to use this app")
                            alertBuilder.setPositiveButton("Go To Settings", DialogInterface.OnClickListener { dialog, which ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:" + activity?.getPackageName()));
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity?.startActivity(intent);
                            })
                            val dialog: AlertDialog = alertBuilder.create()
                            dialog.show()



                        }
                    }


                })
                .withErrorListener(object : PermissionRequestErrorListener {
                    override fun onError(p0: DexterError?) {
                        Toast.makeText(activity, "Error occurred! " + p0.toString(), Toast.LENGTH_SHORT).show();
                    }

                }).check()
//
    }
}