package voice.core.sync

import voice.core.data.Book
import java.security.MessageDigest

/**
 * Converts Voice's device-local book model into a portable cloud representation.
 */
public object BookSyncMapper {

  public fun toSyncBook(book: Book): SyncBook {
    val id = SyncBookId(fingerprint(book))
    return SyncBook(
      id = id,
      name = book.content.name,
      author = book.content.author,
      chapterCount = book.chapters.size,
      durationMs = book.duration,
      progress = SyncProgress(
        bookId = id,
        chapterIndex = book.content.currentChapterIndex,
        positionInChapterMs = book.content.positionInChapter,
        updatedAtEpochMs = book.content.lastPlayedAt.toEpochMilli(),
      ),
    )
  }

  /**
   * Intentionally excludes local URIs and timestamps: those can differ after
   * copying the same audiobook to another Android device.
   */
  public fun fingerprint(book: Book): String {
    val source = buildString {
      append(book.content.name.trim())
      append('\u0000')
      append(book.content.author?.trim().orEmpty())
      append('\u0000')
      book.chapters.forEach { chapter ->
        append(chapter.fileSize)
        append(':')
        append(chapter.duration)
        append(';')
      }
    }
    return MessageDigest.getInstance("SHA-256")
      .digest(source.toByteArray(Charsets.UTF_8))
      .joinToString("") { "%02x".format(it) }
  }
}
