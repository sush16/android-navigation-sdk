package com.ola.maps.sample

import android.content.Context
import android.hardware.SensorManager
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleObserver
import com.ola.maps.sample.databinding.CompassViewBinding


class CompassView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr), LifecycleObserver {

    private lateinit var binding: CompassViewBinding

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1) {
        initViews()
    }

    private fun initViews() {
        binding = CompassViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun update(angle: Float) {
        if (isEnabled && isVisible) {
            binding.fabCompass.rotation = angle
        }
    }
}