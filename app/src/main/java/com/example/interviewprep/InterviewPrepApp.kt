package com.example.interviewprep

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class
 * 
 * @HiltAndroidApp triggers Hilt code generation
 * Must be added to AndroidManifest.xml
 * 
 * This is required for Hilt to work
 */
@HiltAndroidApp
class InterviewPrepApp : Application()
