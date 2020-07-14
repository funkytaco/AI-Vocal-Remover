package vocal.remover.karaoke.instrumental.app


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import vocal.remover.karaoke.instrumental.app.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class MainActivity : AppCompatActivity(), UploadRequestBody.UploadCallback {
    var binding: ActivityMainBinding? = null
    private var selectedImageUri: Uri? = null
    private lateinit var fileName: String;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)



        binding!!.imageView.setOnClickListener({ v -> openImageChooser() })
        binding!!.buttonUpload.setOnClickListener({ v -> uploadImage() })
        binding!!.buttonProcess.setOnClickListener({v-> processImage()})
    }

    private fun processImage() {
        MyAPI().processMp3(fileName).enqueue(object : Callback<ProcessMp3Response> {
            override fun onFailure(call: Call<ProcessMp3Response>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Failed; "+ t.message, Toast.LENGTH_LONG).show();
            }

            override fun onResponse(call: Call<ProcessMp3Response>, response: Response<ProcessMp3Response>) {

                Toast.makeText(this@MainActivity, "Successful"+ response.body()?.message, Toast.LENGTH_LONG).show();
            }
        })
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        val mimeTypes = arrayOf("audio/mpeg", "audio/mp3")
    //    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PICK_IMAGE -> {
                    selectedImageUri = data?.data
                    binding?.imageView?.setImageURI(selectedImageUri)

                }
            }
        }
    }

    private fun uploadImage() {
        if (selectedImageUri == null) {
            binding?.layoutRoot?.snackbar("Select an Image First")
            return
        }

        val parcelFileDescriptor: ParcelFileDescriptor =
                contentResolver.openFileDescriptor(selectedImageUri!!, "r", null) ?: return

        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(cacheDir, contentResolver.getFileName(selectedImageUri!!))
        fileName = file.name
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
                Toast.makeText(this@MainActivity, "Hellfso ${t.message}", Toast.LENGTH_LONG).show();
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
                }
            }
        })

    }

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 101
    }

    override fun onProgressUpdate(percentage: Int) {
        binding?.progressBar?.progress = percentage
    }
}



