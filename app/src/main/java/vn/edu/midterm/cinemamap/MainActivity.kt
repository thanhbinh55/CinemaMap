package vn.edu.midterm.cinemamap

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import vn.edu.midterm.cinemamap.map.CinemaMapView
import vn.edu.midterm.cinemamap.ui.BookingViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: BookingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val cinemaMap = findViewById<CinemaMapView>(R.id.cinema_map)
        findViewById<View>(R.id.reset_view).setOnClickListener {
            cinemaMap.resetView()
        }

        // 1. Chuyển sự kiện click từ Map sang ViewModel
        cinemaMap.onSeatClickListener = { seat ->
            viewModel.onSeatTapped(seat)
        }

        // 2. Thu thập state từ ViewModel và cập nhật lại Map
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    cinemaMap.selectedSeatIds = state.selectedSeats.map { it.id }.toSet()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}