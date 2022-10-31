package ru.aeyu.innosystemstestapp.repositories

import android.content.Context

interface BaseRepo{
    val context: Context
    val classTag: String
}