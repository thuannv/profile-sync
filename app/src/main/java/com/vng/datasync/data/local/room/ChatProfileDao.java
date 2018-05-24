package com.vng.datasync.data.local.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.vng.datasync.data.model.roomdb.ChatProfileDBO;

import java.util.List;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 23/04/2018
 */

@Dao
public interface ChatProfileDao {

    @Query("SELECT * " +
            "FROM chat_profiles")
    List<ChatProfileDBO> getAllProfiles();

    @Insert
    long insert(ChatProfileDBO dbo);
}
