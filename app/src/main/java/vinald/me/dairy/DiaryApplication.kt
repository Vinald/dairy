package vinald.me.dairy

import android.app.Application
import vinald.me.dairy.di.AppContainer

class DiaryApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
