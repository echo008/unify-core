# UI 组件 API

## 📱 基础组件

### UnifyButton - 统一按钮组件

跨平台的按钮组件，支持多种样式和状态。

```kotlin
@Composable
fun UnifyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    icon: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.Start
)
```

#### 参数说明
- `text`: 按钮显示文本
- `onClick`: 点击事件回调
- `modifier`: Compose 修饰符
- `enabled`: 是否启用按钮
- `loading`: 是否显示加载状态
- `variant`: 按钮变体样式
- `size`: 按钮尺寸
- `icon`: 可选图标
- `iconPosition`: 图标位置

#### 按钮变体
```kotlin
enum class ButtonVariant {
    Primary,    // 主要按钮
    Secondary,  // 次要按钮
    Outline,    // 轮廓按钮
    Text,       // 文本按钮
    Danger      // 危险操作按钮
}

enum class ButtonSize {
    Small,      // 小尺寸
    Medium,     // 中等尺寸
    Large       // 大尺寸
}

enum class IconPosition {
    Start,      // 图标在开始位置
    End         // 图标在结束位置
}
```

#### 使用示例
```kotlin
@Composable
fun ButtonExamples() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 主要按钮
        UnifyButton(
            text = "主要操作",
            onClick = { /* 处理点击 */ },
            variant = ButtonVariant.Primary
        )
        
        // 带图标的按钮
        UnifyButton(
            text = "添加项目",
            onClick = { /* 处理点击 */ },
            icon = Icons.Default.Add,
            variant = ButtonVariant.Secondary
        )
        
        // 加载状态按钮
        UnifyButton(
            text = "提交中...",
            onClick = { /* 处理点击 */ },
            loading = true,
            enabled = false
        )
        
        // 危险操作按钮
        UnifyButton(
            text = "删除",
            onClick = { /* 处理点击 */ },
            variant = ButtonVariant.Danger,
            size = ButtonSize.Small
        )
    }
}
```

### UnifyTextField - 文本输入组件

功能丰富的文本输入组件，支持验证和多种输入类型。

```kotlin
@Composable
fun UnifyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    helperText: String = "",
    errorText: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: (() -> Unit)? = null,
    maxLines: Int = 1,
    maxLength: Int? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None
)
```

#### 使用示例
```kotlin
@Composable
fun TextFieldExamples() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 邮箱输入
        UnifyTextField(
            value = email,
            onValueChange = { email = it },
            label = "邮箱地址",
            placeholder = "请输入邮箱",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            helperText = "用于接收通知和找回密码"
        )
        
        // 密码输入
        UnifyTextField(
            value = password,
            onValueChange = { password = it },
            label = "密码",
            placeholder = "请输入密码",
            leadingIcon = Icons.Default.Lock,
            trailingIcon = Icons.Default.Visibility,
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password
        )
        
        // 多行文本输入
        UnifyTextField(
            value = description,
            onValueChange = { description = it },
            label = "描述",
            placeholder = "请输入详细描述",
            maxLines = 4,
            maxLength = 500,
            helperText = "${description.length}/500"
        )
    }
}
```

### UnifyCard - 卡片组件

Material Design 风格的卡片容器组件。

```kotlin
@Composable
fun UnifyCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
    shape: Shape = RoundedCornerShape(8.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
)
```

#### 使用示例
```kotlin
@Composable
fun CardExamples() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 基础卡片
            UnifyCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "基础卡片",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "这是一个基础的卡片组件示例",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        item {
            // 可点击卡片
            UnifyCard(
                onClick = { /* 处理点击 */ },
                elevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Column {
                        Text(
                            text = "通知设置",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "管理应用通知偏好",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
```

### UnifyDialog - 对话框组件

统一的对话框组件，支持多种类型和自定义内容。

```kotlin
@Composable
fun UnifyDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    content: (@Composable () -> Unit)? = null,
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    properties: DialogProperties = DialogProperties()
)
```

#### 使用示例
```kotlin
@Composable
fun DialogExamples() {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    
    Column {
        UnifyButton(
            text = "显示确认对话框",
            onClick = { showConfirmDialog = true }
        )
        
        UnifyButton(
            text = "显示自定义对话框",
            onClick = { showCustomDialog = true }
        )
    }
    
    // 确认对话框
    UnifyDialog(
        visible = showConfirmDialog,
        onDismiss = { showConfirmDialog = false },
        title = "确认删除",
        content = {
            Text("确定要删除这个项目吗？此操作无法撤销。")
        },
        confirmButton = {
            UnifyButton(
                text = "删除",
                onClick = { 
                    // 执行删除操作
                    showConfirmDialog = false 
                },
                variant = ButtonVariant.Danger
            )
        },
        dismissButton = {
            UnifyButton(
                text = "取消",
                onClick = { showConfirmDialog = false },
                variant = ButtonVariant.Text
            )
        }
    )
}
```

## 📋 列表组件

### UnifyList - 统一列表组件

高性能的列表组件，支持多种布局和交互。

```kotlin
@Composable
fun <T> UnifyList(
    items: List<T>,
    modifier: Modifier = Modifier,
    key: ((item: T) -> Any)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
)
```

### UnifyListItem - 列表项组件

标准化的列表项组件。

```kotlin
@Composable
fun UnifyListItem(
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
    headlineContent: @Composable () -> Unit,
    supportingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
)
```

