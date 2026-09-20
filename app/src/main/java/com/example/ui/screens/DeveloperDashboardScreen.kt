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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Gamepad
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContractEntity
import com.example.data.model.ProjectEntity
import com.example.ui.components.ProjectItemCard
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordCard
import com.example.ui.theme.DiscordCardHover
import com.example.ui.theme.DiscordDevBlue
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

@Composable
fun DeveloperDashboardScreen(
    contracts: List<ContractEntity>,
    projects: List<ProjectEntity>,
    selectedContractId: String?,
    selectedCategoryTab: String, // "APP" or "GAME"
    onSelectContract: (String) -> Unit,
    onSelectCategoryTab: (String) -> Unit,
    onAddContractClick: () -> Unit,
    onAddProjectClick: () -> Unit,
    onMarkLiveClick: (ProjectEntity) -> Unit,
    onOpenCommunityHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentContract = contracts.find { it.id == selectedContractId } ?: contracts.firstOrNull()

    // Filter projects for the active contract & selected tab (APP or GAME)
    val contractProjects = projects.filter { it.contractId == (currentContract?.id ?: "") }
    val tabFilteredProjects = contractProjects.filter { it.category == selectedCategoryTab }

    val liveCount = projects.count { it.status == "LIVE" }
    val totalPayoutsUnlocked = projects.filter { it.status == "LIVE" }.sumOf { it.payoutMilestoneAmount }

    Box(modifier = modifier.fillMaxSize().background(DiscordMainBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Quick Stats Ribbon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Stat 1: Live Apps
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DiscordSidebar),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "LIVE ON STORE", color = DiscordLiveGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$liveCount", color = DiscordTextHeader, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Publicly released", color = DiscordTextMuted, fontSize = 10.sp)
                        }
                    }

                    // Stat 2: Unlocked Milestone Payouts
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DiscordSidebar),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "LIVE MILESTONES", color = DiscordGoldPayout, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format("%,.0f", totalPayoutsUnlocked)}",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "Unlocked upon live", color = DiscordTextMuted, fontSize = 10.sp)
                        }
                    }

                    // Stat 3: Contracts
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DiscordSidebar),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "CONTRACTS", color = DiscordBlurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "${contracts.size}", color = DiscordTextHeader, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Active clients", color = DiscordTextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }

            // Client Contracts Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLIENT CONTRACTS",
                        color = DiscordTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Button(
                        onClick = onAddContractClick,
                        colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_contract_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Contract", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Contracts horizontal selector
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(contracts) { contract ->
                        val isSelected = contract.id == currentContract?.id
                        val appCount = projects.count { it.contractId == contract.id && it.category == "APP" }
                        val gameCount = projects.count { it.contractId == contract.id && it.category == "GAME" }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) DiscordCardHover else DiscordSidebar
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) DiscordBlurple else DiscordDivider
                            ),
                            modifier = Modifier
                                .width(220.dp)
                                .clickable { onSelectContract(contract.id) }
                                .testTag("contract_chip_${contract.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = contract.name,
                                    color = DiscordTextHeader,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = contract.clientName,
                                    color = DiscordTextMuted,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$appCount Apps • $gameCount Games",
                                        color = DiscordTextInteractive,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "$${String.format("%,.0f", contract.payoutOnLive)}",
                                        color = DiscordGoldPayout,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Selected Contract Banner
            item {
                if (currentContract != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DiscordSidebar),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DiscordDivider),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(DiscordBlurple.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.BusinessCenter,
                                            contentDescription = null,
                                            tint = DiscordBlurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = currentContract.name,
                                            color = DiscordTextHeader,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${currentContract.clientName} (${currentContract.clientEmail})",
                                            color = DiscordTextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "Payout on Live: $${String.format("%,.2f", currentContract.payoutOnLive)}",
                                    color = DiscordGoldPayout,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (currentContract.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "📌 Contract Clause: ${currentContract.notes}",
                                    color = DiscordTextNormal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2 Tabs: APPS or GAMES (Mandated by user prompt: "in this 2 tab is app or game")
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
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
                            modifier = Modifier.testTag("tab_apps")
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
                            modifier = Modifier.testTag("tab_games")
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = onAddProjectClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCategoryTab == "APP") DiscordDevBlue else DiscordBlurple
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_project_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedCategoryTab == "APP") "Add App" else "Add Game",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Projects List for current tab
            if (tabFilteredProjects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedCategoryTab == "APP") Icons.Default.PhoneAndroid else Icons.Default.Gamepad,
                                contentDescription = null,
                                tint = DiscordTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No ${if (selectedCategoryTab == "APP") "Apps" else "Games"} in this contract yet.",
                                color = DiscordTextMuted,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = onAddProjectClick) {
                                Text("Add ${if (selectedCategoryTab == "APP") "App" else "Game"} Now")
                            }
                        }
                    }
                }
            } else {
                items(tabFilteredProjects) { project ->
                    ProjectItemCard(
                        project = project,
                        isDeveloperMode = true,
                        onMarkLiveClick = { onMarkLiveClick(project) },
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

        // Floating Action Button to open Discord Community Hub
        FloatingActionButton(
            onClick = onOpenCommunityHub,
            containerColor = DiscordBlurple,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_community_hub")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Forum, contentDescription = "Discord Community")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Community Hub", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
