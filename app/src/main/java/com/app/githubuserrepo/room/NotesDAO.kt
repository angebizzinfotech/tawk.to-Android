package com.app.githubuserrepo.room

import androidx.room.*
import com.app.githubuserrepo.model.Notes


@Dao
interface NotesDAO {


    //Song data will be saved in Database.... Only Audio Image , Half Audio ,Full Audio , Quarter Audio will be save seperately
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveNote(notes: Notes)

    @Transaction
    fun getSongTransaction() {
        // Anything inside this method runs in a single transaction.
        getAllNotes()
    }


    @Query("UPDATE Notes SET notes_text = :note_text  WHERE user_id = :id")
    fun updateNote(
        id:String ,
        note_text: String,
    )

    @Query("DELETE FROM Notes WHERE user_id = :id")
    fun deleteNote(id: String)


    @Query(value = "Select * from Notes WHERE user_id LIKE :id")
    fun getNote(id: String) : Notes

    @Transaction @Query(value = "Select * from Notes")
    fun getAllNotes() : List<Notes>


    @Query("DELETE FROM notes")
    fun deleteAllNotesFromTable()
}