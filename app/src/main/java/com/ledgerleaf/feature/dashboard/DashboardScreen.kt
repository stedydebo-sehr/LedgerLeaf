package com.ledgerleaf.feature.dashboard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun DashboardScreen(){
 Scaffold(containerColor=MaterialTheme.colorScheme.background,floatingActionButton={FloatingActionButton(onClick={},shape=CircleShape,containerColor=MaterialTheme.colorScheme.primary){Icon(Icons.Default.Add,"Add expense")}}){pv->
  Column(Modifier.fillMaxSize().padding(pv).padding(horizontal=20.dp,vertical=12.dp)){
   DashboardHeader(); Spacer(Modifier.height(22.dp)); Text("This Month",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold); Spacer(Modifier.height(10.dp))
   Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)){Column(Modifier.padding(18.dp)){Text("Ledger ready",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Spacer(Modifier.height(6.dp));Text("LL-001 foundation is running. Expense totals will appear here after the data module is implemented.")}}
   Spacer(Modifier.height(22.dp)); Text("Recent Entries",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold); Spacer(Modifier.height(10.dp)); HorizontalDivider(); Box(Modifier.fillMaxWidth().weight(1f),contentAlignment=Alignment.Center){Text("Your ledger is empty.")}
  }
 }
}
@Composable private fun DashboardHeader(){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary).padding(horizontal=10.dp,vertical=7.dp)){Text("LL",color=MaterialTheme.colorScheme.onPrimary,fontWeight=FontWeight.Black)};Column(Modifier.weight(1f).padding(start=12.dp)){Text("LedgerLeaf",fontSize=24.sp,fontWeight=FontWeight.Bold);Text("My Personal Ledger",style=MaterialTheme.typography.bodySmall)};IconButton(onClick={}){Text("▧",fontSize=22.sp,fontWeight=FontWeight.Bold)};IconButton(onClick={}){Icon(Icons.Default.Settings,"Settings")}}}
