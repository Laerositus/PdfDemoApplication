package com.laerodev.pdfdemoapplication

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PdfViewerScreen("test.pdf")
                }
            }
        }
    }
}

@Composable
fun PdfViewerScreen(pdfFileName: String) {
    val context = LocalContext.current
    val pdfLoader = remember { PdfLoader(context, pdfFileName) }
    val pageCount = pdfLoader.getPageCount()
    var currentPageIndex by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val headers = remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    val bitmaps = remember { mutableStateListOf<Bitmap?>() }
    val backgroundColor: Color = Color.hsl(215F, 1F, 0.896F)

    // Simulate header extraction (replace with your actual logic)
    LaunchedEffect(key1 = Unit) {
        val extractedHeaders = mapOf(
            0 to "Home",
            4 to "Technical Specifications",
            9 to "Connectivity",
            13 to "Design"
        )
        headers.value = extractedHeaders
    }

    // Load all bitmaps upfront
    LaunchedEffect(key1 = Unit) {
        withContext(Dispatchers.IO) {
            for (i in 0 until pageCount) {
                val bitmap = pdfLoader.renderPage(i)
                bitmaps.add(bitmap)
            }
        }
    }

    DisposableEffect(key1 = pdfLoader) {
        onDispose {
            pdfLoader.closePdf()
        }
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        val (navMenu, pdfViewer) = createRefs()

        // Navigation Menu
        Column(
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight()
                .constrainAs(navMenu) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(headers.value.toList()) { (pageNumber, headerName) ->
                    Text(
                        text = headerName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                currentPageIndex = pageNumber
                                coroutineScope.launch {
                                    lazyListState.animateScrollToItem(pageNumber)
                                }
                            }
                    )
                }
            }
        }

        // PDF Viewer
        Box(modifier = Modifier
            .constrainAs(pdfViewer) {
                top.linkTo(parent.top)
                start.linkTo(parent.start, margin = 200.dp)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            }) {
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState
            ) {
                items(pageCount) { index ->
                    val bitmap = bitmaps.getOrNull(index)
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Page ${index + 1}",
                            modifier = Modifier.fillMaxHeight().fillParentMaxWidth()

                        )
                    }
                }
            }
            // Previous Button
            IconButton(
                onClick = {
                    if (currentPageIndex > 0) {
                        currentPageIndex--
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(currentPageIndex)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous",
                    tint = Color.Black
                )
            }

            // Next Button
            IconButton(
                onClick = {
                    if (currentPageIndex < pageCount - 1) {
                        currentPageIndex++
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(currentPageIndex)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.Black
                )
            }
        }
    }
}