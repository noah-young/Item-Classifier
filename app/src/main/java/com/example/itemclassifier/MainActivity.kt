package com.example.itemclassifier

import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
//import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.itemclassifier.model.Item
import com.example.itemclassifier.ui.theme.ItemClassifierTheme
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ItemClassifierTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScaffold(requestCamera = ::requestCamera)
                    Log.d("Test Value", "${shouldShowCamera.value}")
                    if (shouldShowCamera.value) {
                        CameraView (
                            executor = cameraExecutor
                        )
                    }
                }
            }
        }
        //requestCamera()
        cameraExecutor = Executors.newSingleThreadExecutor()
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
    executor: Executor
) {
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = androidx.camera.core.Preview.Builder().build()
    val previewView = remember {
        PreviewView(context)
    }
    val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

    LaunchedEffect(lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener( {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview)

                preview.setSurfaceProvider(previewView.surfaceProvider)

            } catch(exc: Exception) {
                Log.e("TAG", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.fillMaxSize()) {
        AndroidView({previewView}, modifier = Modifier.fillMaxSize())

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
    Card(modifier = modifier.padding(8.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
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
                modifier = Modifier.padding(horizontal =  16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(item.descResourceId),
                modifier = Modifier.padding(horizontal =  16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ItemList(itemList: List<Item>, modifier: Modifier = Modifier) {
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
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { requestCamera() }) {
                Icon(Icons.Default.Camera, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ItemList(itemList = Datasource().loadItems())
        }
    }
}