package com.hgr.authenticator.presentation.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.hgr.authenticator.domain.model.Account
import com.hgr.authenticator.presentation.theme.AuthenticatorTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysTitle() {
        composeTestRule.setContent {
            AuthenticatorTheme {
                HomeScreen(
                    onNavigateToAddAccount = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Authenticator").assertIsDisplayed()
    }

    @Test
    fun homeScreen_showsEmptyState_whenNoAccounts() {
        composeTestRule.setContent {
            AuthenticatorTheme {
                HomeScreen(
                    onNavigateToAddAccount = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithText("暂无账户").assertIsDisplayed()
        composeTestRule.onNodeWithText("点击右下角的 + 按钮添加您的第一个账户").assertIsDisplayed()
    }
}
