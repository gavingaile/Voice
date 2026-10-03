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

public interface DriveGateway {
  public suspend fun findOrCreateSyncFolder(): String

  public suspend fun startResumableUpload(
    parentFolderId: String,
    source: UploadSource,
  ): ResumableUploadSession

  public suspend fun upload(
    session: ResumableUploadSession,
    source: UploadSource,
    onProgress: (uploadedBytes: Long, totalBytes: Long) -> Unit = { _, _ -> },
  ): RemoteFile
}
