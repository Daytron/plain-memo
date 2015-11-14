package com.github.daytron.plain_memo.suite;

import com.github.daytron.plain_memo.unit.NoteTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runs all unit tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        NoteTest.class})
public class UnitTestSuite {
}
