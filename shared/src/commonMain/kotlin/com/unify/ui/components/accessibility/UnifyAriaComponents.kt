package com.unify.ui.components.accessibility

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*

/**
 * Unify 无障碍组件
 * 对应微信小程序的 aria-component 等无障碍组件
 */

/**
 * Aria 角色类型
 */
enum class UnifyAriaRole {
    BUTTON,         // 按钮
    LINK,           // 链接
    IMAGE,          // 图片
    TEXT,           // 文本
    HEADING,        // 标题
    LIST,           // 列表
    LISTITEM,       // 列表项
    TAB,            // 标签页
    TABLIST,        // 标签页列表
    TABPANEL,       // 标签页面板
    DIALOG,         // 对话框
    ALERT,          // 警告
    MENU,           // 菜单
    MENUITEM,       // 菜单项
    CHECKBOX,       // 复选框
    RADIO,          // 单选框
    TEXTBOX,        // 文本框
    SLIDER,         // 滑块
    PROGRESSBAR,    // 进度条
    COMBOBOX,       // 组合框
    GRID,           // 网格
    GRIDCELL,       // 网格单元格
    TREE,           // 树形结构
    TREEITEM,       // 树形项
    TOOLBAR,        // 工具栏
    TOOLTIP,        // 工具提示
    STATUS,         // 状态
    REGION,         // 区域
    BANNER,         // 横幅
    NAVIGATION,     // 导航
    MAIN,           // 主要内容
    COMPLEMENTARY,  // 补充内容
    CONTENTINFO,    // 内容信息
    SEARCH,         // 搜索
    FORM,           // 表单
    APPLICATION     // 应用程序
}

/**
 * Aria 状态
 */
data class UnifyAriaState(
    val expanded: Boolean? = null,      // aria-expanded
    val selected: Boolean? = null,      // aria-selected
    val checked: Boolean? = null,       // aria-checked
    val pressed: Boolean? = null,       // aria-pressed
    val hidden: Boolean? = null,        // aria-hidden
    val disabled: Boolean? = null,      // aria-disabled
    val readonly: Boolean? = null,      // aria-readonly
    val required: Boolean? = null,      // aria-required
    val invalid: Boolean? = null,       // aria-invalid
    val busy: Boolean? = null,          // aria-busy
    val live: String? = null,           // aria-live: off, polite, assertive
    val atomic: Boolean? = null,        // aria-atomic
    val relevant: String? = null,       // aria-relevant: additions, removals, text, all
    val level: Int? = null,             // aria-level
    val setsize: Int? = null,           // aria-setsize
    val posinset: Int? = null,          // aria-posinset
    val valuemin: Float? = null,        // aria-valuemin
    val valuemax: Float? = null,        // aria-valuemax
    val valuenow: Float? = null,        // aria-valuenow
    val valuetext: String? = null,      // aria-valuetext
    val orientation: String? = null,    // aria-orientation: horizontal, vertical
    val sort: String? = null,           // aria-sort: ascending, descending, none, other
    val multiselectable: Boolean? = null, // aria-multiselectable
    val multiline: Boolean? = null,     // aria-multiline
    val autocomplete: String? = null,   // aria-autocomplete: inline, list, both, none
    val haspopup: String? = null,       // aria-haspopup: false, true, menu, listbox, tree, grid, dialog
    val modal: Boolean? = null,         // aria-modal
    val grabbed: Boolean? = null,       // aria-grabbed
    val dropeffect: String? = null      // aria-dropeffect: copy, execute, link, move, none, popup
)

/**
 * Aria 关系
 */
data class UnifyAriaRelation(
    val labelledby: String? = null,     // aria-labelledby
    val describedby: String? = null,    // aria-describedby
    val controls: String? = null,       // aria-controls
    val owns: String? = null,           // aria-owns
    val flowto: String? = null,         // aria-flowto
    val activedescendant: String? = null, // aria-activedescendant
    val details: String? = null,        // aria-details
    val errormessage: String? = null    // aria-errormessage
)

/**
 * Aria 组件基础配置
 */
