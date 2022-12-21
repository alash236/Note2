package com.example.note.listeners;

import com.example.note.entites.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
