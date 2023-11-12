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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
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
//import androidx.compose.material3.TextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.itemclassifier.data.Datasource
import com.example.itemclassifier.data.TfLiteObjectClassifier
import com.example.itemclassifier.domain.Classification
import com.example.itemclassifier.model.Item
import com.example.itemclassifier.ui.theme.ItemClassifierTheme

private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)

class MainActivity : ComponentActivity() {
    //private lateinit var cameraExecutor: ExecutorService

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
                                    .align(Alignment.TopCenter)
                            ) {
                                classification.forEach {
                                    Log.d("Obj", it.objName)
                                    Text (
                                        text = it.objName,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(8.dp),
                                        textAlign = TextAlign.Center,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        //requestCamera()
        //cameraExecutor = Executors.newSingleThreadExecutor()
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

    Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.fillMaxSize()) {
        AndroidView(
            { previewView },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(onClick = { shouldShowCamera.value = false }) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Localized description"
            )
        }
    }
}

@Composable
fun ItemCard(item: Item, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.fillMaxSize()
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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(item.descResourceId),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(
                modifier = Modifier.padding(8.dp),
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Localized description"
            )
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
    ItemCard(Item(R.string.book, R.string.bookDesc, R.drawable.book))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    requestCamera: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold (
        topBar = {
            Surface (shadowElevation = 2.dp) {
                CenterAlignedTopAppBar(
                    modifier = Modifier.padding(5.dp),
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
            //var searchText = remember { mutableStateOf("") }
            BottomAppBar(
                actions = {
                    /* TextField(
                        value = searchText.value,
                        onValueChange = {value ->
                            searchText.value = value
                        }
                    ) */
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
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
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