data class UnifyAriaConfig(
    val role: UnifyAriaRole? = null,
    val label: String? = null,          // aria-label
    val state: UnifyAriaState = UnifyAriaState(),
    val relation: UnifyAriaRelation = UnifyAriaRelation(),
    val keyshortcuts: String? = null,   // aria-keyshortcuts
    val roledescription: String? = null // aria-roledescription
)

/**
 * 无障碍组件包装器
 */
@Composable
fun UnifyAriaComponent(
    config: UnifyAriaConfig,
    modifier: Modifier = Modifier,
    id: String? = null,
    onFocus: (() -> Unit)? = null,
    onBlur: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .semantics {
                // 设置角色
                config.role?.let { ariaRole ->
                    role = when (ariaRole) {
                        UnifyAriaRole.BUTTON -> Role.Button
                        UnifyAriaRole.IMAGE -> Role.Image
                        UnifyAriaRole.CHECKBOX -> Role.Checkbox
                        UnifyAriaRole.RADIO -> Role.RadioButton
                        UnifyAriaRole.TAB -> Role.Tab
                        UnifyAriaRole.TEXTBOX -> Role.TextBox
                        UnifyAriaRole.SLIDER -> Role.Slider
                        UnifyAriaRole.PROGRESSBAR -> Role.ProgressBar
                        UnifyAriaRole.COMBOBOX -> Role.DropdownList
                        else -> Role.Generic
                    }
                }
                
                // 设置标签
                config.label?.let { label ->
                    contentDescription = label
                }
                
                // 设置状态
                config.state.expanded?.let { expanded ->
                    if (expanded) {
                        stateDescription = "展开"
                    } else {
                        stateDescription = "折叠"
                    }
                }
                
                config.state.selected?.let { selected ->
                    this.selected = selected
                }
                
                config.state.disabled?.let { disabled ->
                    this.disabled = disabled
                }
                
                config.state.hidden?.let { hidden ->
                    invisibleToUser = hidden
                }
                
                // 设置焦点处理
                onFocus?.let { focusCallback ->
                    onFocusEvent = { focusState ->
                        if (focusState.isFocused) {
                            focusCallback()
                        } else {
                            onBlur?.invoke()
                        }
                    }
                }
                
                // 设置ID
                id?.let { elementId ->
                    testTag = elementId
                }
            }
    ) {
        content()
    }
}

/**
 * 无障碍按钮组件
 */
@Composable
fun UnifyAriaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    pressed: Boolean = false,
    ariaLabel: String? = null,
    ariaDescribedBy: String? = null,
    keyShortcuts: String? = null,
    icon: ImageVector? = null,
    contentDescription: String? = null
) {
    val config = UnifyAriaConfig(
        role = UnifyAriaRole.BUTTON,
        label = ariaLabel ?: text,
        state = UnifyAriaState(
            disabled = !enabled,
            pressed = pressed
        ),
        relation = UnifyAriaRelation(
            describedby = ariaDescribedBy
        ),
        keyshortcuts = keyShortcuts
    )
    
    UnifyAriaComponent(
        config = config,
        modifier = modifier
    ) {
        UnifyButton(
            text = text,
            onClick = onClick,
            enabled = enabled,
            leadingIcon = icon,
            contentDescription = contentDescription
        )
    }
}

/**
 * 无障碍标题组件
 */
@Composable
fun UnifyAriaHeading(
    text: String,
    level: Int = 1,
    modifier: Modifier = Modifier,
    ariaLabel: String? = null,
    color: Color = LocalUnifyTheme.current.colors.onSurface,
    contentDescription: String? = null
) {
    val config = UnifyAriaConfig(
        role = UnifyAriaRole.HEADING,
        label = ariaLabel ?: text,
        state = UnifyAriaState(
            level = level
        )
    )
    
    val variant = when (level) {
        1 -> UnifyTextVariant.DISPLAY_LARGE
        2 -> UnifyTextVariant.DISPLAY_MEDIUM
        3 -> UnifyTextVariant.DISPLAY_SMALL
        4 -> UnifyTextVariant.TITLE_LARGE
        5 -> UnifyTextVariant.TITLE_MEDIUM
        6 -> UnifyTextVariant.TITLE_SMALL
        else -> UnifyTextVariant.TITLE_MEDIUM
    }
    
    UnifyAriaComponent(
        config = config,
        modifier = modifier
    ) {
        UnifyText(
            text = text,
            variant = variant,
            color = color,
            fontWeight = FontWeight.Bold,
            contentDescription = contentDescription
        )
    }
}

