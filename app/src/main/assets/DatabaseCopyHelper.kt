package com.melodyplayer.data

import android.content.Context
import java.io.File
import java.io.FileOutputStream

class DatabaseCopyHelper(private val context: Context) {

    private val dbName = "melody.db"
    private val dbPath = "${context.filesDir.path}/$dbName"

    fun createDatabaseIfNeeded() {
        val file = File(dbPath)
        if (!file.exists()) {
            copyDatabase()
        }
    }

    private fun copyDatabase() {
        context.assets.open(dbName).use { input ->
            FileOutputStream(dbPath).use { output ->
                input.copyTo(output)
            }
        }
    }

    fun getDatabasePath(): String = dbPath
}
