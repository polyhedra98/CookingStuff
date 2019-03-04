package com.mishenka.cookingstuff.utils.database

import android.arch.persistence.room.*
import com.mishenka.cookingstuff.utils.Utils

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveBookmark(bookmark: Bookmark)

    @Query("SELECT * FROM ${Utils.DB_BOOKMARK_TABLE}")
    fun getBookmarks(): List<Bookmark>?

    @Query("SELECT * FROM ${Utils.DB_BOOKMARK_TABLE} WHERE (${Utils.DB_BOOKMARK_ID} == :id)")
    fun getBookmark(id: String) : Bookmark?

    @Transaction
    @Query("DELETE FROM ${Utils.DB_BOOKMARK_TABLE} WHERE (${Utils.DB_BOOKMARK_ID} == :id)")
    fun deleteBookmark(id: String)

    @Transaction
    @Query("DELETE FROM ${Utils.DB_BOOKMARK_TABLE}")
    fun deleteAllBookmarks()
}