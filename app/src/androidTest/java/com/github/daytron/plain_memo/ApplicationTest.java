package com.github.daytron.plain_memo;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;


/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends InstrumentationTestCase {

    private static final String APP_PACKAGE = NoteListActivity.class.getPackage().getName();

    private static final long LAUNCH_TIMEOUT = 10000;
    private static final long UI_TIMEOUT = 15000;

    private UiDevice mDevice;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Start from the home screen
        mDevice.pressHome();

        // Launch app via Intent
        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(APP_PACKAGE);
        // Clear out any previous instances
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), LAUNCH_TIMEOUT);
    }

    public void testAddNewNoteViaFabButton() throws Exception {
        // Given
        String expResultMenuSaveDesc = "Save";

        // When
        // Locate and click the add new note fab button
        UiObject2 fabButton = mDevice.findObject(By.res(APP_PACKAGE, "fab_add"));
        fabButton.click();

        mDevice.wait(Until.hasObject(By.text("New Note")), UI_TIMEOUT);

        // Then
        // Get text of the desc content of save menu item in the toolbar as reference
        // to verify that the NoteEditFragment is displayed in the screen
        UiObject2 menuSaveInToolbar = mDevice.findObject(By.desc("Save"));
        String titleText = menuSaveInToolbar.getContentDescription();

        assertEquals(expResultMenuSaveDesc, titleText);

        // Cleanup
        // Delete the newly created note
        mDevice.pressBack();

        // Exit
        mDevice.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), UI_TIMEOUT);
        mDevice.pressBack();
    }

    public void testAddNewNoteViaOptionsMenu() {
        // Given
        String expResultMenuSaveDesc = "Save";

        // When
        // Locate Options Menu and tap New Note menu item
        UiObject2 moreOptions = mDevice.findObject(By.desc("More options"));
        moreOptions.click();

        mDevice.wait(Until.hasObject(By.text("New Note")), UI_TIMEOUT);

        UiObject2 newNoteMenuItem = mDevice.findObject(By.text("New Note"));
        newNoteMenuItem.click();

        mDevice.wait(Until.hasObject(By.desc("Save")), UI_TIMEOUT);

        // Then
        // Get text of the desc content of save menu item in the toolbar as reference
        // to verify that the NoteEditFragment is displayed in the screen
        UiObject2 menuSaveInToolbar = mDevice.findObject(By.desc("Save"));
        String titleText = menuSaveInToolbar.getContentDescription();

        assertEquals(expResultMenuSaveDesc, titleText);

        // Cleanup
        // Delete the newly created note
        mDevice.pressBack();

        // Exit
        mDevice.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), UI_TIMEOUT);
        mDevice.pressBack();
    }
}