package com.example.note;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.note.adapters.NotesAdapter;
import com.example.note.database.NotesDatabase;
import com.example.note.entites.Note;
import com.example.note.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE=1;
    public static final int REQUEST_CODE_UPDATE_NOTE=2;
    public static final int REQUEST_CODE_SHOW_NOTES=3;

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private int noteClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(v -> {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            });

        notesRecyclerView=findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteList=new ArrayList<>();
        notesAdapter=new NotesAdapter(noteList,this);
        notesRecyclerView.setAdapter(notesAdapter);
        getNotes(REQUEST_CODE_SHOW_NOTES,false);

        EditText inputSerach = findViewById(R.id.inputSearch);
        inputSerach.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(noteList.size()!=0){
                    notesAdapter.searchNotes(s.toString());
                }
            }
        });
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition=position;
        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note",note);
        startActivityForResult(intent,REQUEST_CODE_UPDATE_NOTE);

    }

    private void getNotes( final int requestCode,final boolean isNoteDeleted){

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void,Void, List<Note>>{
            @Override
            protected List<Note>doInBackground(Void...voids){
                return NotesDatabase
                        .getDatabase(getApplicationContext())
                        .noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes){
                super.onPostExecute(notes);
                if(requestCode==REQUEST_CODE_SHOW_NOTES){
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                } if(requestCode==REQUEST_CODE_ADD_NOTE){
                    noteList.add(0,notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                }if(requestCode==REQUEST_CODE_UPDATE_NOTE){
                    noteList.remove(noteClickedPosition);
                    if(isNoteDeleted){
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    }else{
                        noteList.add(noteClickedPosition,notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }
            }
        }
        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_CODE_ADD_NOTE && resultCode==RESULT_OK){
            getNotes(REQUEST_CODE_ADD_NOTE,false);
        }else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode==RESULT_OK){
            if(data!=null){
                getNotes(REQUEST_CODE_UPDATE_NOTE,data.getBooleanExtra("isNoteDeleted",false));
            }
        }
    }
}