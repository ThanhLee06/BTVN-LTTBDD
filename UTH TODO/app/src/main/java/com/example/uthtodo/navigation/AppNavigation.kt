package com.example.uthtodo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import android.widget.Toast
import com.example.uthtodo.features.*
import com.example.uthtodo.model.TodoTask
import com.example.uthtodo.screens.*
import java.util.UUID
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import android.net.Uri
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.uthtodo.auth.AuthManager
import com.example.uthtodo.model.OnboardingRepository
import com.example.uthtodo.screens.HomeScreen
import com.example.uthtodo.screens.LoginScreen
import com.example.uthtodo.screens.ProfileScreen
import com.example.uthtodo.screens.StartScreen
import com.example.uthtodo.screens.NotificationsScreen
import com.example.uthtodo.screens.SettingsScreen
import com.example.uthtodo.screens.TaskListScreen
import com.example.uthtodo.screens.TaskDetailScreen
import com.example.uthtodo.screens.TaskEditorDialog
import com.example.uthtodo.viewmodel.TasksViewModel
import com.example.uthtodo.services.AnalyticsService
import com.example.uthtodo.services.NotificationService
import com.example.uthtodo.model.TodoRepository
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    repository: OnboardingRepository = remember { OnboardingRepository() }
) {
    val metaData = repository.getMetaData()
    val tasksModel: TasksViewModel = viewModel()
    val tasksState by tasksModel.state.collectAsState()
    val featureState by FeatureGraph.store.state.collectAsState()
    val destination by FeatureGraph.destination.collectAsState()
    val permissions = rememberPermissionController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var editorId by rememberSaveable { mutableStateOf<String?>(null) }
    var draft by rememberSaveable { mutableStateOf("") }
    var draftOriginals by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var photoTarget by rememberSaveable { mutableStateOf<String?>(null) }
    var photoOwner by rememberSaveable { mutableStateOf<String?>(null) }
    var viewedPhoto by rememberSaveable { mutableStateOf<String?>(null) }

    fun openEditor(id: String) {
        val task = tasksState.tasks.find { it.id == id } ?: TodoTask("", "", "Học tập", "09:00", date = tasksModel.clock.today())
        val extras = featureState.tasks[id]
        draftOriginals = extras?.photos.orEmpty().map { it.id }
        draft = FeatureCodec.encode(LocalTaskData(task, extras?.identity ?: UUID.randomUUID().toString(), extras?.location, extras?.photos.orEmpty()))
        tasksModel.dismissError(); FeatureGraph.store.dismissError(); editorId = id
    }
    fun acceptPhoto(photo: TaskPhoto) {
        if (photoOwner != TodoRepository.currentUser.value.uid || photoOwner.isNullOrBlank()) {
            FeatureGraph.store.safeDelete(photo.path); return
        }
        if (photoTarget == "draft" && editorId != null) {
            val data = FeatureCodec.decode(draft)
            if (data.photos.size < 5) draft = FeatureCodec.encode(data.copy(photos = data.photos + photo))
            else FeatureGraph.store.safeDelete(photo.path)
        } else {
            val task = tasksModel.state.value.tasks.find { it.id == photoTarget }
            if (task == null) { FeatureGraph.store.safeDelete(photo.path); return }
            scope.launch {
                FeatureGraph.store.guard {
                    val existing = FeatureGraph.store.state.value.tasks[task.id]
                    FeatureGraph.store.setExtras(task, existing?.location, existing?.photos.orEmpty() + photo)
                }
            }
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            try { acceptPhoto(PhotoFiles.import(context, uri)) }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) { Toast.makeText(context, "Không nhập được ảnh. Kiểm tra file và bộ nhớ rồi thử lại.", Toast.LENGTH_LONG).show() }
        }
    }
    fun camera(target: String) {
        photoTarget = target; photoOwner = TodoRepository.currentUser.value.uid
        navController.navigate("camera") { launchSingleTop = true }
    }
    fun pick(target: String) {
        photoTarget = target; photoOwner = TodoRepository.currentUser.value.uid
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    fun viewPhoto(photo: TaskPhoto) { viewedPhoto = photo.path; navController.navigate("attachment") }
    fun discardEditor() {
        val persisted = draftOriginals.toSet()
        runCatching { FeatureCodec.decode(draft) }.getOrNull()?.photos?.filterNot { it.id in persisted }
            ?.forEach { FeatureGraph.store.safeDelete(it.path) }
        editorId = null; draft = ""; tasksModel.dismissError(); FeatureGraph.store.dismissError()
    }

    val startDestination = remember {
        AuthManager.initAuthState()
        if (AuthManager.isUserLoggedIn()) "home" else "start"
    }
    val entry by navController.currentBackStackEntryAsState()
    val openInbox = NotificationService.openInbox.value
    val uid = TodoRepository.currentUser.value.uid
    var lastOwner by rememberSaveable { mutableStateOf(uid) }
    LaunchedEffect(uid) {
        if (lastOwner != uid) {
            if (editorId != null) discardEditor()
            permissions.cancel(); viewedPhoto = null; photoTarget = null
            if (entry?.destination?.route in listOf("camera", "attachment")) {
                navController.popBackStack()
            }
        }
        lastOwner = uid
    }
    LaunchedEffect(destination, uid, entry?.destination?.route) {
        val target = destination ?: return@LaunchedEffect
        if (uid.isBlank() || entry == null) return@LaunchedEffect
        if (target.owner == uid) {
            val snapshot = FeatureGraph.store.find(uid, target.taskId)
            if (snapshot?.identity == target.identity && !snapshot.deleted) {
                tasksModel.load()
                withContext(Dispatchers.Main.immediate) {
                    navController.navigate("task/${target.taskId}?source=home") { launchSingleTop = true }
                }
            } else Toast.makeText(context, "Công việc trong thông báo không còn tồn tại.", Toast.LENGTH_LONG).show()
        } else Toast.makeText(context, "Thông báo này thuộc tài khoản khác.", Toast.LENGTH_LONG).show()
        FeatureGraph.destination.value = null
    }
    LaunchedEffect(entry?.destination?.route, uid) {
        if (entry?.destination?.route in listOf("home", "tasks?category={category}")) tasksModel.load(force = true)
    }
    fun navigateTab(tab: String) {
        when (tab) {
            "home" -> if (!navController.popBackStack("home", false)) navController.navigate("home") { launchSingleTop = true }
            "list" -> if (entry?.destination?.route != "tasks?category={category}") {
                navController.navigate("tasks?category=all") { launchSingleTop = true }
            }
            else -> navController.navigate(tab) { launchSingleTop = true }
        }
    }
    LaunchedEffect(entry?.destination?.route) {
        permissions.cancel()
        entry?.destination?.route?.let { AnalyticsService.logScreen(it) }
    }
    LaunchedEffect(openInbox, entry?.destination?.route) {
        if (openInbox && TodoRepository.currentUser.value.uid.isNotBlank() && entry != null) {
            NotificationService.openInbox.value = false
            navController.navigate("notifications") { launchSingleTop = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Màn hình khởi đầu (StartScreen - Splash)
        composable("start") {
            StartScreen(
                metaData = metaData,
                onNavigateNext = {
                    navController.navigate("login") {
                        popUpTo("start") { inclusive = true }
                    }
                }
            )
        }

        // 1. Màn hình Đăng nhập (LoginScreen)
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 2. Màn hình Trang chủ (HomeScreen)
        composable("home") {
            HomeScreen(
                state = tasksState, clock = tasksModel.clock, user = TodoRepository.currentUser.value,
                onTab = ::navigateTab, onAdd = { openEditor("new") },
                onTask = { navController.navigate("task/$it?source=home") }, onToggle = tasksModel::toggle,
                onCategory = { navController.navigate("tasks?category=${Uri.encode(it)}") },
                onProfile = { navController.navigate("profile") }, onRetry = { tasksModel.load(true) }
            )
        }
        composable("tasks?category={category}", arguments = listOf(navArgument("category") { defaultValue = "all" })) { taskEntry ->
            TaskListScreen(tasksState, tasksModel.clock, taskEntry.arguments?.getString("category") ?: "all",
                onTab = ::navigateTab, onAdd = { openEditor("new") },
                onTask = { navController.navigate("task/$it?source=list") }, onToggle = tasksModel::toggle,
                onRetry = { tasksModel.load(true) })
        }
        composable("task/{id}?source={source}", arguments = listOf(navArgument("source") { defaultValue = "home" })) { detailEntry ->
            val id = detailEntry.arguments?.getString("id").orEmpty()
            LaunchedEffect(id, uid) { tasksModel.detail(id) }
            TaskDetailScreen(id, detailEntry.arguments?.getString("source") ?: "home", tasksState, tasksModel.clock,
                onBack = { navController.popBackStack() },
                onEdit = { openEditor(it.id) }, onRetry = { tasksModel.detail(id) },
                onSave = tasksModel::save, onDelete = { tasksModel.delete(id) { navController.popBackStack() } },
                additionalContent = { task -> TaskExtrasPanel(task, permissions, { camera(task.id) }, { pick(task.id) }, ::viewPhoto) })
        }
        composable("camera") { CameraScreen(permissions, { navController.popBackStack() }, { photo -> acceptPhoto(photo); navController.popBackStack() }) }
        composable("attachment") {
            AttachmentViewer(viewedPhoto?.let { TaskPhoto("view", it, 0) }, { navController.popBackStack() })
        }
        composable("permissions") { AppPermissionsScreen(permissions) { navController.popBackStack() } }
        composable("notifications") { NotificationsScreen(onBack = { navController.popBackStack() }) }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() }, onProfile = { navController.navigate("profile") },
                onTaskRefresh = { tasksModel.load(true) }, onPermissions = { navController.navigate("permissions") })
        }

        // 3. Màn hình Hồ sơ (ProfileScreen)
        composable("profile") {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogoutSuccess = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
    editorId?.takeIf { entry?.destination?.route !in listOf("camera", "attachment") }?.let { id ->
        TaskEditorDialog(initialTask = tasksState.tasks.find { it.id == id }, today = tasksModel.clock.today(),
            busy = id in tasksState.busy || featureState.busy, error = featureState.error ?: tasksState.error,
            onDismiss = ::discardEditor,
            onSave = { task -> tasksModel.save(task) {
                val savedId = tasksModel.state.value.lastSavedId
                val saved = tasksModel.state.value.tasks.find { it.id == savedId }
                if (saved != null) {
                    editorId = saved.id
                    scope.launch {
                        val extras = FeatureCodec.decode(draft)
                        if (FeatureGraph.store.guard { FeatureGraph.store.setExtras(saved, extras.location, extras.photos) }) { editorId = null; draft = "" }
                    }
                }
            } }, reminderAvailable = permissions.snapshot.granted(PermissionKind.NOTIFICATIONS),
            requestReminderPermission = { granted -> permissions.request(PermissionKind.NOTIFICATIONS, onGranted = granted) },
            extrasContent = {
                val data = FeatureCodec.decode(draft)
                ExtrasFields(data.location, data.photos, permissions,
                    { draft = FeatureCodec.encode(data.copy(location = it)) }, { camera("draft") }, { pick("draft") },
                    { photo ->
                        draft = FeatureCodec.encode(data.copy(photos = data.photos.filterNot { it.id == photo.id }))
                        if (photo.id !in draftOriginals) FeatureGraph.store.safeDelete(photo.path)
                    }, ::viewPhoto)
            })
    }
    if (entry?.destination?.route in listOf("login", "home", "settings") && editorId == null) PermissionIntroOverlay(permissions)
}
