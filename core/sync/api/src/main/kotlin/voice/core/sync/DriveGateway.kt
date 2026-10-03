package voice.core.sync

import java.io.InputStream

public object DriveSyncContract {
  public const val Scope: String = "https://www.googleapis.com/auth/drive.file"
  public const val FolderName: String = "Voice Sync"
  public const val FolderMimeType: String = "application/vnd.google-apps.folder"
}

public data class UploadSource(
  val name: String,
  val mimeType: String,
  val sizeBytes: Long,
  val openStream: () -> InputStream,
) {
  init {
    require(name.isNotBlank())
    require(mimeType.isNotBlank())
    require(sizeBytes >= 0)
  }
}

public data class ResumableUploadSession(val url: String)

public data class RemoteFile(
  val id: String,
  val name: String,
  val sizeBytes: Long?,
)

public sealed interface DriveUploadResult {
  public data class Complete(val file: RemoteFile) : DriveUploadResult
  public data class Incomplete(val uploadedBytes: Long) : DriveUploadResult
}

public interface DriveGateway {
  public suspend fun findOrCreateSyncFolder(): String

  public suspend fun startResumableUpload(
    parentFolderId: String,
    source: UploadSource,
  ): ResumableUploadSession

  /**
   * Sends one resumable-upload chunk beginning at [offsetBytes].
   *
   * Google Drive may acknowledge only part of an upload. Returning the
   * acknowledged byte count lets the caller reopen the source stream and
   * continue without retransmitting the entire audiobook.
   */
  public suspend fun uploadChunk(
    session: ResumableUploadSession,
    source: UploadSource,
    offsetBytes: Long,
    maxChunkBytes: Int,
  ): DriveUploadResult
}
