package ru.aeyu.innosystemstestapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.aeyu.innosystemstestapp.BuildConfig
import ru.aeyu.innosystemstestapp.interactions.MainStates
import ru.aeyu.innosystemstestapp.interactions.ViewEvents
import ru.aeyu.innosystemstestapp.interactions.ViewStates
import java.util.*

abstract class BaseViewModel<UserEvents : ViewEvents, VModelStates : ViewStates>(val myApp: Application) :
    AndroidViewModel(myApp) {

    private val _uEvents = MutableSharedFlow<UserEvents>()
    private val uEvents: SharedFlow<UserEvents> = _uEvents.asSharedFlow()


    private val initialVModelState: VModelStates by lazy { initialState() }
    private var currentState = initialVModelState
    private val _uStates = MutableStateFlow(initialVModelState)
    val viewModelStates: StateFlow<VModelStates> = _uStates.asStateFlow()

    protected abstract val classTAG: String

    init {
        fetchEvents()
    }

    protected fun printLog(message: String) {
        if (BuildConfig.DEBUG)
            println("$$--$$--$$: ${Calendar.getInstance(Locale.getDefault()).time}: $classTAG: $message")
    }

    fun sendEvent(event: UserEvents) {
        viewModelScope.launch {
        printLog("sendEvent: $event")
            _uEvents.emit(event)
        }
    }

    private fun fetchEvents() {
        viewModelScope.launch {
            uEvents.collect { event ->
                handleEvent(event)
            }
        }
    }

    /**
     * Функция обрабатывает входящие события от View
     */
    protected abstract suspend fun handleEvent(event: UserEvents)

    /**
     * Начальное состояние
     */
    protected abstract fun initialState(): VModelStates

    protected fun setViewModelState(newState: VModelStates) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!_uStates.compareAndSet(currentState, newState)) {
                currentState = newState
                _uStates.value = currentState
            }
        }
    }
}