package com.app.githubuserrepo.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.githubuserrepo.model.UserListResponseItem.CREATOR.TABLE_NAMES

@Entity(tableName = TABLE_NAMES)
data class UserListResponseItem(
    @PrimaryKey
    val id: String,
    val avatar_url: String?,
    val events_url: String?,
    val followers_url: String?,
    val following_url: String?,
    val gists_url: String?,
    val gravatar_id: String?,
    val html_url: String?,
    val login: String?,
    val node_id: String?,
    val organizations_url: String?,
    val received_events_url: String?,
    val repos_url: String?,
    val site_admin: Boolean?,
    val starred_url: String?,
    val subscriptions_url: String?,
    val type: String?,
    val url: String?
) :Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    constructor() : this("" , "" , "" , "" , "" , "" , "" , "",
     "" , "" , "" , "" , "" , false , "" , "" , "" ,"")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(avatar_url)
        parcel.writeString(events_url)
        parcel.writeString(followers_url)
        parcel.writeString(following_url)
        parcel.writeString(gists_url)
        parcel.writeString(gravatar_id)
        parcel.writeString(html_url)
        parcel.writeString(login)
        parcel.writeString(node_id)
        parcel.writeString(organizations_url)
        parcel.writeString(received_events_url)
        parcel.writeString(repos_url)
        parcel.writeValue(site_admin)
        parcel.writeString(starred_url)
        parcel.writeString(subscriptions_url)
        parcel.writeString(type)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserListResponseItem> {
        override fun createFromParcel(parcel: Parcel): UserListResponseItem {
            return UserListResponseItem(parcel)
        }

        override fun newArray(size: Int): Array<UserListResponseItem?> {
            return arrayOfNulls(size)
        }

        const val TABLE_NAMES = "UserListResponseItem"
    }
}