#### 使用示例
```kotlin
data class Contact(
    val id: String,
    val name: String,
    val email: String,
    val avatar: String?
)

@Composable
fun ContactList(contacts: List<Contact>) {
    UnifyList(
        items = contacts,
        key = { it.id }
    ) { contact ->
        UnifyListItem(
            leadingContent = {
                AsyncImage(
                    model = contact.avatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(R.drawable.default_avatar)
                )
            },
            headlineContent = {
                Text(contact.name)
            },
            supportingContent = {
                Text(
                    text = contact.email,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            trailingContent = {
                IconButton(onClick = { /* 更多操作 */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多")
                }
            },
            onClick = { /* 查看联系人详情 */ }
        )
    }
}
```

## 🎛️ 输入控件

### UnifySwitch - 开关组件

```kotlin
@Composable
fun UnifySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    description: String? = null
)
```

### UnifyCheckbox - 复选框组件

```kotlin
@Composable
fun UnifyCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    triState: Boolean = false
)
```

### UnifyRadioButton - 单选按钮组件

```kotlin
@Composable
fun UnifyRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null
)
```

#### 使用示例
```kotlin
@Composable
fun InputControlsExample() {
    var switchState by remember { mutableStateOf(false) }
    var checkboxState by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("option1") }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 开关
        UnifySwitch(
            checked = switchState,
            onCheckedChange = { switchState = it },
            label = "启用通知",
            description = "接收应用推送通知"
        )
        
        // 复选框
        UnifyCheckbox(
            checked = checkboxState,
            onCheckedChange = { checkboxState = it },
            label = "同意用户协议"
        )
        
        // 单选按钮组
        Text("选择主题:")
        listOf(
            "option1" to "浅色主题",
            "option2" to "深色主题",
            "option3" to "跟随系统"
        ).forEach { (value, label) ->
            UnifyRadioButton(
                selected = selectedOption == value,
                onClick = { selectedOption = value },
                label = label
            )
        }
    }
}
```

## 📊 数据展示组件

### UnifyProgressIndicator - 进度指示器

```kotlin
@Composable
fun UnifyProgressIndicator(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 4.dp,
    type: ProgressType = ProgressType.Linear
)

enum class ProgressType {
    Linear,     // 线性进度条
    Circular    // 圆形进度条
}
```

### UnifyBadge - 徽章组件

```kotlin
@Composable
fun UnifyBadge(
    count: Int,
    modifier: Modifier = Modifier,
    maxCount: Int = 99,
    showZero: Boolean = false,
    color: Color = MaterialTheme.colorScheme.error,
    textColor: Color = MaterialTheme.colorScheme.onError
)
```

### UnifyChip - 标签组件

```kotlin
@Composable
fun UnifyChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onSelectionChange: ((Boolean) -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    enabled: Boolean = true
)
```

#### 使用示例
```kotlin
@Composable
fun DataDisplayExample() {
    var progress by remember { mutableFloatStateOf(0.7f) }
    var selectedChips by remember { mutableStateOf(setOf<String>()) }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 进度指示器
        Text("下载进度: ${(progress * 100).toInt()}%")
        UnifyProgressIndicator(
            progress = progress,
            type = ProgressType.Linear
        )
        
        // 徽章
        Box {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "通知"
            )
            UnifyBadge(
                count = 5,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
        
        // 标签组
        Text("选择标签:")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("工作", "学习", "生活", "娱乐")) { tag ->
                UnifyChip(
                    text = tag,
                    selected = tag in selectedChips,
                    onSelectionChange = { selected ->
                        selectedChips = if (selected) {
                            selectedChips + tag
                        } else {
                            selectedChips - tag
                        }
                    }
                )
            }
        }
    }
}
```

## 🎨 主题和样式

### 颜色系统

```kotlin
object UnifyColors {
    val Primary = Color(0xFF6200EE)
    val PrimaryVariant = Color(0xFF3700B3)
    val Secondary = Color(0xFF03DAC6)
    val SecondaryVariant = Color(0xFF018786)
    val Background = Color(0xFFFFFBFE)
    val Surface = Color(0xFFFFFBFE)
    val Error = Color(0xFFB00020)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSecondary = Color(0xFF000000)
    val OnBackground = Color(0xFF1C1B1F)
    val OnSurface = Color(0xFF1C1B1F)
    val OnError = Color(0xFFFFFFFF)
}
```

### 字体系统

```kotlin
val UnifyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

## 📱 响应式布局

### ResponsiveGrid - 响应式网格

```kotlin
@Composable
fun ResponsiveGrid(
    modifier: Modifier = Modifier,
    columns: GridCells = GridCells.Adaptive(minSize = 200.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: LazyGridScope.() -> Unit
)
```

### AdaptiveContainer - 自适应容器

```kotlin
@Composable
fun AdaptiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 1200.dp,
    horizontalPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
)
```

## 🔧 实用工具

### 主题切换

```kotlin
@Composable
fun UnifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UnifyTypography,
        content = content
    )
}
```

### 动画效果

```kotlin
object UnifyAnimations {
    val FastOutSlowIn = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val LinearOutSlowIn = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val FastOutLinearIn = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    
    const val DURATION_SHORT = 150
    const val DURATION_MEDIUM = 300
    const val DURATION_LONG = 500
}
```

---

通过这些 UI 组件，您可以快速构建美观、一致且功能丰富的跨平台用户界面。所有组件都遵循 Material Design 设计规范，并针对不同平台进行了优化。
