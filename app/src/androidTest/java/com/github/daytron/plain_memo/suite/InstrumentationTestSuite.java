package com.github.daytron.plain_memo.suite;

import com.github.daytron.plain_memo.ui.AddNewNoteEspressoTest;
import com.github.daytron.plain_memo.ui.NoteListFragmentEspressoTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runs all Instrumentation tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AddNewNoteEspressoTest.class,
        NoteListFragmentEspressoTest.class})
public class InstrumentationTestSuite {
}
