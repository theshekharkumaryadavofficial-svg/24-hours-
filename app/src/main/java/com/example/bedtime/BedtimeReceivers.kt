package com.example.bedtime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.SleepDatabase
import com.example.data.SleepRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BedtimeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = SleepRepository(SleepDatabase.getInstance(context).sleepDao())
                val goal = repo.getGoalOnce()
                if (!goal.autoBedtimeEnabled) return@launch

                when (action) {
                    BedtimeScheduler.ACTION_BEDTIME_START -> {
                        BedtimeGuardService.startGuard(context, isAutoTriggered = true)
                    }
                    BedtimeScheduler.ACTION_BEDTIME_END -> {
                        BedtimeGuardService.stopGuard(context)
                    }
                }
                // Reschedule for the following day automatically
                BedtimeScheduler.syncSchedule(context, goal)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class BootRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SleepDatabase.getInstance(context)
                val sleepRepo = SleepRepository(db.sleepDao())
                val plansRepo = com.example.data.PlansRepository(db.plansDao())
                BedtimeScheduler.syncSchedule(context, sleepRepo.getGoalOnce())
                com.example.plans.StudyScheduler.syncStudySchedule(context, plansRepo.getStudyConfigOnce())
                com.example.plans.StudyScheduler.syncAllSlotSchedules(context, plansRepo.getAllBlocksOnce())
                com.example.plans.StudyScheduler.syncDisciplineReminder(context, plansRepo.getDisciplineConfigOnce())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
