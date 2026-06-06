package database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class HistoryItem(
    val id: Int,
    val sender: String,
    val message: String,
    val eventTag: String,
    val timestamp: Long
)

class HistoryDatabase(
    context: Context
) : SQLiteOpenHelper(context, "txmax_history.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sender TEXT NOT NULL,
                message TEXT NOT NULL,
                event_tag TEXT DEFAULT 'General',
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE history ADD COLUMN sender TEXT DEFAULT 'Unknown'")
            db.execSQL("ALTER TABLE history ADD COLUMN event_tag TEXT DEFAULT 'General'")

            db.execSQL("UPDATE history SET sender = 'User' WHERE message LIKE 'User:%'")
            db.execSQL("UPDATE history SET sender = 'TX Max' WHERE message LIKE 'TX Max:%'")
        }
    }

    fun insertMessage(sender: String, message: String, eventTag: String = "General") {
        val values = ContentValues().apply {
            put("sender", sender)
            put("message", message)
            put("event_tag", eventTag)
            put("timestamp", System.currentTimeMillis())
        }

        writableDatabase.insert("history", null, values)
    }

    fun getMessages(): List<HistoryItem> {
        val messages = mutableListOf<HistoryItem>()

        readableDatabase.rawQuery(
            "SELECT id, sender, message, event_tag, timestamp FROM history ORDER BY id DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                messages.add(
                    HistoryItem(
                        id = cursor.getInt(0),
                        sender = cursor.getString(1),
                        message = cursor.getString(2),
                        eventTag = cursor.getString(3),
                        timestamp = cursor.getLong(4)
                    )
                )
            }
        }

        return messages
    }

    fun clearAllHistory() {
        writableDatabase.delete("history", null, null)
    }
}