package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ContractEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.ProjectEntity
import com.example.ui.CelebrationData
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordCard
import com.example.ui.theme.DiscordCardHover
import com.example.ui.theme.DiscordDevBlue
import com.example.ui.theme.DiscordDivider
import com.example.ui.theme.DiscordGoldPayout
import com.example.ui.theme.DiscordInput
import com.example.ui.theme.DiscordLiveGreen
import com.example.ui.theme.DiscordSidebar
import com.example.ui.theme.DiscordTextHeader
import com.example.ui.theme.DiscordTextMuted
import com.example.ui.theme.DiscordTextNormal

@Composable
fun LiveTriggerDialog(
    project: ProjectEntity,
    onDismiss: () -> Unit,
    onConfirmLive: (liveUrl: String, notes: String) -> Unit
) {
    val defaultUrl = if (project.liveStoreUrl.isNotBlank()) project.liveStoreUrl
    else "https://play.google.com/store/apps/details?id=${project.packageName}"
    var liveUrl by remember { mutableStateOf(defaultUrl) }
    var releaseNotes by remember {
        mutableStateOf("Production build v${project.versionName} is officially verified live on the store.")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DiscordSidebar,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DiscordLiveGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = DiscordLiveGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Mark ${project.title} as LIVE",
                        color = DiscordTextHeader,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Triggers Client Notification & Payout",
                        color = DiscordTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    text = "Once published as LIVE, the client will immediately receive an instant notification, the green beacon will activate, and the milestone payout of $${String.format("%,.2f", project.payoutMilestoneAmount)} will be unlocked.",
                    color = DiscordTextNormal,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Live Store URL",
                    color = DiscordTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = liveUrl,
                    onValueChange = { liveUrl = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DiscordInput,
                        unfocusedContainerColor = DiscordInput,
                        focusedBorderColor = DiscordLiveGreen,
                        unfocusedBorderColor = DiscordDivider,
                        focusedTextColor = DiscordTextHeader,
                        unfocusedTextColor = DiscordTextNormal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_live_url")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Release Notes for Client",
                    color = DiscordTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = releaseNotes,
                    onValueChange = { releaseNotes = it },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DiscordInput,
                        unfocusedContainerColor = DiscordInput,
                        focusedBorderColor = DiscordBlurple,
                        unfocusedBorderColor = DiscordDivider,
                        focusedTextColor = DiscordTextHeader,
                        unfocusedTextColor = DiscordTextNormal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_release_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmLive(liveUrl, releaseNotes) },
                colors = ButtonDefaults.buttonColors(containerColor = DiscordLiveGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_publish_live_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Publish & Notify Client", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Cancel", color = DiscordTextMuted)
            }
        }
    )
}

@Composable
fun CelebrationModal(
    celebration: CelebrationData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DiscordSidebar),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, DiscordLiveGreen),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("celebration_modal")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(DiscordLiveGreen.copy(alpha = 0.4f), DiscordLiveGreen.copy(alpha = 0.1f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = DiscordLiveGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "🎉 APP IS OFFICIALLY LIVE!",
                    color = DiscordLiveGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${celebration.project.title} (${celebration.project.versionName})",
                    color = DiscordTextHeader,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Client notification sent & community announcement posted.",
                    color = DiscordTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Milestone Payout Unlocked Receipt Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DiscordInput)
                        .border(1.dp, DiscordGoldPayout.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DiscordGoldPayout,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PAYMENT MILESTONE UNLOCKED",
                                color = DiscordGoldPayout,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$${String.format("%,.2f", celebration.payoutAmount)}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Due for settlement upon live verification",
                            color = DiscordTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (celebration.liveUrl.isNotBlank()) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(celebration.liveUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "View Store", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = DiscordLiveGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_close_celebration")
                    ) {
                        Text(text = "Awesome!", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RoleSwitcherModal(
    currentRole: String,
    contracts: List<ContractEntity>,
    selectedContractId: String?,
    onDismiss: () -> Unit,
    onSelectRole: (role: String, contractId: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DiscordSidebar,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = DiscordBlurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Switch Active Workspace Side",
                    color = DiscordTextHeader,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Toggle between Developer Studio and Client Portal to test the end-to-end live tracking & notification flow:",
                    color = DiscordTextNormal,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Developer Option
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentRole == "DEVELOPER") DiscordBlurple.copy(alpha = 0.2f) else DiscordCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (currentRole == "DEVELOPER") DiscordBlurple else DiscordDivider
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val targetId = selectedContractId ?: contracts.firstOrNull()?.id ?: ""
                            onSelectRole("DEVELOPER", targetId)
                        }
                        .testTag("select_dev_role_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DiscordBlurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Developer / Studio Mode",
                                color = DiscordTextHeader,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Manage contracts, add apps/games, mark live",
                                color = DiscordTextMuted,
                                fontSize = 11.sp
                            )
                        }
                        if (currentRole == "DEVELOPER") {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DiscordBlurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Client Option with contract selection
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentRole == "CLIENT") DiscordLiveGreen.copy(alpha = 0.2f) else DiscordCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (currentRole == "CLIENT") DiscordLiveGreen else DiscordDivider
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val targetId = selectedContractId ?: contracts.firstOrNull()?.id ?: ""
                            onSelectRole("CLIENT", targetId)
                        }
                        .testTag("select_client_role_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DiscordLiveGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Client Portal View",
                                color = DiscordTextHeader,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "View live status, notifications & milestone payout",
                                color = DiscordTextMuted,
                                fontSize = 11.sp
                            )
                        }
                        if (currentRole == "CLIENT") {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DiscordLiveGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close", color = Color.White)
            }
        }
    )
}

