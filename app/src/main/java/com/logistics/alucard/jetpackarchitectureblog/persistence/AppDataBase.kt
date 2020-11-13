package com.logistics.alucard.jetpackarchitectureblog.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.logistics.alucard.jetpackarchitectureblog.models.AccountProperties
import com.logistics.alucard.jetpackarchitectureblog.models.AuthToken

@Database(entities = [AuthToken::class, AccountProperties::class], version = 1)
abstract class AppDataBase: RoomDatabase() {

    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    companion object {
        const val  DATABASE_NAME = "app_db"
    }

}