package com.swaply.app.ui.sell

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellScreen(
    viewModel: SellViewModel,
    onPublishSuccess: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onAddPhoto(uri.toString())
        }
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is SellUiEvent.PublishSuccess -> {
                    snackbarHostState.showSnackbar("Listing published successfully!")
                    onPublishSuccess(event.productId)
                }
                is SellUiEvent.PublishError -> {
                    snackbarHostState.showSnackbar("Publish failed: ${event.message}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Listing") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Photos", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(90.dp)
                            .testTag("add_photo_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Text("Add", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                items(formState.imageUris) { uri ->
                    Box(modifier = Modifier.padding(start = 8.dp).size(90.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Listing photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.onRemovePhoto(uri) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove photo")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = formState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title *") },
                isError = formState.titleError != null,
                supportingText = {
                    if (formState.titleError != null) Text(formState.titleError!!)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sell_title_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formState.price,
                onValueChange = viewModel::onPriceChange,
                label = { Text("Price ($) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = formState.priceError != null,
                supportingText = {
                    if (formState.priceError != null) Text(formState.priceError!!)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sell_price_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formState.category,
                onValueChange = viewModel::onCategoryChange,
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description *") },
                minLines = 4,
                isError = formState.descriptionError != null,
                supportingText = {
                    if (formState.descriptionError != null) Text(formState.descriptionError!!)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sell_desc_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::submit,
                enabled = !formState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("publish_button")
            ) {
                if (formState.isSubmitting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Publish Listing")
                }
            }
        }
    }
}
