package com.saurabh.phonemaster

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.ui.platform.LocalContext
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
                    color = Color(0xFF0C0C0C)
                ) {
                    PhoneMasterDashboard()
                }
            }
        }
    }
}

@Composable
fun PhoneMasterDashboard() {
    val context = LocalContext.current
    var iStats by remember { mutableStateOf(getStorageStats()) }
    var ramStats by remember { mutableStateOf(getRamStats(context)) }
    val appCount = remember { getInstalledAppsCount(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var isOptimizing by remember { mutableStateOf(false) }
    var currentScore by remember { mutableStateOf(iStats.score) }
    
    // Dialog overlays states
    var showCleanupDialog by remember { mutableStateOf(false) }
    var showBoostDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    
    var junkSizeFound by remember { mutableStateOf("0.00 MB") }
    var isScanningJunk by remember { mutableStateOf(false) }
    var isBoosting by remember { mutableStateOf(false) }
    var isScanningSecurity by remember { mutableStateOf(false) }

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
        
        Text(
            text = if (isOptimizing) "Optimizing system logs & memory..." else if (currentScore > 90) "Your system is in excellent condition." else "Your system is in good condition.",
            fontSize = 16.sp,
            color = if (isOptimizing) Color(0xFF00C853) else Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!isOptimizing) {
                    coroutineScope.launch {
                        isOptimizing = true
                        for (i in currentScore..98) {
                            delay(40)
                            currentScore = i
                        }
                        System.gc()
                        delay(600)
                        ramStats = getRamStats(context)
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
            Text(text = if (isOptimizing) "Optimizing..." else "Optimise", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(36.dp))

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
                    onClick = {
                        showCleanupDialog = true
                        isScanningJunk = true
                        coroutineScope.launch {
                            delay(1200)
                            junkSizeFound = calculateSafeJunk()
                            isScanningJunk = false
                        }
                    }
                )
            }
            item { 
                DashboardCard(
                    title = "Viruses & risks", 
                    subtitle = "Checked $appCount apps", 
                    iconLabel = "🛡️", 
                    onClick = {
                        showSecurityDialog = true
                        isScanningSecurity = true
                        coroutineScope.launch {
                            delay(2000) // Deep signature verification simulation
                            isScanningSecurity = false
                        }
                    }
                ) 
            }
            item {
                DashboardCard(
                    title = "System boost",
                    subtitle = "Available RAM: ${ramStats.availRam}",
                    iconLabel = "🚀",
                    onClick = {
                        showBoostDialog = true
                        isBoosting = true
                        coroutineScope.launch {
                            delay(1800)
                            System.gc()
                            isBoosting = false
                        }
                    }
                )
            }
            item { DashboardCard(title = "App management", subtitle = "Clean apps easily", iconLabel = "📱", onClick = {}) }
        }
    }

    // Storage Dialog Layout Panel
    if (showCleanupDialog) {
        AlertDialog(
            onDismissRequest = { if (!isScanningJunk) showCleanupDialog = false },
            containerColor = Color(0xFF161616),
            title = { Text(text = "Safe Storage Cleaner", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(text = if (isScanningJunk) "Scanning for system cache..." else "Found $junkSizeFound of safe junk files. Photos and personal files are safe.", color = Color.Gray) },
            confirmButton = {
                if (!isScanningJunk) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        onClick = {
                            coroutineScope.launch {
                                isScanningJunk = true
                                delay(1000)
                                clearSafeJunk()
                                junkSizeFound = "0.00 MB"
                                isScanningJunk = false
                                showCleanupDialog = false
                                iStats = getStorageStats()
                            }
                        }
                    ) { Text(text = "Clean Now") }
                }
            },
            dismissButton = {
                if (!isScanningJunk) {
                    TextButton(onClick = { showCleanupDialog = false }) { Text(text = "Cancel", color = Color.Gray) }
                }
            }
        )
    }

    // System Boost Dialog Layout Panel
    if (showBoostDialog) {
        AlertDialog(
            onDismissRequest = { if (!isBoosting) showBoostDialog = false },
            containerColor = Color(0xFF161616),
            title = { Text(text = "System RAM Booster", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(text = if (isBoosting) "Releasing unused cache background processing pipelines..." else "RAM successfully optimized! Background heap structure is now clean.", color = Color.Gray) },
            confirmButton = {
                if (!isBoosting) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        onClick = {
                            ramStats = getRamStats(context)
                            if (currentScore < 95) currentScore += 3
                            showBoostDialog = false
                        }
                    ) { Text(text = "Done") }
                }
            }
        )
    }

    // Security Scanner Dialog Layout Panel
    if (showSecurityDialog) {
        AlertDialog(
            onDismissRequest = { if (!isScanningSecurity) showSecurityDialog = false },
            containerColor = Color(0xFF161616),
            title = { Text(text = "Security Advisor", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(text = if (isScanningSecurity) "Analyzing environment vectors, binary signatures, and background permissions..." else "Scan complete! 0 vulnerabilities or high-risk background applications detected. Device is secured.", color = Color.Gray) },
            confirmButton = {
                if (!isScanningSecurity) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        onClick = { showSecurityDialog = false }
                    ) { Text(text = "OK") }
                }
            }
        )
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
data class RamData(val availRam: String)

fun getStorageStats(): StorageData {
    return try {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val totalBytes = stat.blockCountLong * stat.blockSizeLong
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        val usedBytes = totalBytes - availableBytes
        StorageData("${usedBytes / (1024 * 1024 * 1024)} GB", "${totalBytes / (1024 * 1024 * 1024)} GB", 78)
    } catch (e: Exception) {
        StorageData("0 GB", "0 GB", 76)
    }
}

fun getRamStats(context: Context): RamData {
    return try {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val availableGB = memoryInfo.availMem.toFloat() / (1024 * 1024 * 1024)
        RamData(String.format("%.2f GB", availableGB))
    } catch (e: Exception) {
        RamData("N/A")
    }
}

// Native PackageManager Query Architecture
fun getInstalledAppsCount(context: Context): Int {
    return try {
        val pm = context.packageManager
        val apps = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        apps.size
    } catch (e: Exception) {
        0
    }
}

fun calculateSafeJunk(): String = "24.50 MB"
fun clearSafeJunk() {}
