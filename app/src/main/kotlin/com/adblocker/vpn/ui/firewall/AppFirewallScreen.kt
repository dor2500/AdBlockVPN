package com.adblocker.vpn.ui.firewall

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFirewallScreen(
    onBack: () -> Unit,
    viewModel: AppFirewallViewModel = viewModel()
) {
    val apps by viewModel.installedApps.collectAsState()
    val blockedApps by viewModel.blockedApps.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val context = LocalContext.current
    val view = LocalView.current
    
    val filteredApps = apps.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.packageName.contains(searchQuery, ignoreCase = true) 
    }

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("App Firewall", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Apps", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha=0.2f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Text(
                    text = "Blocked apps will have their internet severed. Note: You may need to Force Stop an app to clear its cached connections.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            items(filteredApps) { app ->
                val isBlocked = blockedApps.contains(app.packageName)
                val cardColor = if (isBlocked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                val borderColor = if (isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground.copy(alpha=0.1f)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .border(2.dp, borderColor, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // App Icon
                        AsyncImage(
                            model = app.icon,
                            contentDescription = app.name,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = app.name, color = if (isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text(text = app.packageName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        
                        Switch(
                            checked = isBlocked,
                            onCheckedChange = { 
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.toggleAppBlock(app.packageName, it) 
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onError,
                                checkedTrackColor = MaterialTheme.colorScheme.error,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                    
                    // Force Stop Button
                    AnimatedVisibility(
                        visible = isBlocked,
                        enter = expandVertically(animationSpec = tween(300)),
                        exit = shrinkVertically(animationSpec = tween(300))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.parse("package:${app.packageName}")
                                    }
                                    context.startActivity(intent)
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("FORCE STOP TO APPLY", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
