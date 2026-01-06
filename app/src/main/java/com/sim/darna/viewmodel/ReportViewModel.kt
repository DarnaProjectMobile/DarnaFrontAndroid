package com.sim.darna.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.auth.RetrofitClient
import com.sim.darna.repository.ReportRepository
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {

    private var repo: ReportRepository? = null

    fun init(context: Context) {
        if (repo == null) {
            val api = RetrofitClient.reportApi(context)
            repo = ReportRepository(api)
        }
    }

    fun sendReport(
        reason: String,
        details: String,
        onResult: (Boolean) -> Unit
    ) {
        val r = repo ?: return

        viewModelScope.launch {
            val created = r.sendReport(reason, details)
            onResult(created != null)
        }
    }
}
