package com.sim.darna.repository

import com.sim.darna.auth.ReportApi
import com.sim.darna.model.Report
import com.sim.darna.model.ReportRequest

class ReportRepository(
    private val api: ReportApi
) {

    suspend fun sendReport(reason: String, details: String): Report? {
        return try {
            val body = ReportRequest(reason = reason, details = details)
            val res = api.createReport(body)
            if (res.isSuccessful) res.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
