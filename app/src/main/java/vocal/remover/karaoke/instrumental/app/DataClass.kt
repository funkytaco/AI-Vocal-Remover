package vocal.remover.karaoke.instrumental.app

data class UploadResponse(
        val error: Boolean,
        val message: String,
        val file_path: String
)

data class ProcessMp3Response(
        val error: Boolean,
        val message: String,
        val file_path: String
)