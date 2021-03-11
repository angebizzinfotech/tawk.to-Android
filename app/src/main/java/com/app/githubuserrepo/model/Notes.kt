package com.app.githubuserrepo.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity

class Notes {

    @ColumnInfo (name = "user_id" )
    @PrimaryKey(autoGenerate = false)
    var user_id: String=""

    /**
     * when user listens a quarter , half or full audio then this column will be true
     */
    @ColumnInfo (name = "notes_text")
    var notes_text: String=""


}