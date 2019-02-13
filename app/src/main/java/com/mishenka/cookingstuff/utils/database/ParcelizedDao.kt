package com.mishenka.cookingstuff.utils.database

import android.arch.persistence.room.*
import com.mishenka.cookingstuff.utils.Utils

@Dao
interface ParcelizedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveParcelized(parcelized: Parcelized)

    @Query("SELECT * FROM ${Utils.DB_UPLOAD_DATA_TABLE} WHERE ${Utils.DB_UPLOAD_DATA_ID} = :id")
    fun getParcelized(id: String): Parcelized?

    @Transaction
    @Query("DELETE FROM ${Utils.DB_UPLOAD_DATA_TABLE} WHERE (${Utils.DB_UPLOAD_DATA_EXPIRATION} IS NOT NULL) AND (${Utils.DB_UPLOAD_DATA_EXPIRATION} < :nowUtcMillis)")
    fun deleteExpiredParcelized(nowUtcMillis: Long)

    @Transaction
    @Query("DELETE FROM ${Utils.DB_UPLOAD_DATA_TABLE}")
    fun deleteAllParcelized()
}