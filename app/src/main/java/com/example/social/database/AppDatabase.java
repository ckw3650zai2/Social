package com.example.social.database;

import android.content.Context;
import android.provider.ContactsContract;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.social.dao.NoteDao;
import com.example.social.entities.Note;

@Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase appDatabase;

    public static synchronized AppDatabase getNotesDatabase(Context context) {
        if(appDatabase == null){
            appDatabase = Room.databaseBuilder(
                    context,
                    AppDatabase.class,
                    "social_db"
            ).build();
        }
        return appDatabase;

    }
    public abstract NoteDao noteDao();

}
