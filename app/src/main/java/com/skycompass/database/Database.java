package com.skycompass.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.TimeZone;

public class Database {

    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase db;

    private final String[] paths = new String[]{
        "database/create_time_zones.sql",
        "database/insert_time_zone.sql",
        "database/drop_time_zones.sql",
        "database/select_time_zones.sql"
    };
    private final String[] sqlCommands = new String[paths.length];

    public Database(@NonNull Context c) {

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

        if (searchTimeZones("").length == 0) {
            for (String id : ZoneId.getAvailableZoneIds()) {
                insertTimeZone(id);
            }
        }

        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public String[] searchTimeZones(@NonNull String search) {

        Cursor cursor;
        cursor = db.rawQuery(sqlCommands[3], new String[]{search});

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

    private void insertTimeZone(@NonNull String id) {
        db.execSQL(
            sqlCommands[1],
            new String[]{
                id,
                ZoneId.of(id).getDisplayName(TextStyle.FULL, Locale.getDefault())
            }
        );
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        private static final int DB_VERSION = 1;
        private static final String DB_NAME = "TimeZones";

        public DatabaseHelper(@NonNull Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(@NonNull SQLiteDatabase db) {
            db.execSQL(sqlCommands[0]);
        }

        @Override
        public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(sqlCommands[2]);
            onCreate(db);
        }
    }

}
