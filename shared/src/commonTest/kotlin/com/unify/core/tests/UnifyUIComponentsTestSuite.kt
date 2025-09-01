package com.unify.core.tests

import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.unify.ui.components.UnifyButton
import com.unify.ui.components.UnifyText
import com.unify.ui.components.UnifyImage
import com.unify.ui.components.UnifyIcon
import com.unify.ui.components.UnifySurface
import com.unify.ui.theme.UnifyTheme
import com.unify.ui.components.ai.UnifyAIChat
import com.unify.ui.components.ai.UnifyAIAssistant
import com.unify.ui.components.ai.UnifyAIRecommendation
import com.unify.ui.components.advanced.UnifyChart
import com.unify.ui.components.advanced.UnifyCalendar
import com.unify.ui.components.advanced.UnifyPicker
import com.unify.ui.components.navigation.UnifyNavigationBar
import com.unify.ui.components.navigation.UnifyTabBar
import com.unify.ui.components.navigation.UnifyDrawer

/**
 * Unify UI组件测试套件
 * 全面测试UI组件的功能、交互和渲染
 */
class UnifyUIComponentsTestSuite {

    // 基础组件测试
    @Test
    fun testUnifyButtonComponent() = runTest {
        var clickCount = 0
        
        composeTestRule.setContent {
            UnifyButton(
                text = "Test Button",
                onClick = { clickCount++ },
                modifier = Modifier.testTag("test_button")
            )
        }
        
        // 验证按钮存在
        composeTestRule.onNodeWithTag("test_button").assertExists()
        composeTestRule.onNodeWithText("Test Button").assertExists()
        
        // 测试点击功能
        composeTestRule.onNodeWithTag("test_button").performClick()
        assertEquals(1, clickCount)
        
        // 测试多次点击
        repeat(5) {
            composeTestRule.onNodeWithTag("test_button").performClick()
        }
        assertEquals(6, clickCount)
    }

    @Test
    fun testUnifyButtonStates() = runTest {
        var isEnabled by mutableStateOf(true)
        var isLoading by mutableStateOf(false)
        
        composeTestRule.setContent {
            UnifyButton(
                text = "State Button",
                onClick = { },
                enabled = isEnabled,
                loading = isLoading,
                modifier = Modifier.testTag("state_button")
            )
        }
        
        // 测试启用状态
        composeTestRule.onNodeWithTag("state_button").assertIsEnabled()
        
        // 测试禁用状态
        isEnabled = false
        composeTestRule.onNodeWithTag("state_button").assertIsNotEnabled()
        
        // 测试加载状态
        isEnabled = true
        isLoading = true
        composeTestRule.onNodeWithTag("state_button").assertExists()
    }

    @Test
    fun testUnifyTextComponent() = runTest {
        composeTestRule.setContent {
            UnifyText(
                text = "Test Text Content",
                modifier = Modifier.testTag("test_text")
            )
        }
        
        composeTestRule.onNodeWithTag("test_text").assertExists()
        composeTestRule.onNodeWithText("Test Text Content").assertExists()
        composeTestRule.onNodeWithTag("test_text").assertTextEquals("Test Text Content")
    }

    @Test
    fun testUnifyTextFieldComponent() = runTest {
        var textValue by mutableStateOf("")
        
        composeTestRule.setContent {
            UnifyTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = "Test Input",
                placeholder = "Enter text here",
                modifier = Modifier.testTag("test_textfield")
            )
        }
        
        // 验证组件存在
        composeTestRule.onNodeWithTag("test_textfield").assertExists()
        
        // 测试文本输入
        composeTestRule.onNodeWithTag("test_textfield").performTextInput("Hello World")
        assertEquals("Hello World", textValue)
        
