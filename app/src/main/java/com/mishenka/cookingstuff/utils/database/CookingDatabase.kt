package com.mishenka.cookingstuff.utils.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.mishenka.cookingstuff.utils.Utils

@Database(entities = arrayOf(Parcelized::class), version = 1)
abstract class CookingDatabase: RoomDatabase() {
    abstract fun parcelizedDao(): ParcelizedDao

    companion object {
        private var INSTANCE: CookingDatabase? = null

        fun getInstance(context: Context): CookingDatabase? {
            if (INSTANCE == null) {
                synchronized(CookingDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            CookingDatabase::class.java, Utils.DB_NAME)
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}