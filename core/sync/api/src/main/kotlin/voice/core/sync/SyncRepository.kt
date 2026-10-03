package voice.core.sync

import kotlinx.coroutines.flow.Flow

/**
 * Cloud-sync boundary used by Voice features and playback.
 *
 * Provider-specific implementations (Google Drive, etc.) live outside this
 * module so the rest of Voice never depends directly on a cloud SDK.
 */
public interface SyncRepository {
  public fun books(): Flow<List<SyncBook>>

  public suspend fun push(book: SyncBook)

  public suspend fun pull(bookId: SyncBookId): SyncBook?

  public suspend fun remove(bookId: SyncBookId)
}
