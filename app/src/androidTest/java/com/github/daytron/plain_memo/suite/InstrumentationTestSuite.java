package com.github.daytron.plain_memo.suite;

import com.github.daytron.plain_memo.ui.NewNoteEspressoTest;
import com.github.daytron.plain_memo.ui.NoteListFragmentEspressoTest;
import com.github.daytron.plain_memo.ui.OldNoteEspressoTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runs all Instrumentation tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        NewNoteEspressoTest.class,
        OldNoteEspressoTest.class,
        NoteListFragmentEspressoTest.class})
public class InstrumentationTestSuite {
}
