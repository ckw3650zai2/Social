package com.example.social.listeners;

import com.example.social.entities.Note;

public interface NotesListener {

    void onNoteClicked(Note note, int position);
}
