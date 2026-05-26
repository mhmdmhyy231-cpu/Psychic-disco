package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.ui.AdventureGameScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AdventureViewModel
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun testAdventureGameScreenRendersWithoutCrash() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AdventureViewModel(application)
    
    composeTestRule.setContent {
      MyApplicationTheme {
        AdventureGameScreen(viewModel = viewModel)
      }
    }
    
    composeTestRule.waitForIdle()
    assertNotNull(composeTestRule.onRoot())
  }
}

