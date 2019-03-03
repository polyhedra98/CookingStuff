package com.mishenka.cookingstuff.utils.database

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.WorkerThread

class PersistableBookmark<T: Parcelable>(private val mDb: CookingDatabase) {

    @WorkerThread
    fun save(id: String, data: T) {
        val bookmarkData = BookmarkObject(data)
        val bookmark = Bookmark(id, bookmarkData.toBytes())
        mDb.bookmarkDao().saveBookmark(bookmark)
        bookmarkData.recycle()
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