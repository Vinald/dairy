package vinald.me.dairy.di

import android.content.Context
import vinald.me.dairy.data.DiaryDatabase
import vinald.me.dairy.data.DiaryRepository

/** Manual dependency graph, owned by [vinald.me.dairy.DiaryApplication]. */
class AppContainer(context: Context) {

    private val database by lazy { DiaryDatabase.build(context) }

    val diaryRepository by lazy { DiaryRepository(database.diaryDao()) }
}
