package vinald.me.dairy.di

import android.content.Context
import vinald.me.dairy.data.DiaryDatabase
import vinald.me.dairy.data.DiaryRepository
import vinald.me.dairy.data.PhotoStorage
import vinald.me.dairy.security.LockManager
import vinald.me.dairy.security.PinManager

/** Manual dependency graph, owned by [vinald.me.dairy.DiaryApplication]. */
class AppContainer(context: Context) {

    private val database by lazy { DiaryDatabase.build(context) }

    private val photoStorage by lazy { PhotoStorage(context) }

    val diaryRepository by lazy { DiaryRepository(database.diaryDao(), photoStorage) }

    val pinManager by lazy { PinManager(context) }

    val lockManager by lazy { LockManager(pinManager) }
}
