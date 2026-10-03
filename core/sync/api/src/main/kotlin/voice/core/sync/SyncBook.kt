package voice.core.sync

import kotlinx.serialization.Serializable

/**
 * Device-independent identity for a synced audiobook.
 *
 * Local Voice BookIds contain Android document URIs, so they cannot be used
 * to identify the same audiobook on another device.
 */
@Serializable
public data class SyncBookId(val value: String)

@Serializable
public data class SyncProgress(
  val bookId: SyncBookId,
  val chapterIndex: Int,
  val positionInChapterMs: Long,
  val updatedAtEpochMs: Long,
) {
  init {
    require(chapterIndex >= 0)
    require(positionInChapterMs >= 0)
  }
}

@Serializable
public data class SyncBook(
  val id: SyncBookId,
  val name: String,
  val author: String?,
  val chapterCount: Int,
  val durationMs: Long,
  val progress: SyncProgress,
)
