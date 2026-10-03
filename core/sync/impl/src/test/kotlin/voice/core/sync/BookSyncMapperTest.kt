package voice.core.sync

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import voice.core.data.Book
import voice.core.data.BookContent
import voice.core.data.BookId
import voice.core.data.Chapter
import voice.core.data.ChapterId
import java.time.Instant

class BookSyncMapperTest {

  @Test
  fun fingerprintIgnoresDeviceLocalUris() {
    val first = book("content://phone/book", "content://phone/chapter")
    val second = book("content://tablet/book", "content://tablet/chapter")

    assertEquals(BookSyncMapper.fingerprint(first), BookSyncMapper.fingerprint(second))
  }

  @Test
  fun fingerprintChangesWhenAudioShapeChanges() {
    val first = book("content://phone/book", "content://phone/chapter", fileSize = 100)
    val second = book("content://phone/book", "content://phone/chapter", fileSize = 101)

    assertNotEquals(BookSyncMapper.fingerprint(first), BookSyncMapper.fingerprint(second))
  }

  private fun book(
    bookUri: String,
    chapterUri: String,
    fileSize: Long = 100,
  ): Book {
    val chapterId = ChapterId(Uri.parse(chapterUri))
    val chapter = Chapter(
      id = chapterId,
      name = "Chapter",
      duration = 60_000,
      fileLastModified = Instant.EPOCH,
      fileSize = fileSize,
      markData = emptyList(),
    )
    return Book(
      content = BookContent(
        id = BookId(Uri.parse(bookUri)),
        playbackSpeed = 1f,
        skipSilence = false,
        isActive = true,
        lastPlayedAt = Instant.ofEpochMilli(123),
        author = "Author",
        name = "Book",
        addedAt = Instant.EPOCH,
        chapters = listOf(chapterId),
        currentChapter = chapterId,
        positionInChapter = 42_000,
        cover = null,
        gain = 0f,
        genre = null,
        narrator = null,
        series = null,
        part = null,
      ),
      chapters = listOf(chapter),
    )
  }
}
