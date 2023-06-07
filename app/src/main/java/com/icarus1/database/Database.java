package com.icarus1.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.icarus1.views.TimeZonePicker;

import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

public class Database {

    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase db;

    private final String[] paths = new String[]{
        "database/create_time_zones.sql",
        "database/insert_time_zone.sql"
    };
    private final String[] sqlCommands = new String[paths.length];

    public Database(Context c) {

        context = c;

        int i = 0;
        for (String path : paths) {
            String string = "";
            try (
                InputStream inputStream = context.getAssets().open(path)
            ) {
                int size = inputStream.available();
                byte[] buffer = new byte[size];
                inputStream.read(buffer);
                string = new String(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            sqlCommands[i] = string;
            i++;
        }

    }

    public Database open() throws SQLException {

        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();

        if (getTimeZones("").length == 0) {
            for (String id : TimeZone.getAvailableIDs()) {
                insertTimeZone(TimeZone.getTimeZone(id));
            }
        }

        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public String[] getTimeZones(String search) {

        Cursor cursor;
        if (search.trim().equals("")) {
            cursor = db.rawQuery("SELECT * FROM TimeZones", null);
        } else {
            cursor = db.rawQuery("SELECT * FROM TimeZones WHERE ID LIKE '%' || ? || '%';", new String[]{search});
        }

        String[] result = new String[cursor.getCount()];
        if (!cursor.moveToFirst()) {
            return result;
        }

        int i = 0;
        do {
            result[i] = cursor.getString(0);
            i++;
        } while(cursor.moveToNext());

        cursor.close();

        return result;

    }

    private void insertTimeZone(TimeZone timeZone) {
        db.execSQL(
            sqlCommands[1],
            new String[]{
                timeZone.getID(),
                String.valueOf(timeZone.getRawOffset()),
                String.valueOf(timeZone.getDSTSavings())
            }
        );
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        static final int DB_VERSION = 1;

        public DatabaseHelper(Context context) {
            super(context, "dbtest.DB", null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(sqlCommands[0]);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + "dbtest.DB");
            onCreate(db);
        }
    }

}