/**
 * 无障碍列表组件
 */
@Composable
fun UnifyAriaList(
    items: List<String>,
    modifier: Modifier = Modifier,
    ariaLabel: String? = null,
    multiselectable: Boolean = false,
    onItemClick: ((index: Int, item: String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val config = UnifyAriaConfig(
        role = UnifyAriaRole.LIST,
        label = ariaLabel,
        state = UnifyAriaState(
            multiselectable = multiselectable,
            setsize = items.size
        )
    )
    
    UnifyAriaComponent(
        config = config,
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier.semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
        ) {
            itemsIndexed(items) { index, item ->
                UnifyAriaListItem(
                    text = item,
                    position = index + 1,
                    setSize = items.size,
                    onClick = { onItemClick?.invoke(index, item) }
                )
            }
        }
    }
}

/**
 * 无障碍列表项组件
 */
@Composable
fun UnifyAriaListItem(
    text: String,
    modifier: Modifier = Modifier,
    position: Int = 1,
    setSize: Int = 1,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val config = UnifyAriaConfig(
        role = UnifyAriaRole.LISTITEM,
        label = text,
        state = UnifyAriaState(
            selected = selected,
            posinset = position,
            setsize = setSize
        )
    )
    
    UnifyAriaComponent(
        config = config,
        modifier = modifier
    ) {
        ListItem(
            headlineContent = {
                UnifyText(
                    text = text,
                    variant = UnifyTextVariant.BODY_MEDIUM
                )
            },
            modifier = Modifier
                .clickable { onClick?.invoke() }
                .semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                },
            colors = ListItemDefaults.colors(
                containerColor = if (selected) {
                    LocalUnifyTheme.current.colors.primary.copy(alpha = 0.12f)
                } else {
                    Color.Transparent
                }
            )
        )
    }
}

/**
 * 无障碍标签页组件
 */
