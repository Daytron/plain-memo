package com.github.daytron.plain_memo.database;

/**
 * Created by ryan on 27/10/15.
 */
public class NoteDbSchema {

    public static final class NoteTable {
        public static final String NAME = "notes";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String BODY = "body";
            public static final String DATE = "date";
        }
    }
}
