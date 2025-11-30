package com.sim.darna.auth

import com.sim.darna.model.Report
import com.sim.darna.model.ReportRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportApi {

    @POST("reports")
    suspend fun createReport(
        @Body body: ReportRequest
    ): Response<Report>
}
