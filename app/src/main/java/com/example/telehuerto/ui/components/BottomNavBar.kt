package com.example.telehuerto.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.ui.theme.AlertCritical
import com.example.telehuerto.ui.theme.CardBackground
import com.example.telehuerto.ui.theme.GreenPrimary
import com.example.telehuerto.ui.theme.TextOnDark
import com.example.telehuerto.ui.theme.TextSecondary

data class NavItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

/**
 * Barra inferior flotante: píldora blanca redondeada, la sección activa
 * se marca con un rectángulo verde y "Alertas" muestra cuántas hay abiertas.
 */
@Composable
fun BottomNavBar(navController: NavController, currentRoute: String) {
    // Leer la lista de estado aquí hace que el badge se actualice solo.
    val alertCount = HuertoRepository.activeAlerts().size

    val items = listOf(
        NavItem("Inicio", "dashboard", Icons.Outlined.Home),
        NavItem("Plantas", "plants", Icons.Outlined.Eco),
        NavItem("Alertas", "alerts", Icons.Outlined.Notifications, alertCount),
        NavItem("Bitácora", "logbook", Icons.AutoMirrored.Outlined.MenuBook)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(30.dp),
                    ambientColor = GreenPrimary.copy(alpha = 0.18f),
                    spotColor = GreenPrimary.copy(alpha = 0.18f)
                ),
            shape = RoundedCornerShape(30.dp),
            color = CardBackground
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route
                    BottomNavItem(
                        item = item,
                        selected = selected,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo("dashboard")
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    item: NavItem,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (selected) GreenPrimary else Color.Transparent,
        label = "navItemBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) TextOnDark else TextSecondary,
        label = "navItemContent"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            if (item.badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 9.dp, y = (-6).dp)
                        .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                        .clip(CircleShape)
                        .background(AlertCritical)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (item.badgeCount > 9) "9+" else item.badgeCount.toString(),
                        color = TextOnDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp
                    )
                }
            }
        }
        Text(
            text = item.label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
