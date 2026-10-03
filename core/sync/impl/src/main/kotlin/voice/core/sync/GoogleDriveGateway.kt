package voice.core.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URLEncoder

public class GoogleDriveGateway(
  private val client: OkHttpClient,
  private val tokenProvider: DriveAccessTokenProvider,
  private val json: Json = Json { ignoreUnknownKeys = true },
) : DriveGateway {

  override suspend fun findOrCreateSyncFolder(): String {
    val query = "name = '" + DriveSyncContract.FolderName + "' and mimeType = '" +
      DriveSyncContract.FolderMimeType + "' and trashed = false"
    val url = "https://www.googleapis.com/drive/v3/files?q=" +
      URLEncoder.encode(query, Charsets.UTF_8.name()) +
      "&spaces=drive&fields=files(id,name)"
    val request = Request.Builder().url(url).get().authorized().build()
    return execute(request).use { response ->
      val body = response.body?.string().orEmpty()
      if (response.code !in 200..299) throw IOException("Drive list failed: HTTP " + response.code)
      json.decodeFromString<FileList>(body).files.firstOrNull()?.id
        ?: throw IOException("Voice Sync folder does not exist yet")
    }
  }

  override suspend fun startResumableUpload(
    parentFolderId: String,
    source: UploadSource,
  ): ResumableUploadSession {
    throw UnsupportedOperationException("Resumable session transport is the next implementation step")
  }

  override suspend fun uploadChunk(
    session: ResumableUploadSession,
    source: UploadSource,
    offsetBytes: Long,
    maxChunkBytes: Int,
  ): DriveUploadResult {
    throw UnsupportedOperationException("Chunk transport is the next implementation step")
  }

  private suspend fun Request.Builder.authorized(): Request.Builder =
    header("Authorization", "Bearer " + tokenProvider.accessToken())

  private fun execute(request: Request) = client.newCall(request).execute()

  @Serializable
  private data class FileList(val files: List<DriveFile> = emptyList())

  @Serializable
  private data class DriveFile(val id: String)
}
