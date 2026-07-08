package com.sishijiuhack.iflow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

    @Test
    fun homeQuickEntryOpensTransactionForm() {
        composeRule.onNodeWithText("记一笔").performClick()

        composeRule.onNodeWithText("记一笔").assertIsDisplayed()
        composeRule.onNodeWithText("金额").assertIsDisplayed()
        composeRule.onNodeWithText("分类").assertIsDisplayed()
        composeRule.onNodeWithText("账户").assertIsDisplayed()
        composeRule.onNodeWithText("时间").assertIsDisplayed()
        composeRule.onNodeWithText("保存").assertIsDisplayed()
    }

    @Test
    fun ledgerTabShowsFilterControls() {
        composeRule.onNodeWithText("流水").performClick()

        composeRule.onNodeWithText("新增流水").assertIsDisplayed()
        composeRule.onNodeWithText("搜索").assertIsDisplayed()
        composeRule.onNodeWithText("全部日期").assertIsDisplayed()
        composeRule.onNodeWithText("全部账户").assertIsDisplayed()
        composeRule.onNodeWithText("全部分类").assertIsDisplayed()
    }

    @Test
    fun settingsTabShowsExportActions() {
        composeRule.onNodeWithText("设置").performClick()

        composeRule.onNodeWithText("本地导出").assertIsDisplayed()
        composeRule.onNodeWithText("导出 JSON").assertIsDisplayed()
        composeRule.onNodeWithText("导出 CSV").assertIsDisplayed()
        composeRule.onNodeWithText("通知规则").assertIsDisplayed()
    }
}
