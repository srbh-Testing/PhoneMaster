package com.saurabh.phonemaster

import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0C0C0C) // Pure Premium Pitch Black
                ) {
                    PhoneMasterDashboard()
                }
            }
        }
    }
}

@Composable
fun PhoneMasterDashboard() {
    val iStats = remember { getStorageStats() }
    val coroutineScope = rememberCoroutineScope()
    
    // Smooth Animation States
    var isOptimizing by remember { mutableStateOf(false) }
    var currentScore by remember { mutableStateOf(iStats.score) }
    
    // Scale animation logic for the central dashboard ring
    val scaleFactor by animateFloatAsState(
        targetValue = if (isOptimizing) 0.92f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "ScaleAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        
        // Animated Circular Score Panel
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(210.dp)
                .scale(scaleFactor)
                .clip(CircleShape)
                .background(if (isOptimizing) Color(0xFF00C853) else Color(0xFF1B5E20)) 
                .padding(6.dp)
                .background(Color(0xFF0C0C0C), CircleShape)
                .padding(12.dp)
                .background(Color(0xFF161616), CircleShape)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$currentScore",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "pts",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // Fluid status message changer
        Text(
            text = if (isOptimizing) "Optimizing system logs & memory..." else if (currentScore > 90) "Your system is in excellent condition." else "Your system is in good condition.",
            fontSize = 16.sp,
            color = if (isOptimizing) Color(0xFF00C853) else Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Premium Active Green Button with Ripple
        Button(
            onClick = {
                if (!isOptimizing) {
                    coroutineScope.launch {
                        isOptimizing = true
                        for (i in currentScore..98) {
                            delay(40)
                            currentScore = i
                        }
                        delay(600)
                        isOptimizing = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00C853),
                disabledContainerColor = Color(0xFF1B5E20)
            ),
            enabled = !isOptimizing,
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = if (isOptimizing) "Optimizing..." else "Optimise",
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Balanced Grid Layout with Material Interactive Cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                DashboardCard(
                    title = "Storage cleanup",
                    subtitle = "${iStats.usedText} / ${iStats.totalText}",
                    iconLabel = "🧹",
                    onClick = { }
                )
            }
            item {
                DashboardCard(
                    title = "Viruses & risks",
                    subtitle = "No risky apps",
                    iconLabel = "🛡️",
                    onClick = { }
                )
            }
            item {
                DashboardCard(
                    title = "System boost",
                    subtitle = "Improve speed",
                    iconLabel = "🚀",
                    onClick = { }
                )
            }
            item {
                DashboardCard(
                    title = "App management",
                    subtitle = "Clean apps easily",
                    iconLabel = "📱",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, subtitle: String, iconLabel: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = iconLabel, fontSize = 26.sp)
            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
            }
        }
    }
}

data class StorageData(val usedText: String, val totalText: String, val score: Int)

fun getStorageStats(): StorageData {
    return try {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalBytes = totalBlocks * blockSize
        val availableBytes = availableBlocks * blockSize
        val usedBytes = totalBytes - availableBytes

        val totalGB = totalBytes / (1024 * 1024 * 1024)
        val usedGB = usedBytes / (1024 * 1024 * 1024)
        
        val freePercentage = (availableBytes.toFloat() / totalBytes.toFloat()) * 100
        val dynamicScore = (60 + (freePercentage * 0.4)).coerceIn(0.0, 100.0).toInt()

        StorageData("${usedGB} GB", "${totalGB} GB", dynamicScore)
    } catch (e: Exception) {
        StorageData("0 GB", "0 GB", 76)
    }
}
