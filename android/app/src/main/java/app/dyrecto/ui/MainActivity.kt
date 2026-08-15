package app.dyrecto.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.dyrecto.billing.EntitlementProvider
import app.dyrecto.ui.components.FloatingNavBar
import app.dyrecto.ui.components.NavBarItem
import app.dyrecto.ui.components.PremiumSheetHost
import app.dyrecto.ui.components.premiumGated
import app.dyrecto.ui.navigation.Destination
import app.dyrecto.ui.screens.AlertControlCenterScreen
import app.dyrecto.ui.screens.AlertHistoryScreen
import app.dyrecto.ui.screens.CameraDetailsScreen
import app.dyrecto.ui.screens.CameraPickerScreen
import app.dyrecto.ui.screens.CameraScreen
import app.dyrecto.ui.screens.ConnectionScreen
import app.dyrecto.ui.screens.DeveloperScreen
import app.dyrecto.ui.screens.DeviceInfoScreen
import app.dyrecto.ui.screens.DiagnosticsScreen
import app.dyrecto.ui.screens.DiscoveryScreen
import app.dyrecto.ui.screens.HttpLiveViewScreen
import app.dyrecto.ui.screens.LiveViewScreen
import app.dyrecto.ui.screens.PtpSessionScreen
import app.dyrecto.ui.screens.PushLiveViewPocScreen
import app.dyrecto.ui.screens.SettingsScreen
import app.dyrecto.ui.screens.ShotReferenceScreen
import app.dyrecto.ui.screens.SshInfoScreen
import app.dyrecto.ui.screens.SshSessionScreen
import app.dyrecto.ui.screens.TelemetryExplorerScreen
import app.dyrecto.ui.screens.CapabilityExplorerScreen
import app.dyrecto.ui.theme.DyrectoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DyrectoTheme {
                AppRoot(viewModel())
            }
        }
    }
}

/**
 * Permissions gating the scan flow. BLE permissions are required; POST_NOTIFICATIONS (Android 13+)
 * is requested here purely so the ongoing monitoring notification can be shown — it is cosmetic and
 * NEVER gates connecting, monitoring, or alert delivery (see [onScanCameras], which only checks the
 * BLE permissions).
 */
private fun requiredPermissions(): Array<String> {
    val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        mutableListOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        perms += Manifest.permission.POST_NOTIFICATIONS
    }
    return perms.toTypedArray()
}

/** The subset of [requiredPermissions] that actually gates scanning/connecting (BLE only). */
private fun bleGatingPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

/** The four top-level tabs in the bottom navigation bar. */
private data class Tab(val dest: Destination, val icon: ImageVector, val label: String)

private val TABS = listOf(
    Tab(Destination.DASHBOARD, Icons.Rounded.GridView, "Dashboard"),
    Tab(Destination.SHOT_REFERENCE, Icons.Rounded.CenterFocusStrong, "Storyboard"),
    Tab(Destination.ALERT_HISTORY, Icons.Rounded.NotificationsNone, "Alerts"),
    Tab(Destination.SETTINGS, Icons.Rounded.Tune, "Settings"),
)

private val TOP_LEVEL = TABS.map { it.dest }.toSet()

