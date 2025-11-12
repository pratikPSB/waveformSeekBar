package com.masoudss.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.masoudss.R
import com.masoudss.databinding.ActivityMainBinding
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import com.masoudss.lib.utils.Utils
import com.masoudss.lib.utils.WaveGravity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            var denied = false
            for (i in it.keys.indices)
                if (!it.values.elementAt(i)) {
                    denied = true
                    break
                }
            if (denied)
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.permission_error),
                    Toast.LENGTH_SHORT
                ).show()
            else
                launchSelectAudioActivity()
        }

    private val requestIntentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.let { data ->
                    val path = data.getStringExtra("path")


                    val progressDialog = AlertDialog.Builder(this@MainActivity)
                    progressDialog.setMessage(getString(R.string.message_waiting))
                    progressDialog.setCancelable(false)

                    val dialog = progressDialog.show()

                    lifecycleScope.launch(Dispatchers.IO) {
                        binding.waveformSeekBar.setSampleFrom(path!!)
                        with(Dispatchers.Main) {
                            dialog.dismiss()
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.waveformSeekBar.apply {
            progress = 33.2F
            waveWidth = Utils.dp(this@MainActivity, 5)
            waveGap = Utils.dp(this@MainActivity, 2)
            waveMinHeight = Utils.dp(this@MainActivity, 5)
            waveCornerRadius = Utils.dp(this@MainActivity, 2)
            waveGravity = WaveGravity.CENTER
            waveBackgroundColor = ContextCompat.getColor(this@MainActivity, R.color.white)
            waveProgressColor = ContextCompat.getColor(this@MainActivity, R.color.blue)
            sample = getDummyWaveSample()
            marker = getDummyMarkerSample()
            onProgressChanged = object : SeekBarOnProgressChanged {
                override fun onProgressChanged(
                    waveformSeekBar: WaveformSeekBar,
                    progress: Float,
                    fromUser: Boolean
                ) {
                    if (fromUser)
                        binding.waveProgress.progress = progress.toInt()
                }
            }
        }

        binding.waveWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.waveformSeekBar.waveWidth =
                    progress / 100F * Utils.dp(this@MainActivity, 20)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.waveCornerRadius.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.waveformSeekBar.waveCornerRadius =
                    progress / 100F * Utils.dp(this@MainActivity, 10)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.waveGap.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.waveformSeekBar.waveGap = progress / 100F * Utils.dp(this@MainActivity, 10)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.waveProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.waveformSeekBar.progress = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.waveMaxProgress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.waveProgress.max = progress
                binding.waveformSeekBar.maxProgress = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.visibleProgress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.waveformSeekBar.visibleProgress = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.gravityRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = binding.gravityRadioGroup.findViewById(checkedId)
            val index = binding.gravityRadioGroup.indexOfChild(radioButton)
            binding.waveformSeekBar.waveGravity = when (index) {
                0 -> WaveGravity.TOP
                1 -> WaveGravity.CENTER
                else -> WaveGravity.BOTTOM
            }
        }

        binding.waveColorRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = binding.waveColorRadioGroup.findViewById(checkedId)
            val index = binding.waveColorRadioGroup.indexOfChild(radioButton)
            binding.waveformSeekBar.waveBackgroundColor = when (index) {
                0 -> ContextCompat.getColor(this, R.color.pink)
                1 -> ContextCompat.getColor(this, R.color.yellow)
                else -> ContextCompat.getColor(this, R.color.white)
            }
        }

        binding.progressColorRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = binding.progressColorRadioGroup.findViewById(checkedId)
            val index = binding.progressColorRadioGroup.indexOfChild(radioButton)
            binding.waveformSeekBar.waveProgressColor = when (index) {
                0 -> ContextCompat.getColor(this, R.color.red)
                1 -> ContextCompat.getColor(this, R.color.blue)
                else -> ContextCompat.getColor(this, R.color.green)
            }
        }

        binding.icGithub.setOnClickListener {
            val url = "https://github.com/pratikPSB/waveformSeekBar"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        binding.icImport.setOnClickListener {
            checkStoragePermission()
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val storageReadPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_AUDIO
            else Manifest.permission.READ_EXTERNAL_STORAGE

            val hasReadPermission = checkSelfPermission(storageReadPermission)
            val hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            val permissions: ArrayList<String> = arrayListOf()

            if (hasReadPermission != PackageManager.PERMISSION_GRANTED)
                permissions.add(storageReadPermission)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && hasWritePermission != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if (permissions.isNotEmpty()) {
                requestStoragePermission.launch(permissions.toTypedArray())
            } else
                launchSelectAudioActivity()

        } else
            launchSelectAudioActivity()
    }

    private fun launchSelectAudioActivity() {
        val intent = Intent(this@MainActivity, SelectAudioActivity::class.java)
        requestIntentLauncher.launch(intent)
    }

    private fun getDummyWaveSample(): IntArray {
        val data = IntArray(50)
        for (i in data.indices)
            data[i] = Random().nextInt(data.size)

        return data
    }

    private fun getDummyMarkerSample(): HashMap<Float, String> {
        val map = hashMapOf<Float, String>()
        map[binding.waveformSeekBar.maxProgress / 2] = "Middle"
        map[binding.waveformSeekBar.maxProgress / 4] = "Quarter"
        return map
    }
}