@Composable
fun UnifyAriaTabList(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    ariaLabel: String? = null,
    orientation: String = "horizontal",
    contentDescription: String? = null
) {
    val config = UnifyAriaConfig(
        role = UnifyAriaRole.TABLIST,
        label = ariaLabel,
        state = UnifyAriaState(
            orientation = orientation
        )
    )
    
    UnifyAriaComponent(
        config = config,
        modifier = modifier
    ) {
        if (orientation == "horizontal") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription?.let { 
                            this.contentDescription = it 
                        }
                    }
            ) {
                tabs.forEachIndexed { index, tab ->
                    UnifyAriaTab(
                        text = tab,
                        selected = index == selectedIndex,
                        onClick = { onTabSelected(index) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    UnifyAriaTab(
                        text = tab,
                        selected = index == selectedIndex,
                        onClick = { onTabSelected(index) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * 无障碍标签页项组件
 */
@Composable
fun UnifyAriaTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ariaLabel: String? = null,
    controls: String? = null,
    contentDescription: String? = null
) {
    val config = UnifyAriaConfig(
        role = UnifyAriaRole.TAB,
        label = ariaLabel ?: text,
        state = UnifyAriaState(
            selected = selected
        ),
        relation = UnifyAriaRelation(
            controls = controls
        )
    )
    
    UnifyAriaComponent(
        config = config,
        modifier = modifier
    ) {
        Tab(
            selected = selected,
            onClick = onClick,
            text = {
                UnifyText(
                    text = text,
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                )
            },
            modifier = Modifier.semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
        )
    }
}

/**
 * 无障碍对话框组件
 */
@Composable
fun UnifyAriaDialog(
    title: String,
    content: @Composable () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    ariaLabel: String? = null,
    ariaDescribedBy: String? = null,
    modal: Boolean = true,
    contentDescription: String? = null
) {
    val config = UnifyAriaConfig(
        role = UnifyAriaRole.DIALOG,
        label = ariaLabel ?: title,
        state = UnifyAriaState(
            modal = modal
        ),
        relation = UnifyAriaRelation(
            describedby = ariaDescribedBy
        )
    )
    
    UnifyAriaComponent(
        config = config,
        modifier = modifier
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                UnifyText(
                    text = title,
                    variant = UnifyTextVariant.TITLE_LARGE,
                    fontWeight = FontWeight.Medium
                )
            },
            text = {
                content()
            },
            confirmButton = {
                UnifyAriaButton(
                    text = "确定",
                    onClick = onDismiss
                )
            },
            modifier = Modifier.semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
        )
    }
}

/**
 * 无障碍警告组件
 */
@Composable
fun UnifyAriaAlert(
    message: String,
    modifier: Modifier = Modifier,
    severity: String = "info", // info, warning, error, success
    live: String = "polite", // off, polite, assertive
    atomic: Boolean = true,
    ariaLabel: String? = null,
    contentDescription: String? = null
) {
    val config = UnifyAriaConfig(
        role = UnifyAriaRole.ALERT,
        label = ariaLabel ?: message,
        state = UnifyAriaState(
            live = live,
            atomic = atomic
        )
    )
    
    val backgroundColor = when (severity) {
        "error" -> LocalUnifyTheme.current.colors.errorContainer
        "warning" -> Color(0xFFFFF3CD)
        "success" -> Color(0xFFD4EDDA)
        else -> LocalUnifyTheme.current.colors.primaryContainer
    }
    
    val textColor = when (severity) {
        "error" -> LocalUnifyTheme.current.colors.onErrorContainer
        "warning" -> Color(0xFF856404)
        "success" -> Color(0xFF155724)
        else -> LocalUnifyTheme.current.colors.onPrimaryContainer
    }
    
    val icon = when (severity) {
        "error" -> Icons.Default.Error
        "warning" -> Icons.Default.Warning
        "success" -> Icons.Default.CheckCircle
        else -> Icons.Default.Info
    }
    
    UnifyAriaComponent(
        config = config,
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnifyIcon(
                    icon = icon,
                    size = UnifyIconSize.MEDIUM,
                    tint = textColor
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                UnifyText(
                    text = message,
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 无障碍状态组件
 */
@Composable
fun UnifyAriaStatus(
    status: String,
    modifier: Modifier = Modifier,
    live: String = "polite",
    atomic: Boolean = false,
    ariaLabel: String? = null,
    contentDescription: String? = null
) {
    val config = UnifyAriaConfig(
        role = UnifyAriaRole.STATUS,
        label = ariaLabel ?: status,
        state = UnifyAriaState(
            live = live,
            atomic = atomic
        )
    )
    
    UnifyAriaComponent(
        config = config,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                    liveRegion = when (live) {
                        "assertive" -> LiveRegionMode.Assertive
                        "polite" -> LiveRegionMode.Polite
                        else -> LiveRegionMode.None
                    }
                }
        ) {
            UnifyText(
                text = status,
                variant = UnifyTextVariant.BODY_SMALL,
                color = LocalUnifyTheme.current.colors.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 无障碍工具提示组件
 */
@Composable
fun UnifyAriaTooltip(
    text: String,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    ariaLabel: String? = null,
    contentDescription: String? = null
) {
    val config = UnifyAriaConfig(
        role = UnifyAriaRole.TOOLTIP,
        label = ariaLabel ?: text
    )
    
    var showTooltip by remember { mutableStateOf(false) }
    
    UnifyAriaComponent(
        config = config,
        modifier = modifier
    ) {
        Box {
            Box(
                modifier = Modifier
                    .clickable { showTooltip = !showTooltip }
                    .semantics {
                        contentDescription?.let { 
                            this.contentDescription = it 
                        }
                    }
            ) {
                content()
            }
            
            if (showTooltip) {
                Card(
                    modifier = Modifier
                        .offset(y = (-40).dp)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    UnifyText(
                        text = text,
                        variant = UnifyTextVariant.CAPTION,
                        color = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
