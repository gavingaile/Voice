package voice.core.sync

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class ResumableDriveUploaderTest {

  @Test
  fun resumesFromAcknowledgedOffsetUntilComplete() = runTest {
    val gateway = FakeDriveGateway()
    val uploader = ResumableDriveUploader(gateway, chunkSizeBytes = 4)
    val progress = mutableListOf<Long>()

    val file = uploader.upload(source()) { uploaded, _ -> progress += uploaded }

    assertEquals("remote", file.id)
    assertEquals(listOf(0L, 4L), gateway.offsets)
    assertEquals(listOf(4L, 6L), progress)
  }

  private fun source() = UploadSource(
    name = "book.m4b",
    mimeType = "audio/mp4",
    sizeBytes = 6,
    openStream = { ByteArrayInputStream(ByteArray(6)) },
  )

  private class FakeDriveGateway : DriveGateway {
    val offsets = mutableListOf<Long>()

    override suspend fun findOrCreateSyncFolder(): String = "folder"

    override suspend fun startResumableUpload(
      parentFolderId: String,
      source: UploadSource,
    ): ResumableUploadSession = ResumableUploadSession("session")

    override suspend fun uploadChunk(
      session: ResumableUploadSession,
      source: UploadSource,
      offsetBytes: Long,
      maxChunkBytes: Int,
    ): DriveUploadResult {
      offsets += offsetBytes
      return if (offsetBytes == 0L) {
        DriveUploadResult.Incomplete(4)
      } else {
        DriveUploadResult.Complete(RemoteFile("remote", source.name, source.sizeBytes))
      }
    }
  }
}
