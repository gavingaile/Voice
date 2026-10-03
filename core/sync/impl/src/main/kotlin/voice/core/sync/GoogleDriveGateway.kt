package voice.core.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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
    val metadata = FileMetadata(source.name, source.mimeType, listOf(parentFolderId))
    val request = Request.Builder()
      .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable&fields=id,name,size")
      .post(json.encodeToString(metadata).toRequestBody(JsonMediaType))
      .header("X-Upload-Content-Type", source.mimeType)
      .header("X-Upload-Content-Length", source.sizeBytes.toString())
      .authorized()
      .build()
    return execute(request).use { response ->
      if (response.code !in 200..299) {
        throw IOException("Drive resumable session failed: HTTP " + response.code)
      }
      ResumableUploadSession(
        requireNotNull(response.header("Location")) { "Drive omitted resumable session URL" },
      )
    }
  }

  override suspend fun uploadChunk(
    session: ResumableUploadSession,
    source: UploadSource,
    offsetBytes: Long,
    maxChunkBytes: Int,
  ): DriveUploadResult {
    require(offsetBytes in 0 until source.sizeBytes)
    require(maxChunkBytes > 0)
    val length = minOf(source.sizeBytes - offsetBytes, maxChunkBytes.toLong()).toInt()
    val bytes = source.openStream().use { input ->
      var skipped = 0L
      while (skipped < offsetBytes) {
        val count = input.skip(offsetBytes - skipped)
        if (count <= 0) throw IOException("Could not seek upload source to byte " + offsetBytes)
        skipped += count
      }
      input.readNBytes(length)
    }
    if (bytes.size != length) throw IOException("Upload source ended before declared size")

    val endByte = offsetBytes + length - 1
    val request = Request.Builder()
      .url(session.url)
      .put(bytes.toRequestBody(source.mimeType.toMediaType()))
      .header("Content-Range", "bytes " + offsetBytes + "-" + endByte + "/" + source.sizeBytes)
      .build()

    return execute(request).use { response ->
      when (response.code) {
        200, 201 -> {
          val file = json.decodeFromString<DriveFile>(response.body?.string().orEmpty())
          DriveUploadResult.Complete(RemoteFile(file.id, file.name, file.size))
        }
        308 -> DriveUploadResult.Incomplete(acknowledgedBytes(response.header("Range")))
        else -> throw IOException("Drive chunk upload failed: HTTP " + response.code)
      }
    }
  }

  private suspend fun Request.Builder.authorized(): Request.Builder =
    header("Authorization", "Bearer " + tokenProvider.accessToken())

  private fun execute(request: Request) = client.newCall(request).execute()

  private fun acknowledgedBytes(range: String?): Long {
    if (range == null) return 0L
    val lastByte = range.substringAfterLast('-').toLongOrNull()
      ?: throw IOException("Invalid Drive Range header")
    return lastByte + 1
  }

  @Serializable
  private data class FileList(val files: List<DriveFile> = emptyList())

  @Serializable
  private data class DriveFile(
    val id: String,
    val name: String = "",
    val size: Long? = null,
  )

  @Serializable
  private data class FileMetadata(
    val name: String,
    val mimeType: String? = null,
    val parents: List<String>? = null,
  )

  private companion object {
    val JsonMediaType = "application/json; charset=UTF-8".toMediaType()
  }
}
