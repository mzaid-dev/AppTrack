package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommunityChannelEntity
import com.example.data.model.CommunityMessageEntity
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordCard
import com.example.ui.theme.DiscordCardHover
import com.example.ui.theme.DiscordDivider
import com.example.ui.theme.DiscordGoldPayout
import com.example.ui.theme.DiscordInput
import com.example.ui.theme.DiscordLiveGreen
import com.example.ui.theme.DiscordMainBg
import com.example.ui.theme.DiscordSidebar
import com.example.ui.theme.DiscordTextHeader
import com.example.ui.theme.DiscordTextInteractive
import com.example.ui.theme.DiscordTextMuted
import com.example.ui.theme.DiscordTextNormal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommunityChatScreen(
    channels: List<CommunityChannelEntity>,
    selectedChannelId: String,
    messages: List<CommunityMessageEntity>,
    currentRole: String,
    onSelectChannel: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val activeChannel = channels.find { it.id == selectedChannelId } ?: channels.firstOrNull()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DiscordMainBg)
    ) {
        // Discord Channel Header Bar
        Surface(
            color = DiscordSidebar,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DiscordTextHeader
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = DiscordTextMuted,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeChannel?.name ?: "general",
                            color = DiscordTextHeader,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeChannel?.description ?: "Discord Community Channel",
                            color = DiscordTextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Discord Channel Pills List
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(channels) { channel ->
                        val isSelected = channel.id == selectedChannelId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DiscordCardHover else DiscordInput)
                                .border(
                                    1.dp,
                                    if (isSelected) DiscordBlurple else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectChannel(channel.id) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("channel_pill_${channel.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (channel.name) {
                                        "live-releases" -> Icons.Default.RocketLaunch
                                        "announcements" -> Icons.Default.Campaign
                                        "payout-verification" -> Icons.Default.Paid
                                        else -> Icons.Default.Tag
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) DiscordBlurple else DiscordTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = channel.name,
                                    color = if (isSelected) DiscordTextHeader else DiscordTextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Messages Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // Channel Topic Header Notice
                Card(
                    colors = CardDefaults.cardColors(containerColor = DiscordCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null,
                                tint = DiscordBlurple,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Welcome to #${activeChannel?.name}!",
                                color = DiscordTextHeader,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This is the start of the #${activeChannel?.name} channel. Official live app releases and milestone updates will broadcast here.",
                            color = DiscordTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            items(messages) { message ->
                DiscordMessageItem(
                    message = message,
                    onOpenStore = { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )
            }
        }

        // Discord Bottom Input Area
        Surface(
            color = DiscordSidebar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Message #${activeChannel?.name}...",
                            color = DiscordTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DiscordInput,
                        unfocusedContainerColor = DiscordInput,
                        focusedBorderColor = DiscordBlurple,
                        unfocusedBorderColor = DiscordDivider,
                        focusedTextColor = DiscordTextHeader,
                        unfocusedTextColor = DiscordTextNormal
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(DiscordBlurple)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DiscordMessageItem(
    message: CommunityMessageEntity,
    onOpenStore: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("msg_${message.id}"),
        verticalAlignment = Alignment.Top
    ) {
        // Author Avatar
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(message.authorAvatarColor)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message.authorName.take(1).uppercase(),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Author Name, Role Badge, and Timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.authorName,
                    color = DiscordTextHeader,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Discord Role Tag
                val (roleBg, roleText) = when (message.authorRole) {
                    "SYSTEM_BOT" -> Pair(DiscordBlurple, "BOT")
                    "DEVELOPER" -> Pair(Color(0xFF00A8FC), "DEV")
                    "CLIENT" -> Pair(DiscordLiveGreen, "CLIENT")
                    else -> Pair(DiscordDivider, "USER")
                }

                Surface(
                    color = roleBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = roleText,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = timeFormatted,
                    color = DiscordTextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Message text
            Text(
                text = message.content,
                color = DiscordTextNormal,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // Discord Rich Embed if this is a Live Release Announcement
            if (message.isLiveAnnouncement && message.liveUrl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = DiscordCard),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DiscordLiveGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = DiscordLiveGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OFFICIAL STORE LIVE RELEASE",
                                color = DiscordLiveGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (message.projectTitle != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.projectTitle,
                                color = DiscordTextHeader,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { onOpenStore(message.liveUrl) },
                            colors = ButtonDefaults.buttonColors(containerColor = DiscordLiveGreen),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Open on Store", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
