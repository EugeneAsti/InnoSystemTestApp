package ru.aeyu.innosystemstestapp.viewmodels

import android.app.Application
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.hardware.display.DisplayManager
import android.media.MediaPlayer
import android.os.Build
import android.util.DisplayMetrics
import android.util.Size
import android.view.SurfaceHolder
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.aeyu.innosystemstestapp.BuildConfig
import ru.aeyu.innosystemstestapp.interactions.MainEvents
import ru.aeyu.innosystemstestapp.interactions.MainStates
import ru.aeyu.innosystemstestapp.json.ParseJson
import ru.aeyu.innosystemstestapp.json.ReadJsonFromFileUseCase
import ru.aeyu.innosystemstestapp.model.VideoFileItem
import ru.aeyu.innosystemstestapp.model.VideoFiles
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.IllegalArgumentException

class MainViewModel(mApp: Application) : BaseViewModel<MainEvents, MainStates>(mApp) {
    override val classTAG: String = "MainViewModel"

    private val playList = VideoFiles()

    private val _assetFd = MutableSharedFlow<AssetFileDescriptor>()
    private val assetFd: SharedFlow<AssetFileDescriptor> = _assetFd.asSharedFlow()

    private val _layoutParamsFlow = MutableSharedFlow<ConstraintLayout.LayoutParams>()
    val layoutParams: SharedFlow<ConstraintLayout.LayoutParams> = _layoutParamsFlow.asSharedFlow()

    private lateinit var currentAssetFileDescriptor: AssetFileDescriptor

    private var mediaPlayer: MediaPlayer? = null
    private var isMPPrepared = false

    companion object {
        const val PATH_VIDEOS = "Videos"
        const val FILE_NAME_PLAYLIST_JSON = "medialist.json"
    }

    private var currentFilePosition = 0

    private var currentSurfaceHolder: SurfaceHolder? = null

    init {
        fetchPlayList()
        collectFileDescriptors()
    }


    /**
     * Получаем список файлов для проигрывания
     */
    private fun fetchPlayList() {
        viewModelScope.launch(Dispatchers.IO) {
            //получаем InputStream для файла c плейлистом из папки assets и создаем поток
            flowOf(myApp.resources.assets.open(FILE_NAME_PLAYLIST_JSON))
                .map { inputStream ->
                    //трансформируем поток в текст - содержимое файла
                    ReadJsonFromFileUseCase().read(inputStream)

                }.collect { textFromFile ->
                    // парсим содержимое файла
                    val list = ParseJson().fetchDataFromJson(textFromFile)
                    printLog("JSON List Size: ${list.size}")
                    // и выпускаем значение в поток
                    list.sortBy { item ->
                        item.OrderNumber
                    }
                    playList.clear()
                    playList.addAll(list)
                }
        }

    }

    private suspend fun emitFileDescriptor(item: VideoFileItem) {
        currentAssetFileDescriptor = try {
            printLog("emitFileDescriptor for fileName: ${item.VideoIdentifier}")
            myApp.resources.assets.openFd(
                PATH_VIDEOS +
                        File.separator.toString() + item.VideoIdentifier
            )
        } catch (fEx: FileNotFoundException) {
            if (BuildConfig.DEBUG) {
                fEx.printStackTrace()
                setErrState("emitFileDescriptor.err: ${fEx.localizedMessage}")
            }
            return
        }
        _assetFd.emit(currentAssetFileDescriptor)
    }

    private fun prepareData() {
        printLog("prepareData")
        mediaPlayer = MediaPlayer()
        updateMediaPlayerSurface()
        play()
    }

    private fun updateMediaPlayerSurface() {
        try {
            //mediaPlayer?.setDisplay(currentSurfaceHolder)
            mediaPlayer?.setSurface(currentSurfaceHolder?.surface)
        } catch (e: IllegalStateException) {
            setErrState("setDisplay.IllegalStateException: ${e.localizedMessage}")
            return
        }
    }

    private fun playNext() {
        viewModelScope.launch(Dispatchers.IO) {
            printLog("playNext()")
            setViewModelState(MainStates.Loading)
            emitFileDescriptor(playList.nextFile())
        }
    }

    private fun play() {
        if (currentSurfaceHolder == null)
            return
        printLog("play()")
        //Продолжаем видео, на котором прервались
        if (currentFilePosition != 0) {
            playCurrent()
        } else {
            playNext()
        }
    }

    private fun playCurrent() {
        printLog("resumeCurrentVideo()")
        viewModelScope.launch(Dispatchers.IO) {
            printLog("playCurrent()")
            setViewModelState(MainStates.Loading)
            emitFileDescriptor(playList.currentFile())
        }
    }

    private fun collectFileDescriptors() {
        viewModelScope.launch(Dispatchers.Main) {
            assetFd.collect { afd ->
                initMediaPlayer(afd)
            }
        }
    }

    private fun initMediaPlayer(fileDescriptor: AssetFileDescriptor) {

        try {
            mediaPlayer?.setDataSource(
                fileDescriptor.fileDescriptor,
                fileDescriptor.startOffset,
                fileDescriptor.length
            )
        } catch (e: IllegalStateException) {
            setErrState("setDataSource.IllegalStateException: ${e.localizedMessage}")
            return
        } catch (e: IllegalArgumentException) {
            setErrState("setDataSource.IllegalArgumentException: ${e.localizedMessage}")
            return
        } catch (e: IOException) {
            setErrState("setDataSource.IOException: ${e.localizedMessage}")
            return
        }
        try {
            mediaPlayer?.prepareAsync()
        } catch (e: IllegalStateException) {
            setErrState("prepareAsync.IllegalStateException: ${e.localizedMessage}")
            return
        }catch (e: IOException) {
            setErrState("prepareAsync.IOException: ${e.localizedMessage}")
            return
        }
        mediaPlayer?.setOnPreparedListener(mediaPlayerPreparedListener)
        mediaPlayer?.setOnCompletionListener(mediaPlayerCompleteListener)
        mediaPlayer?.setOnErrorListener(mediaPlayerErrorListener)
        mediaPlayer?.setOnVideoSizeChangedListener(mediaPlayerSizeChangeListener)
    }


