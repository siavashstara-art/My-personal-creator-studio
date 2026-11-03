package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.SiavashStudioViewModel
import com.example.ui.components.StudioFooter
import com.example.ui.components.StudioHeader
import com.example.ui.screens.*
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioCream
import com.example.ui.theme.StudioMuted
import com.example.ui.theme.StudioObsidian
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent() {
    val context = LocalContext.current
    val viewModel: SiavashStudioViewModel = viewModel()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Handle toast messages
    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }
    }

    // Audio Permission Request
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (!isGranted) {
            viewModel.showToast("جهت ضبط صدا، دسترسی میکروفون الزامی است.")
        }
    }

    LaunchedEffect(Unit) {
        if (!hasMicPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Back button handling
    BackHandler(enabled = currentScreen != AppScreen.STUDIO) {
        viewModel.navigateTo(AppScreen.STUDIO)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = StudioCardDark,
                drawerContentColor = StudioCream
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                    Text(
                        text = "استودیو شخصی سیاوش",
                        color = GoldLight,
                        fontSize = 17.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(
                        text = "اکوسیستم آفرینش آفرینا (توانا سیتی)",
                        color = StudioMuted,
                        fontSize = 11.sp
                    )
                }
                HorizontalDivider(color = StudioCardElevated, modifier = Modifier.padding(vertical = 10.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Mic, contentDescription = null, tint = GoldPrimary) },
                    label = { Text("استودیوی ضبط و تله‌پرومپتر", color = StudioCream, fontSize = 13.sp) },
                    selected = currentScreen == AppScreen.STUDIO,
                    onClick = {
                        viewModel.navigateTo(AppScreen.STUDIO)
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = GoldDark.copy(alpha = 0.3f))
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Equalizer, contentDescription = null, tint = GoldPrimary) },
                    label = { Text("میکسر و افکت‌های غیرمخرب", color = StudioCream, fontSize = 13.sp) },
                    selected = currentScreen == AppScreen.MIXER,
                    onClick = {
                        viewModel.navigateTo(AppScreen.MIXER)
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = GoldDark.copy(alpha = 0.3f))
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null, tint = GoldPrimary) },
                    label = { Text("هوش آوازی و تحلیل تحریر ایرانی", color = StudioCream, fontSize = 13.sp) },
                    selected = currentScreen == AppScreen.ANALYSIS,
                    onClick = {
                        viewModel.navigateTo(AppScreen.ANALYSIS)
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = GoldDark.copy(alpha = 0.3f))
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Folder, contentDescription = null, tint = GoldPrimary) },
                    label = { Text("مدیریت آرشیو پروژه‌ها", color = StudioCream, fontSize = 13.sp) },
                    selected = currentScreen == AppScreen.PROJECTS,
                    onClick = {
                        viewModel.navigateTo(AppScreen.PROJECTS)
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = GoldDark.copy(alpha = 0.3f))
                )

                HorizontalDivider(color = StudioCardElevated, modifier = Modifier.padding(vertical = 10.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldLight) },
                    label = { Text("لیگ معرفان (Referral League)", color = GoldLight, fontSize = 13.sp) },
                    selected = currentScreen == AppScreen.REFERRAL_LEAGUE,
                    onClick = {
                        viewModel.navigateTo(AppScreen.REFERRAL_LEAGUE)
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = GoldDark.copy(alpha = 0.3f))
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldLight) },
                    label = { Text("لیگ مشاهیر وفاداران (Hall of Fame)", color = GoldLight, fontSize = 13.sp) },
                    selected = currentScreen == AppScreen.HALL_OF_FAME,
                    onClick = {
                        viewModel.navigateTo(AppScreen.HALL_OF_FAME)
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = GoldDark.copy(alpha = 0.3f))
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Backup, contentDescription = null, tint = StudioCream) },
                    label = { Text("پشتیبان‌گیری امن و SAF", color = StudioCream, fontSize = 13.sp) },
                    selected = currentScreen == AppScreen.BACKUP_RESTORE,
                    onClick = {
                        viewModel.navigateTo(AppScreen.BACKUP_RESTORE)
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = GoldDark.copy(alpha = 0.3f))
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Videocam, contentDescription = null, tint = StudioCream) },
                    label = { Text("تیزر تبلیغاتی ۳۰ ثانیه‌ای آپارات", color = StudioCream, fontSize = 13.sp) },
                    selected = currentScreen == AppScreen.PROMO_VIDEO,
                    onClick = {
                        viewModel.navigateTo(AppScreen.PROMO_VIDEO)
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = GoldDark.copy(alpha = 0.3f))
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(StudioObsidian),
            topBar = {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = GoldLight)
                        }
                        Text(
                            text = "SIAVASH MUSIC CREATOR",
                            color = GoldPrimary,
                            fontSize = 13.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.REFERRAL_LEAGUE) }) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = "Referral", tint = GoldLight)
                        }
                    }
                    StudioHeader()
                }
            },
            bottomBar = {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    NavigationBar(
                        containerColor = StudioCardDark,
                        contentColor = StudioCream
                    ) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Mic, contentDescription = "Studio") },
                            label = { Text("استودیو", fontSize = 10.sp) },
                            selected = currentScreen == AppScreen.STUDIO,
                            onClick = { viewModel.navigateTo(AppScreen.STUDIO) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = StudioObsidian,
                                selectedTextColor = GoldLight,
                                indicatorColor = GoldPrimary,
                                unselectedIconColor = StudioMuted,
                                unselectedTextColor = StudioMuted
                            )
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Equalizer, contentDescription = "Mixer") },
                            label = { Text("میکسر", fontSize = 10.sp) },
                            selected = currentScreen == AppScreen.MIXER,
                            onClick = { viewModel.navigateTo(AppScreen.MIXER) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = StudioObsidian,
                                selectedTextColor = GoldLight,
                                indicatorColor = GoldPrimary,
                                unselectedIconColor = StudioMuted,
                                unselectedTextColor = StudioMuted
                            )
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Analytics, contentDescription = "Analysis") },
                            label = { Text("هوش آوازی", fontSize = 10.sp) },
                            selected = currentScreen == AppScreen.ANALYSIS,
                            onClick = { viewModel.navigateTo(AppScreen.ANALYSIS) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = StudioObsidian,
                                selectedTextColor = GoldLight,
                                indicatorColor = GoldPrimary,
                                unselectedIconColor = StudioMuted,
                                unselectedTextColor = StudioMuted
                            )
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Folder, contentDescription = "Projects") },
                            label = { Text("پروژه‌ها", fontSize = 10.sp) },
                            selected = currentScreen == AppScreen.PROJECTS,
                            onClick = { viewModel.navigateTo(AppScreen.PROJECTS) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = StudioObsidian,
                                selectedTextColor = GoldLight,
                                indicatorColor = GoldPrimary,
                                unselectedIconColor = StudioMuted,
                                unselectedTextColor = StudioMuted
                            )
                        )
                    }
                    StudioFooter()
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(StudioObsidian)
            ) {
                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        AppScreen.STUDIO -> StudioScreen(viewModel)
                        AppScreen.MIXER -> MixerScreen(viewModel)
                        AppScreen.ANALYSIS -> VocalAnalysisScreen(viewModel)
                        AppScreen.PROJECTS -> ProjectsScreen(viewModel)
                        AppScreen.REFERRAL_LEAGUE -> ReferralLeagueScreen(viewModel)
                        AppScreen.HALL_OF_FAME -> HallOfFameScreen(viewModel)
                        AppScreen.BACKUP_RESTORE -> BackupRestoreScreen(viewModel)
                        AppScreen.PROMO_VIDEO -> PromoVideoScreen(viewModel)
                    }
                }
            }
        }
    }
}
