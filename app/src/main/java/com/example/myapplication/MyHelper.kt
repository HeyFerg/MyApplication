package com.example.myapplication

import android.database.Cursor
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


data class Song(val id: String, val t: String, val a: String, val y: Long)

data class Steps(val id: String, val day: String, val steps: Long)

class MyHelper(ctx: Context) : SQLiteOpenHelper(ctx, "Exercise", null, 1){

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL("CREATE TABLE IF NOT EXISTS Steps (Id INT PRIMARY KEY, Day VARCHAR, Steps LONG)")
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


    fun searchSteps(Day: String) : Steps? {

        val db = readableDatabase
        val cursor = db.rawQuery ("SELECT * FROM Steps WHERE Day=? AND Steps=?", arrayOf<String>("$Day"))

        if(cursor.moveToFirst()){

            val s = Steps(cursor.getString(cursor.getColumnIndex("ID:")),
                    cursor.getString(cursor.getColumnIndex("Day:")), cursor.getLong(cursor.getColumnIndex("Steps:")))
            cursor.close()
            return s
        }
        cursor.close()
        return null
    }

    fun updateRecord(id: String, Day: String, Steps: Long): Int{

        val db = writableDatabase
        val stmt = db.compileStatement("UPDATE Steps SET Day=?, Steps=?")
        stmt.bindString(1, Day)
        stmt.bindLong(2, Steps)
        val nAffectedRows = stmt.executeUpdateDelete()
        return nAffectedRows
    }

    fun deleteRecord(Day: String, Steps: Long): Int{

        val db = writableDatabase
        val stmt = db.compileStatement("DELETE FROM Steps WHERE Day=? AND Steps=?")
        stmt.bindString(1, Day)
        stmt.bindLong(2, Steps)

        val nAffectedRows = stmt.executeUpdateDelete()
        return nAffectedRows

    }
}