package org.altbeacon.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;


import org.altbeacon.beacon.iBeaconData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBManager {
    private Context mContext;
    private Set<String> macSet;
    /*选择题的集合*/
    public List<iBeaconData> mBeanLists = new ArrayList<>();

    public DBManager(Context mContext) {
        this.mContext = mContext;
    }

    //把assets目录下的db文件复制到dbpath下
    public SQLiteDatabase manage(String dbName) {
        String dbPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/databases/" + dbName;
        if (new File(dbPath).exists()) {
            File file = new File(dbPath);
            file.delete();
        }
        if (!new File(dbPath).exists()) {
            try {
                boolean flag = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/databases/").mkdirs();
                boolean newFile = new File(dbPath).createNewFile();
                try {
                    FileOutputStream out = new FileOutputStream(dbPath);
                    InputStream in = mContext.getAssets().open(dbName);
                    byte[] buffer = new byte[1024];
                    int readBytes = 0;
                    while ((readBytes = in.read(buffer)) != -1)
                        out.write(buffer, 0, readBytes);
                    in.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return SQLiteDatabase.openOrCreateDatabase(dbPath, null);
    }

    //查询选择题
    public List<iBeaconData> query(SQLiteDatabase sqliteDB, String tableName, String[] columns, String selection, String[] selectionArgs) {
        iBeaconData beaconData = null;
        try {
            macSet = new HashSet<>();
            Cursor cursor = sqliteDB.query(tableName, columns, selection, selectionArgs, null, null, null);
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("Name"));
                String mac = cursor.getString(cursor.getColumnIndex("Mac"));
                if (!macSet.contains(mac)) {
                    macSet.add(mac);
                    beaconData = new iBeaconData(name, mac);
                    mBeanLists.add(beaconData);
                }
            }
            cursor.close();
            return mBeanLists;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 查询指纹库
    public Map<double[], Map<String, Double>> queryFinger(SQLiteDatabase sqliteDB, String table, String[] columns, String selection, String[] selectionArgs) {
        Map<double[], Map<String, Double>> fingerMap = null;
        Cursor cursor = null;
        try {
            cursor = sqliteDB.query(table, null, selection, selectionArgs, null, null, null);
            fingerMap = new HashMap<>();
            while (cursor.moveToNext()) {
                double x = cursor.getDouble(cursor.getColumnIndex("x"));
                double y = cursor.getDouble(cursor.getColumnIndex("y"));
                Map<String, Double> map = new HashMap<>();
                for (String uuid : columns) {
                    map.put(uuid, cursor.getDouble(cursor.getColumnIndex(uuid)));
                }
                fingerMap.put(new double[]{x, y}, map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            return fingerMap;
        }
    }
}
