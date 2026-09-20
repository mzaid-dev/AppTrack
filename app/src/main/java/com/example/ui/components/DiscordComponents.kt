package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContractEntity
import com.example.data.model.ProjectEntity
import com.example.ui.theme.DiscordAlertRed
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordBlurpleLight
import com.example.ui.theme.DiscordCard
import com.example.ui.theme.DiscordCardHover
import com.example.ui.theme.DiscordDevBlue
import com.example.ui.theme.DiscordDivider
import com.example.ui.theme.DiscordGoldPayout
import com.example.ui.theme.DiscordInput
import com.example.ui.theme.DiscordLiveGreen
import com.example.ui.theme.DiscordLiveGreenGlow
import com.example.ui.theme.DiscordReviewAmber
import com.example.ui.theme.DiscordSidebar
import com.example.ui.theme.DiscordTextHeader
import com.example.ui.theme.DiscordTextInteractive
import com.example.ui.theme.DiscordTextMuted
import com.example.ui.theme.DiscordTextNormal

@Composable
fun DiscordTopBar(
    title: String,
    subtitle: String,
    currentRole: String,
    unreadNotifs: Int,
    onRoleClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DiscordSidebar,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Discord styled server icon / logo
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(DiscordBlurple, Color(0xFF4752C4))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = "LaunchPulse Logo",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = DiscordTextHeader,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        color = DiscordTextMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Role Switcher Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (currentRole == "DEVELOPER") DiscordBlurple.copy(alpha = 0.2f) else DiscordLiveGreen.copy(alpha = 0.2f))
                        .border(
                            1.dp,
                            if (currentRole == "DEVELOPER") DiscordBlurple else DiscordLiveGreen,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onRoleClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("role_switcher_pill"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (currentRole == "DEVELOPER") DiscordBlurple else DiscordLiveGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentRole == "DEVELOPER") "Dev Mode" else "Client View",
                            color = DiscordTextHeader,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Mode",
                            tint = DiscordTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Notification Bell
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.testTag("notification_bell_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifs > 0) {
                                Badge(
                                    containerColor = DiscordAlertRed,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (unreadNotifs > 9) "9+" else unreadNotifs.toString(),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (unreadNotifs > 0) DiscordTextHeader else DiscordTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bg, text, label) = when (status) {
        "LIVE" -> Triple(DiscordLiveGreen.copy(alpha = 0.2f), DiscordLiveGreen, "LIVE ON STORE")
        "IN_REVIEW" -> Triple(DiscordReviewAmber.copy(alpha = 0.2f), DiscordReviewAmber, "IN REVIEW")
        "DEVELOPMENT" -> Triple(DiscordDevBlue.copy(alpha = 0.2f), DiscordDevBlue, "IN DEV")
        "ACTION_REQUIRED" -> Triple(DiscordAlertRed.copy(alpha = 0.2f), DiscordAlertRed, "ACTION NEEDED")
        else -> Triple(Color(0xFF4E5058).copy(alpha = 0.3f), DiscordTextMuted, "DRAFT")
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, text.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            if (status == "LIVE") {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(DiscordLiveGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else if (status == "IN_REVIEW") {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(DiscordReviewAmber)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                color = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProjectItemCard(
    project: ProjectEntity,
    isDeveloperMode: Boolean,
    onMarkLiveClick: () -> Unit,
    onOpenStoreClick: () -> Unit,
    onEditStatusClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DiscordCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (project.status == "LIVE") DiscordLiveGreen.copy(alpha = 0.5f) else DiscordDivider
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category Icon, Title, Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (project.category == "GAME") DiscordBlurple.copy(alpha = 0.25f)
                                else DiscordDevBlue.copy(alpha = 0.25f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (project.category == "GAME") Icons.Default.Gamepad else Icons.Default.PhoneAndroid,
                            contentDescription = project.category,
                            tint = if (project.category == "GAME") DiscordBlurple else DiscordDevBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = project.title,
                            color = DiscordTextHeader,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${project.category} • ${project.platform.replace("_", " ")}",
                                color = DiscordTextMuted,
                                fontSize = 12.sp
                            )
                            Text(
                                text = " • v${project.versionName}",
                                color = DiscordTextInteractive,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                StatusBadge(status = project.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Package Name & Release Notes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DiscordInput)
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "ID: ${project.packageName}",
                        color = DiscordTextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (project.releaseNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = project.releaseNotes,
                            color = DiscordTextNormal,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Milestone Payout & Action Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Live Release Milestone",
                        color = DiscordTextMuted,
                        fontSize = 11.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$${String.format("%,.2f", project.payoutMilestoneAmount)}",
                            color = DiscordGoldPayout,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (project.status == "LIVE") {
                            Surface(
                                color = DiscordLiveGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Unlocked",
                                        tint = DiscordLiveGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "UNLOCKED",
                                        color = DiscordLiveGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive Action Button
                if (isDeveloperMode) {
                    if (project.status != "LIVE") {
                        Button(
                            onClick = onMarkLiveClick,
                            colors = ButtonDefaults.buttonColors(containerColor = DiscordLiveGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_publish_live_${project.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = "Publish & Go Live",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mark Live",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = onOpenStoreClick,
                            colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_store_link_${project.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "View Store",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Store",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    // Client Mode
                    if (project.status == "LIVE") {
                        Button(
                            onClick = onOpenStoreClick,
                            colors = ButtonDefaults.buttonColors(containerColor = DiscordLiveGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_client_view_store_${project.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open Store",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Open on Store",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Surface(
                            color = DiscordCardHover,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DiscordDivider)
                        ) {
                            Text(
                                text = "Awaiting Live Launch",
                                color = DiscordTextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContractCard(
    contract: ContractEntity,
    isSelected: Boolean,
    appsCount: Int,
    gamesCount: Int,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DiscordCardHover else DiscordCard
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) DiscordBlurple else DiscordDivider
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("contract_card_${contract.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contract.name,
                        color = DiscordTextHeader,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Client: ${contract.clientName} (${contract.companyName})",
                        color = DiscordTextMuted,
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Open Contract",
                    tint = if (isSelected) DiscordBlurple else DiscordTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges for Apps & Games
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Surface(
                        color = DiscordDevBlue.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$appsCount Apps",
                            color = DiscordDevBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = DiscordBlurple.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$gamesCount Games",
                            color = DiscordBlurpleLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Live Payout: $${String.format("%,.0f", contract.payoutOnLive)}",
                    color = DiscordGoldPayout,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
