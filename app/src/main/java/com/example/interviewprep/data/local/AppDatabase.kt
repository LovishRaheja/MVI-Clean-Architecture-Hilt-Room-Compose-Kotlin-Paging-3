package com.example.interviewprep.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.interviewprep.data.local.dao.UserDao
import com.example.interviewprep.data.local.entity.UserEntity

/**
 * Room Database
 * 
 * Key annotations:
 * @Database - marks this as Room database
 * entities - list of all entities
 * version - for migrations
 * exportSchema - set false for simplicity (true in production)
 */
@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
