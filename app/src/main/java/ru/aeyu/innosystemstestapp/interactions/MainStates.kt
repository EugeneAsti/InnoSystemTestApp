package ru.aeyu.innosystemstestapp.interactions

sealed class MainStates : ViewStates {
    object Idle : MainStates()
    object Loading : MainStates()
    data class Error(val errMessage: String) : MainStates()
}
