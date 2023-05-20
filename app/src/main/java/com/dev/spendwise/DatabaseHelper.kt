package com.dev.spendwise

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_DB_QUERY =
            "CREATE TABLE " + DATABASE_TABLE + "(" + USER_ID + " INTEGER ," + CATEGORY + " TEXT)"
        db.execSQL(CREATE_DB_QUERY)
        val query =
            "INSERT INTO USERS (_ID,Category) VALUES (1, 'food'), (1, 'maintenance'), (1, 'education'),(1, 'entertainment'), (1, 'fuel'), (1, 'grocery'),(1, 'health'), (1, 'mobile recharge'), (1, 'rent'),(1, 'stationary'), (1, 'travel'), (1, 'shopping'),(1, 'income salary'), (1, 'income cash')"
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE)
        onCreate(db)
    }

    fun insertLabel(label: String?) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(CATEGORY, label)
        db.insert(DATABASE_TABLE, null, values)
        db.close()
    }

    val allLabels: List<String>
        get() {
            val list: MutableList<String> = ArrayList()
            val selectQuery = "SELECT * FROM " + DATABASE_TABLE + " ORDER BY Category ASC"
            val db = this.readableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursor.getString(1))
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            return list
        }

    fun deleteLabel(label: String) {
        val db = this.writableDatabase
        db.delete(DATABASE_TABLE, CATEGORY + " = ?", arrayOf(label))
        db.close()
    }

    companion object {
        const val DATABASE_NAME = "MY_COMPANY.DB"
        const val DATABASE_VERSION = 1
        const val DATABASE_TABLE = "USERS"
        const val USER_ID = "_ID"
        const val CATEGORY = "Category"
    }
}