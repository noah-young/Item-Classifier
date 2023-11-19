package com.example.itemclassifier

import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.itemclassifier.data.Datasource
import com.example.itemclassifier.data.TfLiteObjectClassifier
import com.example.itemclassifier.domain.Classification
import com.example.itemclassifier.model.Item
import com.example.itemclassifier.ui.theme.ItemClassifierTheme

private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)
private var shouldShowItemPage: MutableState<Boolean> = mutableStateOf(false)
private var activeItem: Item = Datasource().loadItem("Book")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ItemClassifierTheme {
                var classification by remember {
                    mutableStateOf(emptyList<Classification>())
                }

                val analyzer = remember {
                    ObjectAnalyzer (
                        classifier = TfLiteObjectClassifier (
                            context = applicationContext
                        ),
                        onResults = {
                            classification = it
                        }
                    )
                }

                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                        setImageAnalysisAnalyzer(
                            ContextCompat.getMainExecutor(applicationContext),
                            analyzer
                        )
                    }
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScaffold(requestCamera = ::requestCamera)
                    Log.d("Test Value", "${shouldShowCamera.value}")

                    // Open the camera
                    AnimatedVisibility (
                        shouldShowCamera.value,
                        enter = slideInVertically( initialOffsetY = { it / 2 } )
                                + expandVertically ( expandFrom = Alignment.Top )
                                + fadeIn(),
                        exit = slideOutVertically( targetOffsetY = { it / 2 } )
                                + shrinkVertically( shrinkTowards = Alignment.Top )
                                + fadeOut()
                    ) {
                        Box (
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CameraView(controller)

                            Column (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                            ) {
                                classification.forEach {
                                    ItemCard(Datasource().loadItem(it.objName))
                                }
                            }
                        }
                    }

                    // Open the active item details page
                    AnimatedVisibility (
                        shouldShowItemPage.value,
                        enter =  slideInHorizontally ( initialOffsetX = { it / 2 } )
                                + expandHorizontally ( expandFrom = Alignment.End ),
                        exit = slideOutHorizontally( targetOffsetX = { it / 2 } )
                                + shrinkHorizontally ( shrinkTowards = Alignment.End )
                    ) {
                        ItemPage(activeItem)
                    }
                }
            }
        }
    }

    private fun requestCamera() {
        Log.d("TEST", "TEST CALL")
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                shouldShowCamera.value = true
                Log.i("kilo", "Permission previously granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> Log.i("kilo", "Show camera permissions dialog")

            else -> checkCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private val checkCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            shouldShowCamera.value = true
            Log.d("Test Value", "${shouldShowCamera.value}")
        } else {
            Toast.makeText(baseContext, "Camera access denied.", Toast.LENGTH_SHORT).show()
        }
    }

    fun setShouldShowItemPage (showVal: Boolean) {
        shouldShowItemPage.value = showVal
    }
}

@Composable
fun CameraView (
    controller: LifecycleCameraController
) {
    //val lensFacing = CameraSelector.LENS_FACING_BACK
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            this.controller = controller
            controller.bindToLifecycle(lifecycleOwner)
        }
    }

    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            { previewView },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(onClick = { shouldShowCamera.value = false }) {
            Icon(
                imageVector = Icons.Filled.Close,
                modifier = Modifier.size(32.dp),
                tint = Color.White,
                contentDescription = "Localized description"
            )
        }
    }
}

@Composable
fun ItemCard(
    item: Item,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            Column {
                Image(
                    painter = painterResource(item.imageResourceId),
                    contentDescription = stringResource(item.nameResourceId),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(194.dp),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = stringResource(item.nameResourceId),
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(item.descResourceId),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = {
                shouldShowItemPage.value = true
                activeItem = item
            }) {
                Icon(
                    modifier = Modifier.padding(8.dp),
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "Localized description"
                )
            }
        }
    }
}

@Composable
private fun ItemList(itemList: List<Item>) {
    LazyColumn {
        items(itemList){ item ->
            ItemCard(item)
        }
    }
}

@Preview
@Composable
private fun ItemCardPreview() {
    ItemCard(Item(R.string.Book, R.string.bookDesc, R.drawable.book))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    requestCamera: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold (
        topBar = {
            Surface (shadowElevation = 2.dp, tonalElevation = 2.dp) {
                CenterAlignedTopAppBar(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                    //modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors (
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    title = {
                        Text(
                            "Item Classifier",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        IconButton(onClick = { /* click action */ }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Localized description"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.shadow(2.dp),
                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Localized description",
                        )
                    }
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Localized description",
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { requestCamera() },
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Filled.Camera, "Localized description")
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ItemList(itemList = Datasource().loadItems())
        }
    }
}