package com.sim.darna.utils

/**
 * Centralized API configuration
 * Change BASE_URL here to update it across the entire application
 */
object ApiConfig {
    // Base URL for the main API
    // Change this value to switch between production, staging, or local development
    //const val BASE_URL = "https://darna-app.onrender.com/"
    
    // Alternative URLs for different environments (uncomment and use as needed):
     const val BASE_URL = "http://192.168.1.142:3000/"  // Local development
    // const val BASE_URL = "http://10.0.2.2:3000/"      // Android emulator localhost
    // const val BASE_URL = "http://172.16.11.61:3000/"  // Alternative local IP
}