    private fun surfaceDestroyed() {
        currentSurfaceHolder = null
        currentAssetFileDescriptor.close()
    }

    override fun onCleared() {
        super.onCleared()
        releaseMediaPlayer()
    }

    private fun releaseMediaPlayer() {
        printLog("releasePlayer()")
        mediaPlayer?.stop()

        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun resetMediaPlayer() {
        if(isMPPrepared) {
            printLog("resetMediaPlayer()")
            mediaPlayer?.reset()
            isMPPrepared = false
        }
    }

    private val mediaPlayerPreparedListener = MediaPlayer.OnPreparedListener {
        printLog("mediaPlayerPreparedListener")
        isMPPrepared = true
        setViewModelState(MainStates.Idle)
        try {
            if(currentFilePosition != 0)
                it.seekTo(currentFilePosition)
            it.start()
        } catch (e: IllegalStateException) {
            setErrState("OnPreparedListener.IllegalStateException: ${e.localizedMessage}")
        }
    }

    private val mediaPlayerCompleteListener = MediaPlayer.OnCompletionListener {
        printLog("mediaPlayerCompleteListener: it.isPlaying: ${it.isPlaying}")
        if(it.isPlaying)
            it.stop()
        if(mediaPlayer?.isPlaying == true)
            mediaPlayer?.stop()
        currentAssetFileDescriptor.close()
        currentFilePosition = 0
        resetMediaPlayer()
        playNext()
    }

    private val mediaPlayerErrorListener = MediaPlayer.OnErrorListener { _, _, _ ->
        printLog("MediaPlayer.OnErrorListener")
        false
    }

    private val mediaPlayerSizeChangeListener = MediaPlayer.OnVideoSizeChangedListener { player, width, height ->
        player.videoWidth
        if(width > 0 && height > 0) {
            // соотношение ширины и высоты видео
            val aspectRatio = height.toFloat() / width.toFloat()

            // текущие размеры экрана
            val screenDimensions = getDisplaySize()
            printLog("Size: $screenDimensions")

            // в зависимости от ориентации экрана вычисляем размеры поверхности для отображения
            // видео
            val params = if(screenDimensions.height >= screenDimensions.width) {
                // портретная ориентация
                val surfWidth = screenDimensions.width
                val surfHeight = (surfWidth * aspectRatio).toInt()
                ConstraintLayout.LayoutParams(surfWidth, surfHeight)
            } else {
                //альбомная ориентация
                val surfHeight = screenDimensions.height
                val surfWidth = (surfHeight / aspectRatio).toInt()
                ConstraintLayout.LayoutParams(surfWidth, surfHeight)
            }

            viewModelScope.launch {
                _layoutParamsFlow.emit(params)
            }
        }
    }

    /**
     * Данная функция вычисляет размеры экрана
     * @return Возвращает ширину и высоту экрана обёрнутую в класс Size(width, height)
     */
    private fun getDisplaySize(): Size {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // с выходом Android 11 - API 30 метрики экрана стали вычисляться по-новому
            // Получаем размер экрана в Android 11 и выше
            val wm: WindowManager = myApp.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            //requireActivity().windowManager.currentWindowMetrics
            val metrics: WindowMetrics = wm.currentWindowMetrics
            val windowInsets = metrics.windowInsets
            val insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars()
                        or WindowInsets.Type.displayCutout()
            )
            val insetsWidth: Int = insets.right + insets.left
            val insetsHeight: Int = insets.top + insets.bottom

            // Legacy size that Display#getSize reports
            val bounds = metrics.bounds

            Size(
                bounds.width() - insetsWidth,
                bounds.height() - insetsHeight
            )
        } else {
            // Получаем размер экрана с версии Android 10 и ниже
            val displayManager =
                myApp.baseContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

//            val presentationDisplays =
//                displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
            if (displayManager.displays.isEmpty()) {
                Size(0, 0)
            }else {
                val display = displayManager.displays[0]
                val metrics = DisplayMetrics()
                display.getMetrics(metrics)
                Size(metrics.widthPixels, metrics.heightPixels)
            }
        }
    }

    /**
     * Функция срабатывает, когда фрагмент перешел в состояние onPause
     * т.к. в этом состоянии уничтожится surfaceView и holder, то очищаем медиа плеер
     */
    private fun pauseView() {
        if(isMPPrepared) {
            printLog("pause()")
            currentFilePosition = mediaPlayer?.currentPosition ?: 0
            releaseMediaPlayer()
        }
    }

    override suspend fun handleEvent(event: MainEvents) {
        when (event) {
            is MainEvents.OnSurfaceCreated -> {
                printLog("OnSurfaceCreated")
                currentSurfaceHolder = event.surfaceHolder
                prepareData()
            }
            MainEvents.OnSurfaceDestroyed -> {
                surfaceDestroyed()
            }
            MainEvents.OnViewStart -> {
                printLog("OnViewStart")
            }
            MainEvents.OnViewStop -> {
                printLog("OnViewStop")
                pauseView()
            }
            is MainEvents.OnSurfaceChanged -> {
                currentSurfaceHolder = event.surfaceHolder
                updateMediaPlayerSurface()
            }
        }
    }

    override fun initialState(): MainStates {
        return MainStates.Idle
    }

    private fun setErrState(errMessage: String) {
        setViewModelState(MainStates.Error(errMessage))
    }
}