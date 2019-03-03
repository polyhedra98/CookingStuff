package com.mishenka.cookingstuff.utils.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.mishenka.cookingstuff.utils.Utils

@Entity(tableName = Utils.DB_BOOKMARK_TABLE)
class Bookmark internal constructor(
        @field:ColumnInfo(name = Utils.DB_BOOKMARK_ID)
            @field:PrimaryKey
            val id: String,
        @field:ColumnInfo(name = Utils.DB_BOOKMARK_DATA, typeAffinity = ColumnInfo.BLOB)
            val data: ByteArray
)