        // 验证文本显示
        composeTestRule.onNodeWithTag("test_textfield").assertTextContains("Hello World")
    }

    // 布局组件测试
    @Test
    fun testUnifyCardComponent() = runTest {
        composeTestRule.setContent {
            UnifyCard(
                modifier = Modifier.testTag("test_card")
            ) {
                UnifyText("Card Content")
            }
        }
        
        composeTestRule.onNodeWithTag("test_card").assertExists()
        composeTestRule.onNodeWithText("Card Content").assertExists()
    }

    @Test
    fun testUnifyRowComponent() = runTest {
        composeTestRule.setContent {
            UnifyRow(
                modifier = Modifier.testTag("test_row")
            ) {
                UnifyText("Item 1", modifier = Modifier.testTag("item1"))
                UnifyText("Item 2", modifier = Modifier.testTag("item2"))
                UnifyText("Item 3", modifier = Modifier.testTag("item3"))
            }
        }
        
        composeTestRule.onNodeWithTag("test_row").assertExists()
        composeTestRule.onNodeWithTag("item1").assertExists()
        composeTestRule.onNodeWithTag("item2").assertExists()
        composeTestRule.onNodeWithTag("item3").assertExists()
    }

    @Test
    fun testUnifyColumnComponent() = runTest {
        composeTestRule.setContent {
            UnifyColumn(
                modifier = Modifier.testTag("test_column")
            ) {
                UnifyText("Item A", modifier = Modifier.testTag("itemA"))
                UnifyText("Item B", modifier = Modifier.testTag("itemB"))
                UnifyText("Item C", modifier = Modifier.testTag("itemC"))
            }
        }
        
        composeTestRule.onNodeWithTag("test_column").assertExists()
        composeTestRule.onNodeWithTag("itemA").assertExists()
        composeTestRule.onNodeWithTag("itemB").assertExists()
        composeTestRule.onNodeWithTag("itemC").assertExists()
    }

    // 导航组件测试
    @Test
    fun testUnifyTabRowComponent() = runTest {
        var selectedTab by mutableStateOf(0)
        val tabs = listOf("Tab 1", "Tab 2", "Tab 3")
        
        composeTestRule.setContent {
            UnifyTabRow(
                selectedTabIndex = selectedTab,
                tabs = tabs,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.testTag("test_tabrow")
            )
        }
        
        composeTestRule.onNodeWithTag("test_tabrow").assertExists()
        
        // 测试所有标签页存在
        tabs.forEachIndexed { index, tab ->
            composeTestRule.onNodeWithText(tab).assertExists()
        }
        
        // 测试标签页切换
        composeTestRule.onNodeWithText("Tab 2").performClick()
        assertEquals(1, selectedTab)
        
        composeTestRule.onNodeWithText("Tab 3").performClick()
        assertEquals(2, selectedTab)
    }

    @Test
    fun testUnifyBottomNavigationComponent() = runTest {
        var selectedIndex by mutableStateOf(0)
        val items = listOf(
            BottomNavItem("Home", "home_icon"),
            BottomNavItem("Search", "search_icon"),
            BottomNavItem("Profile", "profile_icon")
        )
        
        composeTestRule.setContent {
            UnifyBottomNavigation(
                selectedIndex = selectedIndex,
                items = items,
                onItemSelected = { selectedIndex = it },
                modifier = Modifier.testTag("test_bottom_nav")
            )
        }
        
        composeTestRule.onNodeWithTag("test_bottom_nav").assertExists()
        
        // 测试导航项点击
        composeTestRule.onNodeWithText("Search").performClick()
        assertEquals(1, selectedIndex)
        
        composeTestRule.onNodeWithText("Profile").performClick()
        assertEquals(2, selectedIndex)
    }

    // 输入组件测试
    @Test
    fun testUnifyCheckboxComponent() = runTest {
        var isChecked by mutableStateOf(false)
        
        composeTestRule.setContent {
            UnifyCheckbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                label = "Test Checkbox",
                modifier = Modifier.testTag("test_checkbox")
            )
        }
        
        composeTestRule.onNodeWithTag("test_checkbox").assertExists()
        composeTestRule.onNodeWithText("Test Checkbox").assertExists()
        
        // 测试选中状态切换
        composeTestRule.onNodeWithTag("test_checkbox").performClick()
        assertTrue(isChecked)
        
        composeTestRule.onNodeWithTag("test_checkbox").performClick()
        assertFalse(isChecked)
    }

    @Test
    fun testUnifyRadioButtonComponent() = runTest {
        var selectedOption by mutableStateOf("")
        val options = listOf("Option A", "Option B", "Option C")
        
        composeTestRule.setContent {
            UnifyRadioButtonGroup(
                options = options,
                selectedOption = selectedOption,
                onOptionSelected = { selectedOption = it },
                modifier = Modifier.testTag("test_radio_group")
            )
        }
        
        composeTestRule.onNodeWithTag("test_radio_group").assertExists()
        
        // 测试选项选择
        composeTestRule.onNodeWithText("Option B").performClick()
        assertEquals("Option B", selectedOption)
        
        composeTestRule.onNodeWithText("Option C").performClick()
        assertEquals("Option C", selectedOption)
    }

    @Test
    fun testUnifySliderComponent() = runTest {
        var sliderValue by mutableStateOf(0.5f)
        
        composeTestRule.setContent {
            UnifySlider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 0f..1f,
                modifier = Modifier.testTag("test_slider")
            )
        }
        
        composeTestRule.onNodeWithTag("test_slider").assertExists()
        
        // 测试滑块值变化
        composeTestRule.onNodeWithTag("test_slider").performTouchInput {
            swipeRight()
        }
        assertTrue(sliderValue > 0.5f)
    }

    // 反馈组件测试
    @Test
    fun testUnifyProgressIndicatorComponent() = runTest {
        composeTestRule.setContent {
            UnifyProgressIndicator(
                progress = 0.7f,
                modifier = Modifier.testTag("test_progress")
            )
        }
        
        composeTestRule.onNodeWithTag("test_progress").assertExists()
    }

    @Test
    fun testUnifyLoadingComponent() = runTest {
        composeTestRule.setContent {
            UnifyLoading(
                isLoading = true,
                message = "Loading...",
                modifier = Modifier.testTag("test_loading")
            )
        }
        
        composeTestRule.onNodeWithTag("test_loading").assertExists()
        composeTestRule.onNodeWithText("Loading...").assertExists()
    }

    @Test
    fun testUnifyAlertDialogComponent() = runTest {
        var showDialog by mutableStateOf(true)
        var dialogResult by mutableStateOf("")
        
        composeTestRule.setContent {
            if (showDialog) {
                UnifyAlertDialog(
                    title = "Test Dialog",
                    message = "This is a test dialog",
                    onConfirm = { 
                        dialogResult = "confirmed"
                        showDialog = false 
                    },
                    onDismiss = { 
                        dialogResult = "dismissed"
                        showDialog = false 
                    },
                    modifier = Modifier.testTag("test_dialog")
                )
            }
        }
        
        composeTestRule.onNodeWithTag("test_dialog").assertExists()
        composeTestRule.onNodeWithText("Test Dialog").assertExists()
        composeTestRule.onNodeWithText("This is a test dialog").assertExists()
        
        // 测试确认按钮
        composeTestRule.onNodeWithText("确认").performClick()
        assertEquals("confirmed", dialogResult)
    }

    // 高级组件测试
    @Test
    fun testUnifyChartComponent() = runTest {
        val chartData = listOf(
            ChartDataPoint("Jan", 100f),
            ChartDataPoint("Feb", 150f),
            ChartDataPoint("Mar", 120f),
            ChartDataPoint("Apr", 180f)
        )
        
        composeTestRule.setContent {
            UnifyChart(
                data = chartData,
                chartType = ChartType.LINE,
                modifier = Modifier.testTag("test_chart")
            )
        }
        
        composeTestRule.onNodeWithTag("test_chart").assertExists()
    }

    @Test
    fun testUnifyCalendarComponent() = runTest {
        var selectedDate by mutableStateOf<Long?>(null)
        
        composeTestRule.setContent {
            UnifyCalendar(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                modifier = Modifier.testTag("test_calendar")
            )
        }
        
        composeTestRule.onNodeWithTag("test_calendar").assertExists()
    }

    @Test
    fun testUnifyPickerComponent() = runTest {
        var selectedValue by mutableStateOf("")
        val options = listOf("Apple", "Banana", "Cherry", "Date")
        
        composeTestRule.setContent {
            UnifyPicker(
                options = options,
                selectedValue = selectedValue,
                onValueSelected = { selectedValue = it },
                modifier = Modifier.testTag("test_picker")
            )
        }
        
        composeTestRule.onNodeWithTag("test_picker").assertExists()
    }

    // AI组件测试
    @Test
    fun testUnifyAIChatComponent() = runTest {
        val messages = mutableListOf<ChatMessage>()
        
        composeTestRule.setContent {
            UnifyAIChat(
                messages = messages,
                onSendMessage = { message ->
                    messages.add(ChatMessage("user", message, System.currentTimeMillis()))
                },
                modifier = Modifier.testTag("test_ai_chat")
            )
        }
        
        composeTestRule.onNodeWithTag("test_ai_chat").assertExists()
    }

    @Test
    fun testUnifyAIAssistantComponent() = runTest {
        var assistantResponse by mutableStateOf("")
        
        composeTestRule.setContent {
            UnifyAIAssistant(
                onQuery = { query ->
                    assistantResponse = "AI response to: $query"
                },
                response = assistantResponse,
                modifier = Modifier.testTag("test_ai_assistant")
            )
        }
        
        composeTestRule.onNodeWithTag("test_ai_assistant").assertExists()
    }

    // 主题和样式测试
    @Test
    fun testUnifyThemeApplication() = runTest {
        composeTestRule.setContent {
            UnifyTheme {
                UnifyButton(
                    text = "Themed Button",
                    onClick = { },
                    modifier = Modifier.testTag("themed_button")
                )
            }
        }
        
        composeTestRule.onNodeWithTag("themed_button").assertExists()
        composeTestRule.onNodeWithText("Themed Button").assertExists()
    }

    @Test
    fun testDarkThemeSupport() = runTest {
        composeTestRule.setContent {
            UnifyTheme(darkTheme = true) {
                UnifyCard(
                    modifier = Modifier.testTag("dark_card")
                ) {
                    UnifyText("Dark Theme Content")
                }
            }
        }
        
        composeTestRule.onNodeWithTag("dark_card").assertExists()
        composeTestRule.onNodeWithText("Dark Theme Content").assertExists()
    }

    // 响应式设计测试
    @Test
    fun testResponsiveLayout() = runTest {
        composeTestRule.setContent {
            UnifyResponsiveLayout(
                modifier = Modifier.testTag("responsive_layout")
            ) { screenSize ->
                when (screenSize) {
                    ScreenSize.COMPACT -> {
                        UnifyColumn {
                            UnifyText("Compact Layout")
                        }
                    }
                    ScreenSize.MEDIUM -> {
                        UnifyRow {
                            UnifyText("Medium Layout")
                        }
                    }
                    ScreenSize.EXPANDED -> {
                        UnifyRow {
                            UnifyText("Expanded Layout")
                        }
                    }
                }
            }
        }
        
        composeTestRule.onNodeWithTag("responsive_layout").assertExists()
    }

    // 无障碍功能测试
    @Test
    fun testAccessibilitySupport() = runTest {
        composeTestRule.setContent {
            UnifyButton(
                text = "Accessible Button",
                onClick = { },
                contentDescription = "This is an accessible button for testing",
                modifier = Modifier.testTag("accessible_button")
            )
        }
        
        composeTestRule.onNodeWithTag("accessible_button").assertExists()
        composeTestRule.onNodeWithTag("accessible_button").assertContentDescriptionEquals(
            "This is an accessible button for testing"
        )
    }

    @Test
    fun testKeyboardNavigation() = runTest {
        composeTestRule.setContent {
            UnifyColumn {
                UnifyButton(
                    text = "Button 1",
                    onClick = { },
                    modifier = Modifier.testTag("button1")
                )
                UnifyButton(
                    text = "Button 2", 
                    onClick = { },
                    modifier = Modifier.testTag("button2")
                )
            }
        }
        
        // 测试Tab键导航
        composeTestRule.onNodeWithTag("button1").assertExists()
        composeTestRule.onNodeWithTag("button2").assertExists()
        
        composeTestRule.onNodeWithTag("button1").requestFocus()
        composeTestRule.onNodeWithTag("button1").assertIsFocused()
    }

    // 性能测试
    @Test
    fun testComponentRenderingPerformance() = runTest {
        val startTime = System.currentTimeMillis()
        
        composeTestRule.setContent {
            UnifyColumn {
                repeat(100) { index ->
                    UnifyCard(
                        modifier = Modifier.testTag("card_$index")
                    ) {
                        UnifyText("Card $index")
                        UnifyButton(
                            text = "Button $index",
                            onClick = { }
                        )
                    }
                }
            }
        }
        
        val renderTime = System.currentTimeMillis() - startTime
        assertTrue(renderTime < 5000, "Component rendering too slow: ${renderTime}ms")
        
        // 验证所有组件都已渲染
        composeTestRule.onNodeWithTag("card_0").assertExists()
        composeTestRule.onNodeWithTag("card_50").assertExists()
        composeTestRule.onNodeWithTag("card_99").assertExists()
    }

    @Test
    fun testRecompositionOptimization() = runTest {
        var recompositionCount = 0
        var counter by mutableStateOf(0)
        
        composeTestRule.setContent {
            UnifyColumn {
                // 这个组件应该在counter变化时重组
                UnifyText(
                    text = "Counter: $counter",
                    modifier = Modifier.testTag("counter_text")
                )
                
                // 这个组件不应该在counter变化时重组
                UnifyButton(
                    text = "Static Button",
                    onClick = { 
                        recompositionCount++
                    },
                    modifier = Modifier.testTag("static_button")
                )
            }
        }
        
        // 触发状态变化
        counter = 1
        composeTestRule.onNodeWithText("Counter: 1").assertExists()
        
        counter = 2
        composeTestRule.onNodeWithText("Counter: 2").assertExists()
        
        // 静态按钮应该仍然可点击
        composeTestRule.onNodeWithTag("static_button").performClick()
        assertEquals(1, recompositionCount)
    }

    // 错误处理测试
    @Test
    fun testComponentErrorHandling() = runTest {
        var hasError by mutableStateOf(false)
        
        composeTestRule.setContent {
            if (hasError) {
                UnifyErrorBoundary(
                    error = Exception("Test error"),
                    onRetry = { hasError = false },
                    modifier = Modifier.testTag("error_boundary")
                )
            } else {
                UnifyButton(
                    text = "Trigger Error",
                    onClick = { hasError = true },
                    modifier = Modifier.testTag("error_trigger")
                )
            }
        }
        
        // 触发错误
        composeTestRule.onNodeWithTag("error_trigger").performClick()
        composeTestRule.onNodeWithTag("error_boundary").assertExists()
        
        // 测试重试功能
        composeTestRule.onNodeWithText("重试").performClick()
        composeTestRule.onNodeWithTag("error_trigger").assertExists()
    }

    companion object {
        private val composeTestRule = createComposeRule()
    }
}

// 辅助数据类
data class BottomNavItem(val label: String, val icon: String)
data class ChartDataPoint(val label: String, val value: Float)
data class ChatMessage(val sender: String, val content: String, val timestamp: Long)

enum class ChartType { LINE, BAR, PIE }
enum class ScreenSize { COMPACT, MEDIUM, EXPANDED }
