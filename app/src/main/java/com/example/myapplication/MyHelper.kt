package com.example.myapplication

import android.database.Cursor
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


data class Song(val id: String, val t: String, val a: String, val y: Long)

data class Steps(val Day: String, val Steps: Long)

class MyHelper(ctx: Context) : SQLiteOpenHelper(ctx, "Exercise", null, 1){

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL("CREATE TABLE IF NOT EXISTS Steps (ID INT PRIMARY KEY, Day VARCHAR, Steps LONG)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion:Int, newVersion:Int) {
        db.execSQL("DROP TABLE IF EXISTS Hits")
        onCreate(db)
    }

    fun addSteps(day:String, steps:Long) : Long{

        val db = writableDatabase
        val stmt = db.compileStatement ("INSERT INTO Steps(Day, Steps) VALUES (?,?)");
        stmt.bindString(1, day)
        stmt.bindLong(2, steps)
        val id = stmt.executeInsert()
        return id
    }


    fun searchSteps(Day: String) : List<Steps> {

        val db = readableDatabase
        val cursor = db.rawQuery ("SELECT * FROM Steps WHERE Day=?", arrayOf<String>(Day))
        val st = mutableListOf<Steps>()
        if(cursor.moveToFirst()){

            val s = Steps(cursor.getString(cursor.getColumnIndexOrThrow("Day")), cursor.getLong(cursor.getColumnIndexOrThrow("Steps")))
            cursor.close()
            st.add(s)
        }
        cursor.close()

        return st
    }

}