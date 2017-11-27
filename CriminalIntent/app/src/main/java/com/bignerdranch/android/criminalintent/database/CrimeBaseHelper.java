package com.bignerdranch.android.criminalintent.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable;

/**
 * Created by james_huker on 8/24/17.
 * 这是数据库文档
 */

public class CrimeBaseHelper extends SQLiteOpenHelper{
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "crimeBase.db";

    public CrimeBaseHelper(Context context) {
        super(context , DATABASE_NAME , null , VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + CrimeTable.NAME +"("
                +/** 此处的引号和_id之间有一个空格，如果省略了就会出现莫名其妙的错误，但是至今不知道为何？*/
                " _id integer primary key autoincrement, "
                + CrimeTable.Cols.UUID + ", "
                + CrimeTable.Cols.TITLE + ", "
                + CrimeTable.Cols.DATE + ", "
                + CrimeTable.Cols.SOLVED +", "
                + CrimeTable.Cols.SUSPECT + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db , int oldVersion ,int newVersion) {

    }
}
