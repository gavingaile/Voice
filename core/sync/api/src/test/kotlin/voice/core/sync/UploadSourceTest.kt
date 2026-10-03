package voice.core.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFailsWith
import org.junit.Test
import java.io.ByteArrayInputStream

class UploadSourceTest {

  @Test
  fun acceptsAudiobookSource() {
    val source = UploadSource(
      name = "book.m4b",
      mimeType = "audio/mp4",
      sizeBytes = 3,
      openStream = { ByteArrayInputStream(byteArrayOf(1, 2, 3)) },
    )

    assertEquals(3, source.sizeBytes)
  }

  @Test
  fun rejectsNegativeSize() {
    assertFailsWith<IllegalArgumentException> {
      UploadSource("book.m4b", "audio/mp4", -1) { ByteArrayInputStream(byteArrayOf()) }
    }
  }
}
