package com.example.mynewsapp.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Stock::class, InvestHistory::class, FollowingList::class],
    version = 3,
    autoMigrations = [AutoMigration (from = 2, to = 3, spec = StockDatabase.MyExampleAutoMigration::class)],
    exportSchema = true
)
abstract class StockDatabase:RoomDatabase() {

    abstract fun stockDao(): StockDao

    companion object {
        @Volatile
        private var INSTANCE: StockDatabase? = null

        fun getDatabase(context: Context): StockDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockDatabase::class.java,
                    "stock_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }


    @RenameTable(fromTableName = "stockList", toTableName = "stocks")
    class MyExampleAutoMigration : AutoMigrationSpec {
        @Override
        override fun onPostMigrate(db: SupportSQLiteDatabase) {
            // Invoked once auto migration is done
        }
    }
}