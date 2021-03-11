package com.app.githubuserrepo.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.githubuserrepo.model.Notes
import com.app.githubuserrepo.model.ProfileResponse
import com.app.githubuserrepo.model.UserListResponse
import com.app.githubuserrepo.model.UserListResponseItem

@Database(entities = [UserListResponseItem::class,Notes::class],version = 1 ,exportSchema = false)
abstract class AppDb : RoomDatabase() {


    companion object {
        @Volatile private var instance: AppDb? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            AppDb::class.java, "UserDB.db")

            .allowMainThreadQueries()
            .build()
            .also {
                instance = it
            }


    }

    abstract fun noteDao(): NotesDAO

    abstract fun userDao() : UserDAO


}