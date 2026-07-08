package com.sishijiuhack.iflow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
        composeRule.onNodeWithText("现在").assertIsDisplayed()
        composeRule.onNodeWithText("保存").assertIsDisplayed()
    }

    @Test
    fun homePendingEntryOpensPendingList() {
        composeRule.onNodeWithText("待确认 0 笔").performClick()

        composeRule.onNodeWithText("待确认").assertIsDisplayed()
        composeRule.onNodeWithText("返回").assertIsDisplayed()
        composeRule.onNodeWithText("没有待确认记录。").assertIsDisplayed()
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
    fun statsTabShowsSummaryAndBars() {
        composeRule.onNodeWithText("统计").performClick()

        composeRule.onNodeWithText("今日支出").assertIsDisplayed()
        composeRule.onNodeWithText("本月收支对比").assertIsDisplayed()
        composeRule.onNodeWithText("分类支出排行").assertIsDisplayed()
    }

    @Test
    fun settingsTabShowsExportActions() {
        composeRule.onNodeWithText("设置").performClick()

        composeRule.onNodeWithText("本地导出").assertIsDisplayed()
        composeRule.onNodeWithText("导出 JSON").assertIsDisplayed()
        composeRule.onNodeWithText("导出 CSV").assertIsDisplayed()
        composeRule.onNodeWithText("通知规则").assertIsDisplayed()
    }

    @Test
    fun settingsTabShowsPermissionAndPrivacyGuidance() {
        composeRule.onNodeWithText("设置").performClick()

        composeRule.onNodeWithText("通知读取：", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("前往系统设置开启").assertIsDisplayed()
        composeRule.onNodeWithText("HyperOS 入口可能随版本变化，请以系统设置页面为准。").assertIsDisplayed()
        composeRule.onNodeWithText("隐私说明").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("账本、通知解析结果和设置默认仅保存在本机。本应用不上传服务器，不接入第三方统计 SDK，不采集联系人、定位或相册。")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
