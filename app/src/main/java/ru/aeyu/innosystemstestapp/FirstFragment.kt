package ru.aeyu.innosystemstestapp

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Size
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ru.aeyu.innosystemstestapp.databinding.FragmentFirstBinding
import ru.aeyu.innosystemstestapp.interactions.MainEvents
import ru.aeyu.innosystemstestapp.interactions.MainStates
import ru.aeyu.innosystemstestapp.viewmodels.MainViewModel


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var surfaceView: SurfaceView
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        surfaceView = binding.surView
        surfaceView.holder.addCallback(surfaceCallback)
        surfaceView.keepScreenOn = true
        collectStates()
        collectLayoutParams()
    }

    private fun collectLayoutParams() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.layoutParams.collect { params ->
                    surfaceView.layoutParams = params
                    surfaceView.holder.setKeepScreenOn(true)
                    mainViewModel.sendEvent(MainEvents.OnSurfaceChanged(surfaceView.holder))
                }
            }
        }
    }

    private fun collectStates() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.viewModelStates.collect { state ->
                    when (state) {
                        is MainStates.Error -> setErrMessage(state.errMessage)
                        MainStates.Idle -> setIdle()
                        MainStates.Loading -> setLoading()
                    }
                }
            }
        }
    }

    private fun setLoading() {
        binding.pBarMain.isVisible = true
        binding.textviewFirst.isVisible = false
    }

    private fun setIdle() {
        binding.pBarMain.isVisible = false
        binding.textviewFirst.isVisible = false
    }

    private fun setErrMessage(errMessage: String) {
        binding.pBarMain.isVisible = false
        binding.textviewFirst.isVisible = true
        binding.textviewFirst.text = errMessage
    }

    override fun onDestroyView() {
        super.onDestroyView()
        surfaceView.keepScreenOn = false
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        println("###---###---###. onStart()")
        surfaceView.invalidate()
        mainViewModel.sendEvent(MainEvents.OnViewStart)
    }

    override fun onPause() {
        super.onPause()
        println("###---###---###. onPause()")
        mainViewModel.sendEvent(MainEvents.OnViewStop)
    }

    override fun onStop() {
        println("###---###---###. onStop()")

        super.onStop()
    }

    private val surfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(p0: SurfaceHolder) {
            println("###---###---###. surfaceCreated()")
            p0.setKeepScreenOn(true)
            mainViewModel.sendEvent(MainEvents.OnSurfaceCreated(p0))
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            println("###---###---###. surfaceDestroyed()")
            p0.setKeepScreenOn(false)
            mainViewModel.sendEvent(MainEvents.OnSurfaceDestroyed)
            //surfaceView.clearAnimation()
        }
    }
}