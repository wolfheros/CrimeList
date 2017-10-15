package com.bignerdranch.android.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bignerdranch.android.criminalintent.database.CrimeBaseHelper;
import com.bignerdranch.android.criminalintent.database.CrimeCursorWrapper;
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema;
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable;

/**
 * Created by james_huker on 8/6/17.
 */

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    /**
     * 单例类创建，1、设置一个static的类对象引用。
     *           2、首先判断引用是否为空，是，用私有构造器创建新的对象，返回给调用者同时，赋给static引用。
     *           3、如果已经存在static引用的对象，则直接返回此引用下的对象。
     * */
    public static CrimeLab get (Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    public void addCrime(Crime c) {
        ContentValues values = getContentValues(c);
        mDatabase.insert(CrimeTable.NAME , null ,values);
    }

    // 私有构造器
    private CrimeLab(Context context) {
        /*此处使用的是ApplicationContext 而非ActivityContext 原因是：CrimeLab 是一个单例，一旦创建就会一直存在直至整个应用进程被销毁。*/
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
        // 创建List数组为了保存Crime对象。
    }
    // 返回所有的crime 组成数组。方法是从数据库中直接获取。
    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();
        /*
        * 匹配条件为null , null 结果就是获取所有的行数据。即所有的保存在数据库中的crime对象。
        * */
        CrimeCursorWrapper cursor = queryCrimes(null , null);
        try {
            cursor.moveToFirst();
            /*判断cursor 表指针是不是指向最后一个row 之后，采用循环的方式，
            * 循环取出里面包含的crime 实例*/
            while (!cursor.isAfterLast()) {
                // 读取数据库获得cursor 然后再调用getCrime() 方法获取crime实例，并添加到crimes数组中。
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }

        return crimes;
    }

    /*此处的方法为了返回指定ID 的Crime对象。不同以往的直接在Crime
    List中获取制定crime 的方法。这里是直接在数据库中进行查询。*/
    public Crime getCrime(UUID id) {
        CrimeCursorWrapper cursor = queryCrimes(CrimeTable.Cols.UUID + " = ?" , new String[] {id.toString()});

        try {
            /*如果没有匹配上任何一行的数据，则返回是null。getCount()
            * 返回匹配的多少行数据库。数量*/
            if (cursor.getCount() == 0) {
                return null;
            }
            /*这里只匹配了一行数据，所以不需要进行遍历*/
            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);
        mDatabase.update(CrimeTable.NAME ,values ,CrimeTable.Cols.UUID + " = ?"  , new String[] {uuidString});
    }
    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID , crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE , crime.getTitle());
        values.put(CrimeTable.Cols.DATE , crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED , crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT , crime.getSuspect());

        return values;
    }

    // 此方法的作用是返回一个CrimeCursorWrapper 实例
    private CrimeCursorWrapper queryCrimes(String whereClause , String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null);
        return new CrimeCursorWrapper(cursor);
    }

    // 定位图片文件
    public File getPhotoFile(Crime crime) {
        File externalFilesDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (externalFilesDir == null) {
            return null;
        }
        return new File(externalFilesDir,crime.getPhotosFilename());
    }
}