@Composable
fun NotificationCenterDialog(
    notifications: List<NotificationEntity>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (NotificationEntity) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DiscordSidebar),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .testTag("notification_center_modal")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = DiscordBlurple,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Release Notifications",
                            color = DiscordTextHeader,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onMarkAllRead) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark All Read",
                                tint = DiscordTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = DiscordTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notifications yet. Live launches will alert here!",
                            color = DiscordTextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(notifications) { notif ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!notif.isRead) DiscordCardHover else DiscordCard
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (!notif.isRead) DiscordLiveGreen.copy(alpha = 0.5f) else DiscordDivider
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNotificationClick(notif) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = notif.title,
                                            color = if (!notif.isRead) DiscordLiveGreen else DiscordTextHeader,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (!notif.isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(DiscordLiveGreen)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notif.message,
                                        color = DiscordTextNormal,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                    if (notif.storeUrl != null && notif.storeUrl.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "🔗 ${notif.storeUrl}",
                                            color = DiscordBlurple,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddContractModal(
    onDismiss: () -> Unit,
    onAddContract: (
        name: String,
        clientName: String,
        email: String,
        company: String,
        budget: Double,
        payoutOnLive: Double,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var totalBudget by remember { mutableStateOf("10000") }
    var payoutOnLive by remember { mutableStateOf("3500") }
    var notes by remember { mutableStateOf("Remaining milestone payable upon Play Store live verification.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DiscordSidebar,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Create New Client Contract",
                color = DiscordTextHeader,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Contract Name (e.g. Phoenix Studios)", color = DiscordTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DiscordInput,
                            unfocusedContainerColor = DiscordInput,
                            focusedBorderColor = DiscordBlurple,
                            unfocusedBorderColor = DiscordDivider,
                            focusedTextColor = DiscordTextHeader,
                            unfocusedTextColor = DiscordTextNormal
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_contract_name")
                    )
                }
                item {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client Full Name", color = DiscordTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DiscordInput,
                            unfocusedContainerColor = DiscordInput,
                            focusedBorderColor = DiscordBlurple,
                            unfocusedBorderColor = DiscordDivider,
                            focusedTextColor = DiscordTextHeader,
                            unfocusedTextColor = DiscordTextNormal
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_client_name")
                    )
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Client Email (Login credential)", color = DiscordTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DiscordInput,
                            unfocusedContainerColor = DiscordInput,
                            focusedBorderColor = DiscordBlurple,
                            unfocusedBorderColor = DiscordDivider,
                            focusedTextColor = DiscordTextHeader,
                            unfocusedTextColor = DiscordTextNormal
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_client_email")
                    )
                }
                item {
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Client Company", color = DiscordTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DiscordInput,
                            unfocusedContainerColor = DiscordInput,
                            focusedBorderColor = DiscordBlurple,
                            unfocusedBorderColor = DiscordDivider,
                            focusedTextColor = DiscordTextHeader,
                            unfocusedTextColor = DiscordTextNormal
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = totalBudget,
                            onValueChange = { totalBudget = it },
                            label = { Text("Total Budget ($)", color = DiscordTextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DiscordInput,
                                unfocusedContainerColor = DiscordInput,
                                focusedBorderColor = DiscordBlurple,
                                unfocusedBorderColor = DiscordDivider,
                                focusedTextColor = DiscordTextHeader,
                                unfocusedTextColor = DiscordTextNormal
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = payoutOnLive,
                            onValueChange = { payoutOnLive = it },
                            label = { Text("Payout on Live ($)", color = DiscordGoldPayout) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DiscordInput,
                                unfocusedContainerColor = DiscordInput,
                                focusedBorderColor = DiscordGoldPayout,
                                unfocusedBorderColor = DiscordDivider,
                                focusedTextColor = DiscordTextHeader,
                                unfocusedTextColor = DiscordTextNormal
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Milestone Terms & Notes", color = DiscordTextMuted) },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DiscordInput,
                            unfocusedContainerColor = DiscordInput,
                            focusedBorderColor = DiscordBlurple,
                            unfocusedBorderColor = DiscordDivider,
                            focusedTextColor = DiscordTextHeader,
                            unfocusedTextColor = DiscordTextNormal
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && clientName.isNotBlank()) {
                        onAddContract(
                            name,
                            clientName,
                            email.ifBlank { "client@example.com" },
                            company.ifBlank { "Independent" },
                            totalBudget.toDoubleOrNull() ?: 10000.0,
                            payoutOnLive.toDoubleOrNull() ?: 3500.0,
                            notes
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("submit_create_contract_btn")
            ) {
                Text("Create Contract", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel", color = DiscordTextMuted)
            }
        }
    )
}

@Composable
fun AddProjectModal(
    contractId: String,
    initialCategory: String,
    onDismiss: () -> Unit,
    onAddProject: (
        title: String,
        category: String,
        platform: String,
        packageName: String,
        version: String,
        build: Int,
        payout: Double,
        notes: String,
        iconKey: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory) } // "APP" or "GAME"
    var platform by remember { mutableStateOf("GOOGLE_PLAY") }
    var packageName by remember { mutableStateOf("") }
    var version by remember { mutableStateOf("1.0.0") }
    var buildNumber by remember { mutableStateOf("1") }
    var payoutMilestone by remember { mutableStateOf("2500") }
    var releaseNotes by remember { mutableStateOf("Initial release build ready for store testing.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DiscordSidebar,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Add App or Game to Contract",
                color = DiscordTextHeader,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Two Tabs: APP or GAME
                item {
                    TabRow(
                        selectedTabIndex = if (selectedCategory == "APP") 0 else 1,
                        containerColor = DiscordInput,
                        contentColor = DiscordTextHeader,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[if (selectedCategory == "APP") 0 else 1]),
                                color = if (selectedCategory == "APP") DiscordDevBlue else DiscordBlurple
                            )
                        },
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = selectedCategory == "APP",
                            onClick = { selectedCategory = "APP" },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("App Project", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                        Tab(
                            selected = selectedCategory == "GAME",
                            onClick = { selectedCategory = "GAME" },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Gamepad, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Game Project", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (packageName.isBlank() && it.isNotBlank()) {
                                packageName = "com.client." + it.lowercase().replace(" ", "")
                            }
                        },
                        label = { Text("Title / Game Name", color = DiscordTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DiscordInput,
                            unfocusedContainerColor = DiscordInput,
                            focusedBorderColor = DiscordBlurple,
                            unfocusedBorderColor = DiscordDivider,
                            focusedTextColor = DiscordTextHeader,
                            unfocusedTextColor = DiscordTextNormal
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_project_title")
                    )
                }

                item {
                    OutlinedTextField(
                        value = packageName,
                        onValueChange = { packageName = it },
                        label = { Text("Package Name / Bundle ID", color = DiscordTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DiscordInput,
                            unfocusedContainerColor = DiscordInput,
                            focusedBorderColor = DiscordBlurple,
                            unfocusedBorderColor = DiscordDivider,
                            focusedTextColor = DiscordTextHeader,
                            unfocusedTextColor = DiscordTextNormal
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = version,
                            onValueChange = { version = it },
                            label = { Text("Version", color = DiscordTextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DiscordInput,
                                unfocusedContainerColor = DiscordInput,
                                focusedBorderColor = DiscordBlurple,
                                unfocusedBorderColor = DiscordDivider,
                                focusedTextColor = DiscordTextHeader,
                                unfocusedTextColor = DiscordTextNormal
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = payoutMilestone,
                            onValueChange = { payoutMilestone = it },
                            label = { Text("Live Payout ($)", color = DiscordGoldPayout) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DiscordInput,
                                unfocusedContainerColor = DiscordInput,
                                focusedBorderColor = DiscordGoldPayout,
                                unfocusedBorderColor = DiscordDivider,
                                focusedTextColor = DiscordTextHeader,
                                unfocusedTextColor = DiscordTextNormal
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("input_payout_milestone")
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = releaseNotes,
                        onValueChange = { releaseNotes = it },
                        label = { Text("Build Description / Release Notes", color = DiscordTextMuted) },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DiscordInput,
                            unfocusedContainerColor = DiscordInput,
                            focusedBorderColor = DiscordBlurple,
                            unfocusedBorderColor = DiscordDivider,
                            focusedTextColor = DiscordTextHeader,
                            unfocusedTextColor = DiscordTextNormal
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddProject(
                            title,
                            selectedCategory,
                            platform,
                            packageName.ifBlank { "com.example." + title.lowercase().replace(" ", "") },
                            version,
                            buildNumber.toIntOrNull() ?: 1,
                            payoutMilestone.toDoubleOrNull() ?: 2000.0,
                            releaseNotes,
                            if (selectedCategory == "GAME") "gamepad" else "phone"
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("submit_add_project_btn")
            ) {
                Text("Add Project", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel", color = DiscordTextMuted)
            }
        }
    )
}
