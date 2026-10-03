package voice.core.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFailsWith
import org.junit.Test

class SyncProgressTest {

  @Test
  fun acceptsPortableProgress() {
    val progress = SyncProgress(
      bookId = SyncBookId("book"),
      chapterIndex = 2,
      positionInChapterMs = 42_000,
      updatedAtEpochMs = 123,
    )

    assertEquals(2, progress.chapterIndex)
    assertEquals(42_000, progress.positionInChapterMs)
  }

  @Test
  fun rejectsNegativeChapterIndex() {
    assertFailsWith<IllegalArgumentException> {
      SyncProgress(SyncBookId("book"), -1, 0, 0)
    }
  }

  @Test
  fun rejectsNegativePosition() {
    assertFailsWith<IllegalArgumentException> {
      SyncProgress(SyncBookId("book"), 0, -1, 0)
    }
  }
}
