package voice.features.bookOverview.sync

import android.content.ContentResolver
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import voice.core.data.BookId
import voice.core.data.repo.BookRepository
import voice.core.sync.ResumableDriveUploader
import voice.core.sync.UploadSource
import voice.features.bookOverview.bottomSheet.BottomSheetItem
import voice.features.bookOverview.bottomSheet.BottomSheetItemViewModel
import voice.features.bookOverview.di.BookOverviewScope

@SingleIn(BookOverviewScope::class)
@ContributesIntoSet(BookOverviewScope::class)
class SyncToCloudViewModel(
  private val bookRepository: BookRepository,
  private val contentResolver: ContentResolver,
  private val uploader: ResumableDriveUploader,
) : BottomSheetItemViewModel {

  override suspend fun items(bookId: BookId): List<BottomSheetItem> =
    listOf(BottomSheetItem.SyncToCloud)

  override suspend fun onItemClick(bookId: BookId, item: BottomSheetItem) {
    if (item != BottomSheetItem.SyncToCloud) return
    val book = bookRepository.get(bookId) ?: return

    book.chapters.forEachIndexed { index, chapter ->
      val uri = chapter.id.toUri()
      val name = uri.lastPathSegment?.substringAfterLast('/') ?: "chapter-" + (index + 1)
      val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
      uploader.upload(
        UploadSource(
          name = name,
          mimeType = mimeType,
          sizeBytes = chapter.fileSize,
          openStream = {
            requireNotNull(contentResolver.openInputStream(uri)) {
              "Could not open audiobook chapter: " + uri
            }
          },
        ),
      )
    }
  }
}
