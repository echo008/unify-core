package com.unify.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Desktop平台统一图标组件
 * 针对桌面端优化的图标显示
 */
@Composable
actual fun UnifyIcon(
    iconName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp
) {
    val iconVector = getDesktopIconVector(iconName)
    val iconSize = if (size == Dp.Unspecified) 24.dp else size
    val iconTint = if (tint == Color.Unspecified) Color(0xFF757575) else tint
    
    Icon(
        imageVector = iconVector,
        contentDescription = contentDescription,
        modifier = modifier.size(iconSize),
        tint = iconTint
    )
}

/**
 * Desktop平台矢量图标组件
 */
@Composable
actual fun UnifyVectorIcon(
    vectorPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp
) {
    // Desktop平台使用Material Icons作为矢量图标
    val iconVector = getDesktopIconVector(vectorPath)
    val iconSize = if (size == Dp.Unspecified) 24.dp else size
    val iconTint = if (tint == Color.Unspecified) Color(0xFF757575) else tint
    
    Icon(
        imageVector = iconVector,
        contentDescription = contentDescription,
        modifier = modifier.size(iconSize),
        tint = iconTint
    )
}

/**
 * Desktop平台系统图标组件
 */
@Composable
actual fun UnifySystemIcon(
    systemIconType: SystemIconType,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp
) {
    val iconVector = getSystemIconVector(systemIconType)
    val iconSize = if (size == Dp.Unspecified) 24.dp else size
    val iconTint = if (tint == Color.Unspecified) Color(0xFF757575) else tint
    
    Icon(
        imageVector = iconVector,
        contentDescription = contentDescription,
        modifier = modifier.size(iconSize),
        tint = iconTint
    )
}

/**
 * 获取系统图标向量
 */
private fun getSystemIconVector(systemIconType: SystemIconType): ImageVector {
    return when (systemIconType) {
        SystemIconType.HOME -> Icons.Default.Home
        SystemIconType.BACK -> Icons.Default.ArrowBack
        SystemIconType.MENU -> Icons.Default.Menu
        SystemIconType.SEARCH -> Icons.Default.Search
        SystemIconType.SETTINGS -> Icons.Default.Settings
        SystemIconType.PROFILE -> Icons.Default.Person
        SystemIconType.FAVORITE -> Icons.Default.Favorite
        SystemIconType.SHARE -> Icons.Default.Share
        SystemIconType.DELETE -> Icons.Default.Delete
        SystemIconType.EDIT -> Icons.Default.Edit
        SystemIconType.ADD -> Icons.Default.Add
        SystemIconType.CLOSE -> Icons.Default.Close
        SystemIconType.CHECK -> Icons.Default.Check
        SystemIconType.ARROW_UP -> Icons.Default.KeyboardArrowUp
        SystemIconType.ARROW_DOWN -> Icons.Default.KeyboardArrowDown
        SystemIconType.ARROW_LEFT -> Icons.Default.KeyboardArrowLeft
        SystemIconType.ARROW_RIGHT -> Icons.Default.KeyboardArrowRight
        SystemIconType.REFRESH -> Icons.Default.Refresh
        SystemIconType.DOWNLOAD -> Icons.Default.Download
        SystemIconType.UPLOAD -> Icons.Default.Upload
        SystemIconType.CAMERA -> Icons.Default.CameraAlt
        SystemIconType.GALLERY -> Icons.Default.Image
        SystemIconType.PHONE -> Icons.Default.Phone
        SystemIconType.EMAIL -> Icons.Default.Email
        SystemIconType.LOCATION -> Icons.Default.LocationOn
        SystemIconType.CALENDAR -> Icons.Default.DateRange
        SystemIconType.CLOCK -> Icons.Default.AccessTime
        SystemIconType.NOTIFICATION -> Icons.Default.Notifications
        SystemIconType.VOLUME_UP -> Icons.Default.VolumeUp
        SystemIconType.VOLUME_DOWN -> Icons.Default.VolumeDown
        SystemIconType.VOLUME_OFF -> Icons.Default.VolumeOff
    }
}

