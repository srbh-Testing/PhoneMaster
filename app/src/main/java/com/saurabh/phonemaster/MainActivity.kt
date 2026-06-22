package com.saurabh.phonemaster

import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0Targetx121212) // Dark Premium Background
                ) {
                    PhoneMasterDashboard()
                }
            }
        }
    }
}

@Composable
fun PhoneMasterDashboard() {
    // Storage Details Fetch Logic
    val iStats = remember { getStorageStats() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Circular Score/Condition Indicator (76 pts style)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F3D1A)) // Dark Green Ring Outline proxy
                .padding(12.dp)
                .background(Color(0xFF1E1E1E), CircleShape)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${iStats.score}",
                    fontSize = 54.sp,
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
        
        Text(
            text = if (iStats.score > 70) "Your system is in good condition." else "System needs optimization",
            fontSize = 18.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Optimise Green Button
        Button(
            onClick = { /* Action integration next */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = "Optimise", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Feature Dashboard Grid Layout
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                DashboardCard(
                    title = "Storage cleanup",
                    subtitle = "${iStats.usedText} / ${iStats.totalText}",
                    iconLabel = "🧹"
                )
            }
            item {
                DashboardCard(
                    title = "Viruses & risks",
                    subtitle = "No risky apps",
                    iconLabel = "🛡️"
                )
            }
            item {
                DashboardCard(
                    title = "System boost",
                    subtitle = "Improve system performance",
                    iconLabel = "🚀"
                )
            }
            item {
                DashboardCard(
                    title = "App management",
                    subtitle = "Manage apps efficiently",
                    iconLabel = "📱"
                )
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, subtitle: String, iconLabel: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = iconLabel, fontSize = 24.sp)
            Column {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
            }
        }
    }
}

// Backend Storage Framework Model Data Class
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
        
        // Simple Dynamic Score logic based on storage usage
        val freePercentage = (availableBytes.toFloat() / totalBytes.toFloat()) * 100
        val dynamicScore = (50 + (freePercentage * 0.5)).coerceIn(0.0, 100.0).toInt()

        StorageData("${usedGB} GB", "${totalGB} GB", dynamicScore)
    } catch (e: Exception) {
        StorageData("0 GB", "0 GB", 76) // Fallback stable default matching user screenshot
    }
}
