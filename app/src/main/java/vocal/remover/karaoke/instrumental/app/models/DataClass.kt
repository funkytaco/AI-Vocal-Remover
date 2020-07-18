package vocal.remover.karaoke.instrumental.app.models

data class UploadResponse(
        val error: Boolean,
        val message: String,
        val file_path: String
)

data class AudioResultResponse(
        val error: Boolean,
        val message: String,
        val file_path: String,
        val vocal_path: String,
        val instrumental_path: String
)