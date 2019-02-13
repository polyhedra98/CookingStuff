package com.mishenka.cookingstuff.utils.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.mishenka.cookingstuff.utils.Utils

@Entity(tableName = Utils.DB_UPLOAD_DATA_TABLE)
class Parcelized internal constructor(
        @field:ColumnInfo(name = Utils.DB_UPLOAD_DATA_ID)
            @field:PrimaryKey
            val id: String,
        @field:ColumnInfo(name = Utils.DB_UPLOAD_DATA_CREATION)
            val creationDateTime: Long,
        @field:ColumnInfo(name = Utils.DB_UPLOAD_DATA_EXPIRATION)
            val expirationDateTime: Long?,
        @field:ColumnInfo(name = Utils.DB_UPLOAD_DATA_DATA, typeAffinity = ColumnInfo.BLOB)
            val data: ByteArray
)