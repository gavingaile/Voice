package voice.core.sync

public class ResumableDriveUploader(
  private val driveGateway: DriveGateway,
  private val chunkSizeBytes: Int = DefaultChunkSizeBytes,
) {

  init {
    require(chunkSizeBytes > 0)
  }

  public suspend fun upload(
    source: UploadSource,
    onProgress: (uploadedBytes: Long, totalBytes: Long) -> Unit = { _, _ -> },
  ): RemoteFile {
    val folderId = driveGateway.findOrCreateSyncFolder()
    val session = driveGateway.startResumableUpload(folderId, source)
    var offset = 0L

    while (true) {
      when (
        val result = driveGateway.uploadChunk(
          session = session,
          source = source,
          offsetBytes = offset,
          maxChunkBytes = chunkSizeBytes,
        )
      ) {
        is DriveUploadResult.Complete -> {
          onProgress(source.sizeBytes, source.sizeBytes)
          return result.file
        }

        is DriveUploadResult.Incomplete -> {
          require(result.uploadedBytes > offset) {
            "Drive upload made no forward progress"
          }
          require(result.uploadedBytes <= source.sizeBytes) {
            "Drive acknowledged more bytes than the source contains"
          }
          offset = result.uploadedBytes
          onProgress(offset, source.sizeBytes)
        }
      }
    }
  }

  public companion object {
    public const val DefaultChunkSizeBytes: Int = 8 * 1024 * 1024
  }
}
