package com.vng.datasync.data.model.roomdb;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 23/04/2018
 */

@Entity(tableName = "chat_profiles",
        indices = @Index("user_id"))
//@Entity(tableName = "chat_profiles")
public class ChatProfileDBO {

    @PrimaryKey
    @ColumnInfo(name = "user_id")
    public Integer userId;

    @ColumnInfo(name = "display_name")
    public String displayName;

    @ColumnInfo(name = "avatar")
    public String avatar;

    @ColumnInfo(name = "birthday")
    public Integer birthday;

    @ColumnInfo(name = "gender")
    public Integer gender;

    @ColumnInfo(name = "is_follow")
    public Integer isFollow;

    @ColumnInfo(name = "user_name")
    public String userName;

    @ColumnInfo(name = "phone_number")
    public Integer phoneNumber;

    @ColumnInfo(name = "state")
    public Integer state;

    @ColumnInfo(name = "last_update")
    public Long lastUpdateTime;

    @ColumnInfo(name = "is_blocked")
    public Integer isBlocked;

    @ColumnInfo(name = "verified")
    public Integer isVerified;
}
