package ru.aeyu.innosystemstestapp.interactions

import android.view.SurfaceHolder

sealed class MainEvents: ViewEvents {
    data class OnSurfaceCreated(val surfaceHolder: SurfaceHolder): MainEvents()
    data class OnSurfaceChanged(val surfaceHolder: SurfaceHolder): MainEvents()
    object OnSurfaceDestroyed : MainEvents()
    object OnViewStart : MainEvents()
    object OnViewStop : MainEvents()
}