private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(Destination.DASHBOARD.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(vm: CameraViewModel) {
    val state by vm.state.collectAsState()
    val nav = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val perms = remember { requiredPermissions() }
    val gating = remember { bleGatingPermissions() }

    fun bleGranted(): Boolean = gating.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        // Only the BLE permissions gate the flow; a denied POST_NOTIFICATIONS must not block it.
        if (bleGranted()) nav.navigate(Destination.CAMERA_PICKER.route)
    }

    val onScanCameras: () -> Unit = {
        if (bleGranted()) nav.navigate(Destination.CAMERA_PICKER.route)
        else permissionLauncher.launch(perms)
    }

    // Dyrecto Premium: re-run the Play ownership query + acknowledge sweep whenever the app
    // returns to the foreground (resolves pending purchases and retries failed acks).
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) EntitlementProvider.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    // The single purchase sheet, shown on demand from any locked surface.
    PremiumSheetHost()

    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val current = Destination.entries.firstOrNull { it.route == currentRoute } ?: Destination.DASHBOARD

    val isTopLevel = current in TOP_LEVEL
    val immersive = current == Destination.LIVE_VIEW   // full-screen Live View: no chrome
    val navItems = remember { TABS.map { NavBarItem(it.dest.route, it.icon, it.label) } }
    // The Dashboard, Reference Setup and Alerts screens render their own large header (title +
    // subtitle + action), so they hide the Material top app bar — same "owns its own chrome"
    // treatment as immersive Live View.
    val ownsHeader = current == Destination.DASHBOARD ||
        current == Destination.SHOT_REFERENCE ||
        current == Destination.ALERT_HISTORY ||
        current == Destination.ALERT_SETTINGS ||
        current == Destination.SETTINGS ||
        current == Destination.CAMERA_PICKER

    Scaffold(
        topBar = {
            if (!immersive && !ownsHeader) {
                TopAppBar(
                    title = { Text(current.title) },
                    navigationIcon = {
                        if (!isTopLevel) {
                            IconButton(onClick = { nav.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                )
            }
        },
    ) { padding ->
        // The nav bar is a floating overlay, not a reserved layout slot, so the screens draw
        // full-height and their content flows *under* the translucent glass island. Top-level
        // screens reserve `FloatingNavBarInset` at the bottom so their last items clear it.
        Box(Modifier.fillMaxSize().padding(padding)) {
            NavHost(
                navController = nav,
                startDestination = Destination.DASHBOARD.route,
                modifier = Modifier.fillMaxSize(),
            ) {
            composable(Destination.DASHBOARD.route) {
                val referenceState by vm.referenceState.collectAsState()
                val referenceMatch by vm.referenceMatch.collectAsState()
                val voiceSettings by vm.voiceSettings.collectAsState()
                CameraScreen(
                    state = state,
                    onScanCameras = onScanCameras,
                    onDisconnect = { vm.disconnect() },
                    onOpenAlerts = { nav.switchTab(Destination.ALERT_HISTORY.route) },
                    onOpenLiveView = { nav.navigate(Destination.LIVE_VIEW.route) },
                    onOpenDetails = { nav.navigate(Destination.CAMERA_DETAILS.route) },
                    onOpenReference = { nav.switchTab(Destination.SHOT_REFERENCE.route) },
                    onOpenVoice = { nav.switchTab(Destination.SETTINGS.route) },
                    onStartMonitoring = premiumGated { vm.startReferenceMonitoring() },
                    onStopMonitoring = { vm.stopReferenceMonitoring() },
                    liveRender = vm.pushLiveRender,
                    referenceMatch = referenceMatch,
                    referenceState = referenceState,
                    voiceSettings = voiceSettings,
                )
            }
            composable(Destination.SHOT_REFERENCE.route) {
                val referenceState by vm.referenceState.collectAsState()
                val referenceMatch by vm.referenceMatch.collectAsState()
                val render by vm.pushLiveRender.collectAsState()
                // Dyrecto Premium: the whole Storyboard feature is premium. The screen stays
                // browsable, but every mutating intent (incl. image import, which runs the AI
                // reference analysis) routes free users to the purchase sheet.
                val premium by EntitlementProvider.isPremium.collectAsState()
                fun locked(action: () -> Unit) {
                    if (premium) action() else app.dyrecto.ui.components.PremiumSheetController.show()
                }
                ShotReferenceScreen(
                    referenceState = referenceState,
                    referenceMatch = referenceMatch,
                    frameStreamActive = render.active,
                    onAddImage = { uri -> locked { vm.addReferenceImage(uri, "Shot") } },
                    onReplaceImage = { id, uri -> locked { vm.replaceReference(id, uri, "Shot") } },
                    onRemoveImage = { id -> vm.removeReference(id) },
                    onSetOptions = { opts -> locked { vm.setReferenceOptions(opts) } },
                    onSetCompletionRule = { rule -> locked { vm.setCompletionRule(rule) } },
                    onResetProgress = { vm.resetStoryboardProgress() },
                    onStart = premiumGated { vm.startReferenceMonitoring() },
                    onStop = { vm.stopReferenceMonitoring() },
                    onClear = { vm.clearReference() },
                )
            }
            composable(Destination.CAMERA_DETAILS.route) { CameraDetailsScreen(state) }
            composable(Destination.SETTINGS.route) {
                val voiceSettings by vm.voiceSettings.collectAsState()
                SettingsScreen(
                    voiceSettings = voiceSettings,
                    onVoiceSettingsChange = { vm.setVoiceSettings(it) },
                    onOpenAlerts = { nav.navigate(Destination.ALERT_SETTINGS.route) },
                    onOpenDeveloper = { nav.navigate(Destination.DEVELOPER.route) },
                )
            }
            composable(Destination.LIVE_VIEW.route) {
                LiveViewScreen(
                    render = vm.pushLiveRender,
                    frameContext = vm.frameContext,
                    visionContext = vm.visionContext,
                    capabilities = vm.cameraCapabilities,
                    onSetProperty = { code, value -> vm.setCameraProperty(code, value) },
                )
            }
            // Debug-only diagnostic routes: reachable only from the Developer screen, so like
            // DEVELOPER itself they are not even registered in the release NavHost (surface-area
            // hardening — a stray navigate() to any of them no-ops in production).
            if (app.dyrecto.BuildConfig.DEBUG) {
                composable(Destination.LIVE_VIEW_HTTP.route) {
                    HttpLiveViewScreen(
                        liveView = vm.liveView,
                        onStart = { vm.startLiveView() },
                        onStop = { vm.stopLiveView() },
                    )
                }
                composable(Destination.PUSH_LIVE_VIEW_POC.route) {
                    PushLiveViewPocScreen(
                        pushLiveView = vm.pushLiveView,
                        pushLiveRender = vm.pushLiveRender,
                        frameContext = vm.frameContext,
                        onStart = { vm.startPushLiveView() },
                    )
                }
                composable(Destination.DISCOVERY.route) { DiscoveryScreen(state) }
                composable(Destination.CONNECTION.route) { ConnectionScreen(state) }
                composable(Destination.SSH_INFO.route) { SshInfoScreen(state) }
                composable(Destination.SSH_SESSION.route) { SshSessionScreen(state) }
                composable(Destination.PTP_SESSION.route) { PtpSessionScreen(state) }
                composable(Destination.DEVICE_INFO.route) { DeviceInfoScreen(state) }
                composable(Destination.DIAGNOSTICS.route) { DiagnosticsScreen(state) }
                composable(Destination.TELEMETRY_EXPLORER.route) { TelemetryExplorerScreen(state) }
                composable(Destination.CAPABILITY_EXPLORER.route) {
                    val caps by vm.cameraCapabilities.collectAsState()
                    CapabilityExplorerScreen(caps)
                }
            }
            composable(Destination.CAMERA_PICKER.route) {
                CameraPickerScreen(
                    state = state,
                    onStartScan = { vm.startScan() },
                    onStopScan = { vm.stopScan() },
                    onSelectCamera = { address ->
                        vm.selectCamera(address)
                        nav.popBackStack()
                    },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Destination.ALERT_HISTORY.route) {
                AlertHistoryScreen(
                    onOpenControlCenter = { nav.navigate(Destination.ALERT_SETTINGS.route) },
                )
            }
            composable(Destination.ALERT_SETTINGS.route) {
                val configs by vm.alertConfigs.collectAsState()
                val testPlaying by vm.testPlaying.collectAsState()
                AlertControlCenterScreen(
                    configs = configs,
                    testPlaying = testPlaying,
                    onUpdate = { vm.updateAlertConfig(it) },
                    onTest = { vm.testAlert(it) },
                    onRestoreDefault = { vm.restoreAlertDefault(it) },
                    onRestoreAll = { vm.restoreAllAlertDefaults() },
                    onBack = { nav.popBackStack() },
                )
            }
            // Debug-only: the route is never registered in release, so the screen is unreachable
            // even if a navigate(DEVELOPER) call slips through (Settings hides its entry too).
            if (app.dyrecto.BuildConfig.DEBUG) composable(Destination.DEVELOPER.route) {
                val pushLiveView by vm.pushLiveView.collectAsState()
                val visionStats by vm.visionStatistics.collectAsState()
                val visionContext by vm.visionContext.collectAsState()
                val sceneContext by vm.sceneContext.collectAsState()
                val referenceState by vm.referenceState.collectAsState()
                val referenceMatch by vm.referenceMatch.collectAsState()
                val aiCapabilities by vm.aiCapabilities.collectAsState()
                val perceptualTuning by vm.perceptualTuning.collectAsState()
                val voiceDebug by vm.voiceDebug.collectAsState()
                val voiceSettings by vm.voiceSettings.collectAsState()
                DeveloperScreen(
                    pushLiveView = pushLiveView,
                    visionStats = visionStats,
                    visionContext = visionContext,
                    sceneContext = sceneContext,
                    referenceState = referenceState,
                    referenceMatch = referenceMatch,
                    aiCapabilities = aiCapabilities,
                    perceptualTuning = perceptualTuning,
                    voiceDebug = voiceDebug,
                    voiceSettings = voiceSettings,
                    onPerceptualTuningChange = { vm.setPerceptualTuning(it) },
                    onCameraIpChange = { vm.setCameraIp(it) },
                    onReadEe02 = { vm.readEe02() },
                    onReadEe04 = { vm.readEe04() },
                    onDumpEeState = { vm.dumpEeState() },
                    onNavigate = { nav.navigate(it.route) },
                )
            }
            }

            if (isTopLevel) {
                FloatingNavBar(
                    items = navItems,
                    selectedRoute = current.route,
                    onSelect = { route -> if (current.route != route) nav.switchTab(route) },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}
