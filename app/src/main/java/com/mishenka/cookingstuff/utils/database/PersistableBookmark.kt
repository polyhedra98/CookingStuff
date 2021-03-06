package com.mishenka.cookingstuff.utils.database

import android.os.Parcel
import android.os.Parcelable
import android.provider.Settings
import android.support.annotation.WorkerThread
import com.mishenka.cookingstuff.data.BookmarkData

class PersistableBookmark<T: Parcelable>(private val mDb: CookingDatabase) {

    @WorkerThread
    fun saveBookmark(id: String, data: T) {
        val bookmarkData = BookmarkObject(data)
        val bookmark = Bookmark(id, bookmarkData.toBytes())
        mDb.bookmarkDao().saveBookmark(bookmark)
        bookmarkData.recycle()
    }

    @WorkerThread
    fun loadBookmark(id: String, creator: Parcelable.Creator<T>): T? {
        val bookmarkData = mDb.bookmarkDao().getBookmark(id) ?: return null
        val bookmarkObject = BookmarkObject(bookmarkData.data)
        val bookmarkToReturn = creator.createFromParcel(bookmarkObject.getParcel())
        bookmarkObject.recycle()
        return bookmarkToReturn
    }

    @WorkerThread
    fun deleteBookmark(id: String, creator: Parcelable.Creator<T>? = null): T? {
        var bookmarkDataToProcess: Bookmark? = null
        creator?.let {
            bookmarkDataToProcess = mDb.bookmarkDao().getBookmark(id)
        }
        mDb.bookmarkDao().deleteBookmark(id)
        return if (bookmarkDataToProcess == null) {
            null
        } else {
            val bookmarkObject = BookmarkObject(bookmarkDataToProcess!!.data)
            val bookmarkToReturn = creator!!.createFromParcel(bookmarkObject.getParcel())
            bookmarkObject.recycle()
            bookmarkToReturn
        }
    }

    @WorkerThread
    fun loadBookmarks(creator: Parcelable.Creator<T>): List<T>? {
        val bookmarksDataList = mDb.bookmarkDao().getBookmarks()
        if (bookmarksDataList == null || bookmarksDataList.isEmpty()) {
            return null
        }
        val bookmarkObjectsList = ArrayList<T>(bookmarksDataList.size)
        for (bookmarkData in bookmarksDataList) {
            val bookmarkObject = BookmarkObject(bookmarkData.data)
            bookmarkObjectsList.add(creator.createFromParcel(bookmarkObject.getParcel()))
            bookmarkObject.recycle()
        }
        return bookmarkObjectsList.toList()
    }

    private class BookmarkObject {
        private val parcel = Parcel.obtain()

        internal constructor(parcelable: Parcelable) {
            parcelable.writeToParcel(parcel, 0)
        }

        internal constructor(data: ByteArray) {
            parcel.unmarshall(data, 0, data.size)
            parcel.setDataPosition(0)
        }

        internal fun toBytes(): ByteArray {
            return parcel.marshall()
        }

        fun getParcel(): Parcel {
            parcel.setDataPosition(0)
            return parcel
        }

        fun recycle() {
            parcel.recycle()
        }
    }
}