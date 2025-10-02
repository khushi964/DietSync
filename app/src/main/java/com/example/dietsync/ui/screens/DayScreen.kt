package com.example.dietsync.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietsync.R
import com.example.dietsync.notifications.MealNotificationReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

data class MealEntry(
    var day: String = DayOfWeek.MONDAY.name,
    var time: LocalTime = LocalTime.now(),
    var meal: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "day" to day,
        "time" to time.format(DateTimeFormatter.ofPattern("HH:mm")),
        "meal" to meal
    )

    companion object {
        fun fromMap(map: Map<String, Any>): MealEntry {
            val day = map["day"] as? String ?: DayOfWeek.MONDAY.name
            val timeString = map["time"] as? String ?: "00:00"
            val meal = map["meal"] as? String ?: ""
            return MealEntry(
                day = day,
                time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm")),
                meal = meal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(day: String) {
    var meals by remember { mutableStateOf(listOf<MealEntry>()) }
    var loading by remember { mutableStateOf(true) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Request notification permission on Android13+ (safely from Compose)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Load meals
    LaunchedEffect(day, userId) {
        if (userId != null) {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("meals")
                .document(day)
                .get()
                .await()

            val mealList = (snapshot["meals"] as? List<Map<String, Any>>)?.map {
                MealEntry.fromMap(it)
            } ?: emptyList()

            meals = mealList
        }
        loading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "$day's Diet Plan", fontSize = 30.sp)
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            if (loading) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier.padding(padding).fillMaxSize().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Here you can add/view meals for $day", fontSize = 20.sp)

                    Spacer(modifier = Modifier.height(20.dp))

                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)) {
                        itemsIndexed(meals) { index, meal ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                // Hour selector
                                var expandedHour by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedButton(onClick = { expandedHour = true }) { Text(meal.time.hour.toString().padStart(2, '0')) }
                                    DropdownMenu(expanded = expandedHour, onDismissRequest = { expandedHour = false }) {
                                        (0..23).forEach { h ->
                                            DropdownMenuItem(text = { Text(h.toString().padStart(2, '0')) }, onClick = {
                                                meals = meals.toMutableList().also { it[index] = it[index].copy(time = meal.time.withHour(h)) }
                                                expandedHour = false
                                            })
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Minute selector
                                var expandedMinute by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedButton(onClick = { expandedMinute = true }) { Text(meal.time.minute.toString().padStart(2, '0')) }
                                    DropdownMenu(expanded = expandedMinute, onDismissRequest = { expandedMinute = false }) {
                                        (0..59).forEach { m ->
                                            DropdownMenuItem(text = { Text(m.toString().padStart(2, '0')) }, onClick = {
                                                meals = meals.toMutableList().also { it[index] = it[index].copy(time = meal.time.withMinute(m)) }
                                                expandedMinute = false
                                            })
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Meal name
                                OutlinedTextField(value = meal.meal, onValueChange = { newValue ->
                                    meals = meals.toMutableList().also { it[index] = it[index].copy(meal = newValue) }
                                }, label = { Text("Meal") }, textStyle = TextStyle(fontSize = 18.sp), modifier = Modifier.weight(2f))

                                IconButton(onClick = { meals = meals.toMutableList().also { it.removeAt(index) } }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Meal")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { meals = meals + MealEntry(day = day.uppercase()) }) { Text("➕ Add Meal") }
                        Button(onClick = {
                            if (userId != null) {
                                firestore.collection("users").document(userId).collection("meals").document(day)
                                    .set(mapOf("meals" to meals.map { it.toMap() }))

                                // Schedule each saved meal for its next occurrence
                                meals.forEach { meal ->
                                    scheduleMealReminder(context, meal)
                                }
                            }
                        }) { Text("💾 Save & Notify") }
                    }
                }
            }
        }
    }
}

/** Schedule the *next occurrence* for the given MealEntry.
 *  Alarm is exact (setExactAndAllowWhileIdle). The receiver will reschedule for next week after firing. */
private fun scheduleMealReminder(context: Context, meal: MealEntry) {
    try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, MealNotificationReceiver::class.java).apply {
            putExtra("meal", meal.meal)
            putExtra("day", meal.day)
            putExtra("hour", meal.time.hour)
            putExtra("minute", meal.time.minute)
        }

        val requestCode = ("${meal.day}|${meal.time.hour}:${meal.time.minute}|${meal.meal}").hashCode()
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance()
        val calendarDay = when (meal.day.uppercase()) {
            "MONDAY" -> Calendar.MONDAY
            "TUESDAY" -> Calendar.TUESDAY
            "WEDNESDAY" -> Calendar.WEDNESDAY
            "THURSDAY" -> Calendar.THURSDAY
            "FRIDAY" -> Calendar.FRIDAY
            "SATURDAY" -> Calendar.SATURDAY
            "SUNDAY" -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
        cal.set(Calendar.DAY_OF_WEEK, calendarDay)
        cal.set(Calendar.HOUR_OF_DAY, meal.time.hour)
        cal.set(Calendar.MINUTE, meal.time.minute)
        cal.set(Calendar.SECOND, 0)

        // if time already passed this week, schedule for next week
        if (cal.timeInMillis <= System.currentTimeMillis()) cal.add(Calendar.WEEK_OF_YEAR, 1)

        // cancel any existing with same intent/requestCode then set exact
        alarmManager.cancel(pending)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending)
        }
    } catch (t: Throwable) {
        t.printStackTrace()
    }
}