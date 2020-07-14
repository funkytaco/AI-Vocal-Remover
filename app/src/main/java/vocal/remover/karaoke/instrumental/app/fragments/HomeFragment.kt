package vocal.remover.karaoke.instrumental.app.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import vocal.remover.karaoke.instrumental.app.*
import vocal.remover.karaoke.instrumental.app.databinding.FragmentHomeBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class HomeFragment : Fragment(), UploadRequestBody.UploadCallback {
    lateinit var binding: FragmentHomeBinding
    private var selectedMp3Uri: Uri? = null
    val dialog = CustomProgressDialog.getInstance();


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding.root

        binding.btnSelectMp3.setOnClickListener { selectMp3File() }
        binding.btnUploadMp3.setOnClickListener { uploadImage() }
        binding.btnProcessMp3.setOnClickListener { processMp3() }

        return view
    }

    private fun processMp3() {
        MyAPI().processMp3(getFormattedMp3FileName(selectedMp3Uri)).enqueue(object : Callback<ProcessMp3Response> {
            override fun onFailure(call: Call<ProcessMp3Response>, t: Throwable) {
                Toast.makeText(activity, "Failed; "+ t.message, Toast.LENGTH_LONG).show();
            }

            override fun onResponse(call: Call<ProcessMp3Response>, response: Response<ProcessMp3Response>) {

                Toast.makeText(activity, "Successful"+ response.body()?.message, Toast.LENGTH_LONG).show();
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
                    binding.btnUploadMp3.visibility = View.VISIBLE
                }
            }
        }

    }



    private fun uploadImage() {
        if (selectedMp3Uri == null) {
            binding?.layoutRoot?.snackbar("Select an Mp3 File First")
            return
        }
        dialog.ShowProgress(activity,"Uploading Mp3..",true);

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
                Toast.makeText(activity, "Hellfso ${t.message}", Toast.LENGTH_LONG).show();
                //  binding?.layoutRoot?.snackbar(t.message!!)
                binding?.progressBar?.progress = 0
            }

            override fun onResponse(
                    call: Call<UploadResponse>,
                    response: Response<UploadResponse>
            ) {
                //  Toast.makeText(this@MainActivity, "Hello ${response.body()?.message}", Toast.LENGTH_LONG).show();
                binding?.tvmessage?.setText(response.body()?.file_path)
                response.body()?.let {
                    binding?.layoutRoot?.snackbar(it.file_path)
                    binding?.progressBar?.progress = 100
                    dialog.hideProgress()
                    dialog.ShowProgress(activity,"Processing Mp3..",true);
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
        val file = File(activity?.cacheDir, activity?.contentResolver?.getFileName(selectedMp3Uri!!))
        return file.name.replace(" ", "")

    }


    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 101
    }

    override fun onProgressUpdate(percentage: Int) {
        binding?.progressBar?.progress = percentage
    }
}