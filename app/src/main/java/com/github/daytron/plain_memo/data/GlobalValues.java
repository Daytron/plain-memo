package com.github.daytron.plain_memo.data;

/**
 * Class for providing global data at runtime where Android xml resources are not applicable.
 */
public class GlobalValues {

    private GlobalValues(){}

    public static final int FONT_SIZE_DEFAULT = 18;
    public static final float FONT_SIZE_DIFFERENCE = 4.0F;
    public static final String WELCOME_TITLE = "Welcome to Plain Memo!";
    public static final String WELCOME_BODY = "A simple note app for all your text storage needs."
            + "\n\nPlease feel free to submit any issues or questions "
            + "you might have via Send Feedback section in Settings > General."
            + "\n\nFEATURES:"
            + "\n1. Create, view, edit, and delete notes."
            + "\n2. Search and filter notes easily."
            + "\n3. Single touch edit. While in Note view screen, simply "
            + "tap on the location of the note text you would like to edit."
            + "\n4. Change font size."
            + "\n5. Support for large screen or tablet devices."
            + "\n6. Share your notes to your favourite apps and vice versa."
            + "\n\nI hope you enjoy using Plain Memo.";

    public static final String ChangeLogTitle = "Change Log";
    public static final String ChangeLogMsg = "\nPlainMemo 1.0.0 (November 17 2015)" +
            "\n- First Production Release." +
            "\n\nPlainMemo 1.0.1 (March 14 2016)" +
            "\n- Fixed date in the about section.";
}
