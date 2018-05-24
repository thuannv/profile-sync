package com.vng.datasync.data.local.room;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.RoomDatabase;

import com.vng.datasync.data.model.roomdb.ChatConversationDBO;
import com.vng.datasync.data.model.roomdb.ChatMessageDBO;
import com.vng.datasync.data.model.roomdb.ChatProfileDBO;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 23/04/2018
 */

@Database(entities = {ChatConversationDBO.class, ChatMessageDBO.class, ChatProfileDBO.class},
        version = 2)
public abstract class UserDatabase extends RoomDatabase {

    public abstract ChatConversationDao getChatConversationDAO();

    public abstract ChatMessageDao getChatMessageDAO();

    public abstract ChatProfileDao getChatProfileDAO();

    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }
}
