package com.sishijiuhack.iflow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class MainActivitySmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesAndShowsHomeNavigation() {
        composeRule.onNodeWithText("本月概览").assertIsDisplayed()
        composeRule.onNodeWithText("记一笔").assertIsDisplayed()
        composeRule.onNodeWithText("首页").assertIsDisplayed()
        composeRule.onNodeWithText("流水").assertIsDisplayed()
        composeRule.onNodeWithText("统计").assertIsDisplayed()
        composeRule.onNodeWithText("设置").assertIsDisplayed()
    }
}
