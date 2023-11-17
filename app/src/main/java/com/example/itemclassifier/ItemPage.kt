package com.example.itemclassifier

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.itemclassifier.data.Datasource
import com.example.itemclassifier.model.Item

@Composable
fun ExpandableCard(title: String, item: Item, imageCard: Boolean) {

    var expanded by remember { mutableStateOf (false) }

    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else  0f,
        label = "rotState"
    )

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 12.dp,
                top = 16.dp,
                end = 8.dp,
                bottom = 12.dp
            )
            .clickable {
                expanded = !expanded
            }
    ) {
        Column() {
            Row (
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(8.dp)
                )
                IconButton(
                    modifier = Modifier
                        .rotate(rotationState),
                    onClick = {
                        expanded = !expanded
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Localized description"
                    )
                }
            }

            if (expanded) {
                if (imageCard) {
                    Image(
                        painter = painterResource(item.imageResourceId),
                        contentDescription = stringResource(item.nameResourceId),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(194.dp)
                            .shadow(5.dp),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        text = stringResource(item.descResourceId),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ItemPagePreview() {
    ItemPage(Datasource().loadItem("Book"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemPage(
    item: Item
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold (
        topBar = {
            Surface (shadowElevation = 2.dp) {
                CenterAlignedTopAppBar(
                    modifier = Modifier.padding(5.dp),
                    title = {
                        Text(
                            text = stringResource(item.nameResourceId),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { MainActivity().setShouldShowItemPage(false) }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Localized description"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        },
        bottomBar = {
            //var searchText = remember { mutableStateOf("") }
            BottomAppBar(
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
                        onClick = { /* On Click function */ },
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
            //verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                Image(
                    painter = painterResource(item.imageResourceId),
                    contentDescription = stringResource(item.nameResourceId),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(194.dp)
                        .shadow(5.dp),
                    contentScale = ContentScale.Crop,
                )
                ExpandableCard("Details", item, false)
            }
        }
    }
}