/**
 * 获取Desktop平台对应的图标向量
 */
private fun getDesktopIconVector(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "home" -> Icons.Default.Home
        "search" -> Icons.Default.Search
        "settings" -> Icons.Default.Settings
        "profile", "person" -> Icons.Default.Person
        "menu" -> Icons.Default.Menu
        "close" -> Icons.Default.Close
        "back", "arrow_back" -> Icons.Default.ArrowBack
        "forward", "arrow_forward" -> Icons.Default.ArrowForward
        "up", "arrow_up" -> Icons.Default.KeyboardArrowUp
        "down", "arrow_down" -> Icons.Default.KeyboardArrowDown
        "left", "arrow_left" -> Icons.Default.KeyboardArrowLeft
        "right", "arrow_right" -> Icons.Default.KeyboardArrowRight
        "add" -> Icons.Default.Add
        "remove" -> Icons.Default.Remove
        "edit" -> Icons.Default.Edit
        "delete" -> Icons.Default.Delete
        "save" -> Icons.Default.Done
        "cancel" -> Icons.Default.Clear
        "refresh" -> Icons.Default.Refresh
        "share" -> Icons.Default.Share
        "favorite", "heart" -> Icons.Default.Favorite
        "star" -> Icons.Default.Star
        "info" -> Icons.Default.Info
        "warning" -> Icons.Default.Warning
        "error" -> Icons.Default.Error
        "check", "done" -> Icons.Default.Check
        "email", "mail" -> Icons.Default.Email
        "phone" -> Icons.Default.Phone
        "location" -> Icons.Default.LocationOn
        "calendar", "date" -> Icons.Default.DateRange
        "time", "clock" -> Icons.Default.AccessTime
        "camera" -> Icons.Default.CameraAlt
        "image", "photo" -> Icons.Default.Image
        "video" -> Icons.Default.Videocam
        "music", "audio" -> Icons.Default.MusicNote
        "file", "document" -> Icons.Default.Description
        "folder" -> Icons.Default.Folder
        "download" -> Icons.Default.Download
        "upload" -> Icons.Default.Upload
        "cloud" -> Icons.Default.Cloud
        "wifi" -> Icons.Default.Wifi
        "bluetooth" -> Icons.Default.Bluetooth
        "battery" -> Icons.Default.Battery3Bar
        "volume" -> Icons.Default.VolumeUp
        "brightness" -> Icons.Default.Brightness6
        "lock" -> Icons.Default.Lock
        "unlock" -> Icons.Default.LockOpen
        "visibility", "show" -> Icons.Default.Visibility
        "visibility_off", "hide" -> Icons.Default.VisibilityOff
        "fullscreen" -> Icons.Default.Fullscreen
        "fullscreen_exit" -> Icons.Default.FullscreenExit
        "minimize" -> Icons.Default.Minimize
        "maximize" -> Icons.Default.CropFree
        "window" -> Icons.Default.OpenInNew
        "desktop" -> Icons.Default.Computer
        "laptop" -> Icons.Default.Laptop
        "tablet" -> Icons.Default.Tablet
        "phone_android" -> Icons.Default.PhoneAndroid
        "tv" -> Icons.Default.Tv
        "watch" -> Icons.Default.Watch
        "print" -> Icons.Default.Print
        "copy" -> Icons.Default.ContentCopy
        "paste" -> Icons.Default.ContentPaste
        "cut" -> Icons.Default.ContentCut
        "undo" -> Icons.Default.Undo
        "redo" -> Icons.Default.Redo
        "zoom_in" -> Icons.Default.ZoomIn
        "zoom_out" -> Icons.Default.ZoomOut
        "fit_screen" -> Icons.Default.FitScreen
        "grid" -> Icons.Default.GridView
        "list" -> Icons.Default.List
        "sort" -> Icons.Default.Sort
        "filter" -> Icons.Default.FilterList
        "more_vert" -> Icons.Default.MoreVert
        "more_horiz" -> Icons.Default.MoreHoriz
        "expand_more" -> Icons.Default.ExpandMore
        "expand_less" -> Icons.Default.ExpandLess
        "chevron_left" -> Icons.Default.ChevronLeft
        "chevron_right" -> Icons.Default.ChevronRight
        "first_page" -> Icons.Default.FirstPage
        "last_page" -> Icons.Default.LastPage
        "play" -> Icons.Default.PlayArrow
        "pause" -> Icons.Default.Pause
        "stop" -> Icons.Default.Stop
        "skip_previous" -> Icons.Default.SkipPrevious
        "skip_next" -> Icons.Default.SkipNext
        "replay" -> Icons.Default.Replay
        "shuffle" -> Icons.Default.Shuffle
        "repeat" -> Icons.Default.Repeat
        "volume_off" -> Icons.Default.VolumeOff
        "volume_down" -> Icons.Default.VolumeDown
        "volume_up" -> Icons.Default.VolumeUp
        "mic" -> Icons.Default.Mic
        "mic_off" -> Icons.Default.MicOff
        "headset" -> Icons.Default.Headset
        "speaker" -> Icons.Default.Speaker
        "notifications" -> Icons.Default.Notifications
        "notifications_off" -> Icons.Default.NotificationsOff
        "account_circle" -> Icons.Default.AccountCircle
        "login" -> Icons.Default.Login
        "logout" -> Icons.Default.Logout
        "security" -> Icons.Default.Security
        "admin_panel_settings" -> Icons.Default.AdminPanelSettings
        "dashboard" -> Icons.Default.Dashboard
        "analytics" -> Icons.Default.Analytics
        "trending_up" -> Icons.Default.TrendingUp
        "trending_down" -> Icons.Default.TrendingDown
        "bar_chart" -> Icons.Default.BarChart
        "pie_chart" -> Icons.Default.PieChart
        "timeline" -> Icons.Default.Timeline
        "schedule" -> Icons.Default.Schedule
        "event" -> Icons.Default.Event
        "task" -> Icons.Default.Task
        "assignment" -> Icons.Default.Assignment
        "bookmark" -> Icons.Default.Bookmark
        "bookmark_border" -> Icons.Default.BookmarkBorder
        "label" -> Icons.Default.Label
        "tag" -> Icons.Default.LocalOffer
        "flag" -> Icons.Default.Flag
        "priority_high" -> Icons.Default.PriorityHigh
        "bug_report" -> Icons.Default.BugReport
        "help" -> Icons.Default.Help
        "support" -> Icons.Default.Support
        "feedback" -> Icons.Default.Feedback
        "contact_support" -> Icons.Default.ContactSupport
        "language" -> Icons.Default.Language
        "translate" -> Icons.Default.Translate
        "public" -> Icons.Default.Public
        "travel" -> Icons.Default.Flight
        "hotel" -> Icons.Default.Hotel
        "restaurant" -> Icons.Default.Restaurant
        "shopping_cart" -> Icons.Default.ShoppingCart
        "store" -> Icons.Default.Store
        "payment" -> Icons.Default.Payment
        "credit_card" -> Icons.Default.CreditCard
        "money" -> Icons.Default.AttachMoney
        "work" -> Icons.Default.Work
        "business" -> Icons.Default.Business
        "group" -> Icons.Default.Group
        "people" -> Icons.Default.People
        "family" -> Icons.Default.FamilyRestroom
        "child_care" -> Icons.Default.ChildCare
        "school" -> Icons.Default.School
        "book" -> Icons.Default.Book
        "library" -> Icons.Default.LocalLibrary
        "science" -> Icons.Default.Science
        "medical" -> Icons.Default.MedicalServices
        "health" -> Icons.Default.HealthAndSafety
        "fitness" -> Icons.Default.FitnessCenter
        "sports" -> Icons.Default.Sports
        "games" -> Icons.Default.Games
        "movie" -> Icons.Default.Movie
        "theater" -> Icons.Default.TheaterComedy
        "art" -> Icons.Default.Palette
        "brush" -> Icons.Default.Brush
        "color" -> Icons.Default.ColorLens
        "format_paint" -> Icons.Default.FormatPaint
        "text_format" -> Icons.Default.TextFormat
        "font_download" -> Icons.Default.FontDownload
        "style" -> Icons.Default.Style
        "design_services" -> Icons.Default.DesignServices
        "architecture" -> Icons.Default.Architecture
        "engineering" -> Icons.Default.Engineering
        "construction" -> Icons.Default.Construction
        "build" -> Icons.Default.Build
        "handyman" -> Icons.Default.Handyman
        "plumbing" -> Icons.Default.Plumbing
        "electrical_services" -> Icons.Default.ElectricalServices
        "hvac" -> Icons.Default.Hvac
        "cleaning_services" -> Icons.Default.CleaningServices
        "pest_control" -> Icons.Default.PestControl
        "yard" -> Icons.Default.Yard
        "agriculture" -> Icons.Default.Agriculture
        "eco" -> Icons.Default.Eco
        "recycling" -> Icons.Default.Recycling
        "energy_savings_leaf" -> Icons.Default.EnergySavingsLeaf
        "solar_power" -> Icons.Default.SolarPower
        "water_drop" -> Icons.Default.WaterDrop
        "air" -> Icons.Default.Air
        "wb_sunny" -> Icons.Default.WbSunny
        "wb_cloudy" -> Icons.Default.WbCloudy
        "umbrella" -> Icons.Default.Umbrella
        "ac_unit" -> Icons.Default.AcUnit
        "thermostat" -> Icons.Default.Thermostat
        "device_thermostat" -> Icons.Default.DeviceThermostat
        "sensors" -> Icons.Default.Sensors
        "memory" -> Icons.Default.Memory
        "storage" -> Icons.Default.Storage
        "developer_mode" -> Icons.Default.DeveloperMode
        "code" -> Icons.Default.Code
        "terminal" -> Icons.Default.Terminal
        "integration_instructions" -> Icons.Default.IntegrationInstructions
        "api" -> Icons.Default.Api
        "webhook" -> Icons.Default.Webhook
        "token" -> Icons.Default.Token
        "key" -> Icons.Default.Key
        "vpn_key" -> Icons.Default.VpnKey
        "password" -> Icons.Default.Password
        "fingerprint" -> Icons.Default.Fingerprint
        "face" -> Icons.Default.Face
        "verified" -> Icons.Default.Verified
        "shield" -> Icons.Default.Shield
        "gpp_good" -> Icons.Default.GppGood
        "gpp_bad" -> Icons.Default.GppBad
        "policy" -> Icons.Default.Policy
        "privacy_tip" -> Icons.Default.PrivacyTip
        "cookie" -> Icons.Default.Cookie
        "track_changes" -> Icons.Default.TrackChanges
        "update" -> Icons.Default.Update
        "upgrade" -> Icons.Default.Upgrade
        "new_releases" -> Icons.Default.NewReleases
        "auto_awesome" -> Icons.Default.AutoAwesome
        "star_rate" -> Icons.Default.StarRate
        "thumb_up" -> Icons.Default.ThumbUp
        "thumb_down" -> Icons.Default.ThumbDown
        "sentiment_satisfied" -> Icons.Default.SentimentSatisfied
        "sentiment_dissatisfied" -> Icons.Default.SentimentDissatisfied
        "mood" -> Icons.Default.Mood
        "celebration" -> Icons.Default.Celebration
        "cake" -> Icons.Default.Cake
        "gift" -> Icons.Default.CardGiftcard
        "local_florist" -> Icons.Default.LocalFlorist
        "pets" -> Icons.Default.Pets
        "cruelty_free" -> Icons.Default.CrueltyFree
        else -> Icons.Default.Help // 默认图标
    }
}
