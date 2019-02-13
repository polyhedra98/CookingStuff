package com.mishenka.cookingstuff.utils.database

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.WorkerThread
import java.util.concurrent.TimeUnit

class PersistableParcelable<T: Parcelable>(private val mDb: CookingDatabase,
                                           private val mTimesource: TimeSource = PersistableParcelable.TimeSource()) {

    fun save(id: String, data: T) {
        save(id, data, TimeUnit.DAYS.toMillis(1))
    }

    @WorkerThread
    fun save(id: String, data: T, expirationDurationMillis: Long?) {
        val nowUtcTimeMillis = mTimesource.nowUtcMillias
        val expirationTimeMillis = if (expirationDurationMillis == null) null else nowUtcTimeMillis + expirationDurationMillis
        val parcelizedData = ParcelizedObject(data)
        val parcelized = Parcelized(id, nowUtcTimeMillis, expirationTimeMillis, parcelizedData.toBytes())
        mDb.parcelizedDao().saveParcelized(parcelized)
        parcelizedData.recycle()
    }

    @WorkerThread
    fun load(id: String, creator: Parcelable.Creator<T>): T? {
        val nowUtcMillis = mTimesource.nowUtcMillias
        mDb.parcelizedDao().deleteExpiredParcelized(nowUtcMillis)
        val parcelized = mDb.parcelizedDao().getParcelized(id) ?: return null
        val parcelizedObject = ParcelizedObject(parcelized.data)
        val obj = creator.createFromParcel(parcelizedObject.getParcel())
        parcelizedObject.recycle()
        return obj
    }

    class TimeSource {
        val nowUtcMillias: Long
            get() = System.currentTimeMillis()
    }

    private class ParcelizedObject {
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