package com.example.social.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import com.example.social.entities.Note;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes WHERE User_Id= :id")
    List<Note> getAllNotes(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Delete
    void deleteNote(Note note);
}
