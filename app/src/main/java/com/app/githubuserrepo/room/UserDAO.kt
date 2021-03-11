package com.app.githubuserrepo.room

import androidx.room.*
import com.app.githubuserrepo.model.UserListResponseItem


@Dao
interface UserDAO {


    //Song data will be saved in Database.... Only Audio Image , Half Audio ,Full Audio , Quarter Audio will be save seperately
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUser(userListResponseItem: UserListResponseItem)

    @Transaction
    fun getSongTransaction() {
        // Anything inside this method runs in a single transaction.
        getAllUsers()
    }

    @Query("UPDATE UserListResponseItem SET avatar_url = :path  WHERE id = :id")
    fun updateUser(
        id:String ,
        path: String,
    )
    /*@Query(value = "Select * from UserListResponseItem WHERE login LIKE '%'+:querytext+'%'")*/
    @Query(value = "Select * from UserListResponseItem WHERE login LIKE +:querytext")
    fun getSearchUser(querytext:String) : List<UserListResponseItem>


    @Query(value = "Select * from UserListResponseItem WHERE id LIKE :user_id")
    fun getUser(user_id: String) : UserListResponseItem

    @Transaction @Query(value = "Select * from UserListResponseItem ORDER BY id ASC")
    fun getAllUsers() : List<UserListResponseItem>

    @Query(value = "select id from UserListResponseItem where id LIKE :user_id")
    fun getUserId(user_id: String) : String

    @Query("DELETE FROM UserListResponseItem WHERE id = :user_id")
    fun deleteUser(user_id: String)

    @Query("DELETE FROM UserListResponseItem")
    fun deleteAllFromTable()
}