package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContractEntity
import com.example.data.model.ProjectEntity
import com.example.ui.components.ProjectItemCard
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordCard
import com.example.ui.theme.DiscordDevBlue
import com.example.ui.theme.DiscordDivider
import com.example.ui.theme.DiscordGoldPayout
import com.example.ui.theme.DiscordInput
import com.example.ui.theme.DiscordLiveGreen
import com.example.ui.theme.DiscordMainBg
import com.example.ui.theme.DiscordReviewAmber
import com.example.ui.theme.DiscordSidebar
import com.example.ui.theme.DiscordTextHeader
import com.example.ui.theme.DiscordTextMuted
import com.example.ui.theme.DiscordTextNormal

@Composable
fun ClientPortalScreen(
    contract: ContractEntity?,
    projects: List<ProjectEntity>,
    selectedCategoryTab: String, // "APP" or "GAME"
    onSelectCategoryTab: (String) -> Unit,
    onOpenCommunityHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contractProjects = projects.filter { it.contractId == (contract?.id ?: "") }
    val tabFilteredProjects = contractProjects.filter { it.category == selectedCategoryTab }

    val liveProjects = contractProjects.filter { it.status == "LIVE" }
    val unlockedPayout = liveProjects.sumOf { it.payoutMilestoneAmount }

    val infiniteTransition = rememberInfiniteTransition(label = "beacon")
    val beaconScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_scale"
    )

    Box(modifier = modifier.fillMaxSize().background(DiscordMainBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Welcome Client Hero Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DiscordSidebar),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DiscordDivider),
                    modifier = Modifier.fillMaxWidth().testTag("client_portal_hero")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "CLIENT PORTAL",
                                    color = DiscordLiveGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = contract?.name ?: "Client Contract Suite",
                                    color = DiscordTextHeader,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Welcome, ${contract?.clientName ?: "Valued Client"} (${contract?.companyName ?: "Client"})",
                                    color = DiscordTextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(DiscordLiveGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RocketLaunch,
                                    contentDescription = null,
                                    tint = DiscordLiveGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Release Status Highlight Box
                        if (liveProjects.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DiscordInput)
                                    .border(1.dp, DiscordLiveGreen.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .scale(beaconScale)
                                                .clip(CircleShape)
                                                .background(DiscordLiveGreen)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "STATUS: ${liveProjects.size} PROJECT(S) VERIFIED LIVE",
                                            color = DiscordLiveGreen,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Your application is published and publicly downloadable! As per the contract terms, the live release milestone payment has unlocked.",
                                        color = DiscordTextNormal,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Milestone Payment Unlocked:",
                                                color = DiscordTextMuted,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = "$${String.format("%,.2f", unlockedPayout)}",
                                                color = DiscordGoldPayout,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        val firstLive = liveProjects.firstOrNull()
                                        if (firstLive != null && firstLive.liveStoreUrl.isNotBlank()) {
                                            Button(
                                                onClick = {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(firstLive.liveStoreUrl))
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {}
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = DiscordLiveGreen),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Open on Store", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Pending Live Launch
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DiscordInput)
                                    .border(1.dp, DiscordReviewAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.RocketLaunch,
                                        contentDescription = null,
                                        tint = DiscordReviewAmber,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Awaiting Live Store Verification",
                                            color = DiscordReviewAmber,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Developer is preparing your build. You will receive an instant notification when it goes live.",
                                            color = DiscordTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2 Tabs for Client: APPS and GAMES
            item {
                TabRow(
                    selectedTabIndex = if (selectedCategoryTab == "APP") 0 else 1,
                    containerColor = DiscordInput,
                    contentColor = DiscordTextHeader,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[if (selectedCategoryTab == "APP") 0 else 1]),
                            color = if (selectedCategoryTab == "APP") DiscordDevBlue else DiscordBlurple
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedCategoryTab == "APP",
                        onClick = { onSelectCategoryTab("APP") },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = if (selectedCategoryTab == "APP") DiscordDevBlue else DiscordTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Apps (${contractProjects.count { it.category == "APP" }})",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCategoryTab == "APP") DiscordTextHeader else DiscordTextMuted
                                )
                            }
                        },
                        modifier = Modifier.testTag("client_tab_apps")
                    )
                    Tab(
                        selected = selectedCategoryTab == "GAME",
                        onClick = { onSelectCategoryTab("GAME") },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Gamepad,
                                    contentDescription = null,
                                    tint = if (selectedCategoryTab == "GAME") DiscordBlurple else DiscordTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Games (${contractProjects.count { it.category == "GAME" }})",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCategoryTab == "GAME") DiscordTextHeader else DiscordTextMuted
                                )
                            }
                        },
                        modifier = Modifier.testTag("client_tab_games")
                    )
                }
            }

            // Projects List for Tab
            if (tabFilteredProjects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No ${if (selectedCategoryTab == "APP") "Apps" else "Games"} listed in this contract.",
                            color = DiscordTextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(tabFilteredProjects) { project ->
                    ProjectItemCard(
                        project = project,
                        isDeveloperMode = false,
                        onMarkLiveClick = {},
                        onOpenStoreClick = {
                            if (project.liveStoreUrl.isNotBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(project.liveStoreUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to Chat with Developer in Discord Community
        FloatingActionButton(
            onClick = onOpenCommunityHub,
            containerColor = DiscordBlurple,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("client_fab_community")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Forum, contentDescription = "Community Chat")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Chat with Developer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
