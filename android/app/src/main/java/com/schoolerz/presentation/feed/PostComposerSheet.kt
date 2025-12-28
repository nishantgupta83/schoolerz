package com.schoolerz.presentation.feed

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.schoolerz.domain.model.PostType
import com.schoolerz.presentation.theme.Tokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostComposerSheet(onDismiss: () -> Unit, onSubmit: (PostType, String, String) -> Unit) {
    var type by remember { mutableStateOf(PostType.OFFER) }
    var body by remember { mutableStateOf("") }
    var neighborhood by remember { mutableStateOf("Downtown") }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val hasContent = body.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = { if (hasContent) showDiscardDialog = true else onDismiss() }
    ) {
        Column(modifier = Modifier.padding(Tokens.Spacing.m)) {
            Text("New Post", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Tokens.Spacing.m))

            Text("Type", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(Tokens.Spacing.s)) {
                FilterChip(selected = type == PostType.OFFER, onClick = { type = PostType.OFFER }, label = { Text("Offer") })
                FilterChip(selected = type == PostType.REQUEST, onClick = { type = PostType.REQUEST }, label = { Text("Request") })
            }
            Spacer(Modifier.height(Tokens.Spacing.m))

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )
            Spacer(Modifier.height(Tokens.Spacing.m))

            Button(
                onClick = { onSubmit(type, body, neighborhood) },
                enabled = hasContent,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Post") }
            Spacer(Modifier.height(Tokens.Spacing.l))
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Draft?") },
            confirmButton = { TextButton(onClick = { showDiscardDialog = false; onDismiss() }) { Text("Discard") } },
            dismissButton = { TextButton(onClick = { showDiscardDialog = false }) { Text("Keep Editing") } }
        )
    }
}
