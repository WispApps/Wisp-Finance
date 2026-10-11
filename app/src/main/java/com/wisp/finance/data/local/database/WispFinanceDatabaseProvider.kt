package com.wisp.finance.data.local.database

import android.content.Context
import androidx.room.Room
import com.wisp.finance.data.local.seed.DefaultCategorySeeder

object WispFinanceDatabaseProvider {
    private const val DATABASE_NAME = "wisp-finance.db"

    @Volatile
    private var instance: WispFinanceDatabase? = null

    fun get(context: Context): WispFinanceDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                WispFinanceDatabase::class.java,
                DATABASE_NAME,
            )
                .addMigrations(*DatabaseMigrations.ALL)
                .build()
                .also { instance = it }
        }
    }

    suspend fun initialize(context: Context) {
        val database = get(context)
        DefaultCategorySeeder(database.categoryDao()).seedIfNeeded()
    }
}
