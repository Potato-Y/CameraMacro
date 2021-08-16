package io.github.potato_y.cameramacro

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null
    private var cycleTime: Long = 60
    private var type: Int = 0
    private var weekSetting = arrayOf(false, false, false, false, false, false, false)
    private var startHH = 0
    private var startmm = 0
    private var endHH = 0
    private var endmm = 0

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //화면이 꺼지지 않도록 조정
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()

        val buttonBlackScreen = findViewById<Button>(R.id.buttonBlackScreen)
        val linearLayoutBlackScreen = findViewById<LinearLayout>(R.id.linearLayoutBlackScreen)

        buttonBlackScreen.setOnClickListener {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )

            linearLayoutBlackScreen.isVisible = true
        }

        linearLayoutBlackScreen.setOnClickListener {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE

                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

            linearLayoutBlackScreen.isVisible = false
        }

        val buttonOpenSource = findViewById<Button>(R.id.buttonOpenSource)
        buttonOpenSource.setOnClickListener {
            val dialog = OpenSourcePage(this)
            dialog.start()
        }


        val buttonStart = findViewById<Button>(R.id.buttonStart)
        buttonStart.setOnClickListener {
            val dialog = MacroSetDialog(this)
            dialog.start()

            dialog.setOnClickListener(object : MacroSetDialog.ButtonClickListener {
                // 단순 반복 매크로
                override fun onClicked(cycle: Long) {
                    captureStart()
                    cycleTime = cycle
                    SimpleIterationThread().start()
                }

                // 요일별 메크로
                override fun onClicked(
                    cycle: Long,
                    sun: Boolean,
                    mon: Boolean,
                    tue: Boolean,
                    wen: Boolean,
                    thu: Boolean,
                    fri: Boolean,
                    sat: Boolean
                ) {
                    cycleTime = cycle
                    weekSetting[0] = sun
                    weekSetting[1] = mon
                    weekSetting[2] = tue
                    weekSetting[3] = wen
                    weekSetting[4] = thu
                    weekSetting[5] = fri
                    weekSetting[6] = sat

                    captureStart()
                    RepeatTheDayOfTheWeekThread().start()
                }

                override fun onClicked(
                    cycle: Long,
                    sun: Boolean,
                    mon: Boolean,
                    tue: Boolean,
                    wen: Boolean,
                    thu: Boolean,
                    fri: Boolean,
                    sat: Boolean,
                    startHH: Int,
                    startmm: Int,
                    endHH: Int,
                    endmm: Int
                ) {
                    cycleTime = cycle
                    weekSetting[0] = sun
                    weekSetting[1] = mon
                    weekSetting[2] = tue
                    weekSetting[3] = wen
                    weekSetting[4] = thu
                    weekSetting[5] = fri
                    weekSetting[6] = sat
                    this@MainActivity.startHH = startHH
                    this@MainActivity.startmm = startmm
                    this@MainActivity.endHH = endHH
                    this@MainActivity.endmm = endmm

                    captureStart()
                    RepeatDetailsThread().start()
                }
            })
        }

    }

    // 이미지 촬영을 쓰레드로 분리
    inner class ImageCaptureThread : Thread() {
        override fun run() {
            Log.e(MACRO_THREAD_TAG, "ImageCaptureThread run")
            takePhoto()
        }
    }

    /// 단순 매일 반복 코드
    inner class SimpleIterationThread : Thread() {
        override fun run() {
            Log.e(MACRO_THREAD_TAG, "Thread Run. \ncycle time: ${cycleTime} s")
            // 본 타입은 특정 조건 없이 어플이 실행되는 동안 항시 촬영을 반복한다.
            while (true) {
                ImageCaptureThread().run()
                SystemClock.sleep(cycleTime * 1000)
            }
        }
    }

    /// 단순 매일 반복 코드 끝
    /// 특정 요일 반복 코드
    inner class RepeatTheDayOfTheWeekThread : Thread() {
        override fun run() {
            Log.e(
                MACRO_THREAD_TAG, "Thread Run. \n" +
                        "cycle time: ${cycleTime} s \n" +
                        "sun: ${weekSetting[0]}\n" +
                        "mon: ${weekSetting[1]}\n" +
                        "tue: ${weekSetting[2]}\n" +
                        "wen: ${weekSetting[3]}\n" +
                        "thu: ${weekSetting[4]}\n" +
                        "fri: ${weekSetting[5]}\n" +
                        "sat: ${weekSetting[6]}\n"
            )
            // 촬영 가능 요일인지 확인 후 하루동안 작동할 쓰레드 생성
            // 시작날 기록
            var todayTemp = -1 // 실존하지 않는 요일로 설정하여 첫 시작시 작동하도록 설
            // 항시 작동하기에 true
            while (true) {
                val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
                if (todayTemp != today) { //같은 날이 아니라면 작동
                    todayTemp = today
                    //오늘이 작동 날이 맞는지 확인
                    if (weekSetting[today] == true) {
                        RepeatTheDayOfTheWeekRunThread().start()
                    }
                }
                SystemClock.sleep(1000)
            }
        }
    }

    inner class RepeatTheDayOfTheWeekRunThread : Thread() {
        override fun run() {
            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
            while (true) {
                if (today == Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1) {
                    // 같은 날이 맞으면 시작
                    ImageCaptureThread().start()
                }
                SystemClock.sleep(cycleTime * 1000)
            }
        }
    }

    /// 특정 요일 반복 코드 끝
    /// 특정 요일 시간 반복 코드
    inner class RepeatDetailsThread : Thread() {
        override fun run() {
            Log.e(
                MACRO_THREAD_TAG, "Thread Run. \n" +
                        "cycle time: ${cycleTime} s \n" +
                        "sun: ${weekSetting[0]}\n" +
                        "mon: ${weekSetting[1]}\n" +
                        "tue: ${weekSetting[2]}\n" +
                        "wen: ${weekSetting[3]}\n" +
                        "thu: ${weekSetting[4]}\n" +
                        "fri: ${weekSetting[5]}\n" +
                        "sat: ${weekSetting[6]}\n" +
                        "Start HH: ${startHH}\n" +
                        "Start mm: ${startmm}\n" +
                        "End HH: ${endHH}\n" +
                        "End mm: ${endmm}"
            )
            // 항시 작동하기에 true
            while (true) {
                val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
                if (weekSetting[today] == true) {
                    // 오늘이 작동하는 날이라면 시작 시간이 맞는지 확인
                    val now: Long = System.currentTimeMillis()
                    val date = Date(now)
                    val nowTimeDateFormatHH = SimpleDateFormat("HH")
                    val nowTimeDateFormatmm = SimpleDateFormat("mm")

                    val nowTimeHH = nowTimeDateFormatHH.format(date).toInt()
                    val nowTimemm = nowTimeDateFormatmm.format(date).toInt()

                    Log.e(MACRO_THREAD_TAG, "현재 시간 ${nowTimeHH}:${nowTimemm}")
                    if (nowTimeHH == startHH && nowTimemm == startmm) {
                        //시작 시간이 맞으면 쓰레드 실행
                        RepeatDetailsRunThread().start()
                        SystemClock.sleep(120 * 1000) // 2분동안 쉬도록 하여 중복 실행 방지
                    }
                }
                SystemClock.sleep(1000)
            }
        }
    }

    var RepeatDetailsRun = true

    inner class RepeatDetailsRunThread : Thread() {
        override fun run() {
            RepeatDetailsRun = true
            Log.e(MACRO_THREAD_TAG, "RepeatDetailsRunThread 실행됨")
            while (RepeatDetailsRun) {
                RepeatDetailsAlgorithmRunThread().start()
                SystemClock.sleep(cycleTime * 1000)
            }
            Log.e(MACRO_THREAD_TAG, "RepeatDetailsRunThread 종료")
        }
    }

    inner class RepeatDetailsAlgorithmRunThread : Thread() {
        override fun run() {
            Log.e(
                MACRO_THREAD_TAG,
                "RepeatDetailsAlgorithmRunThread 실행됨\nRepeatDetailsRunThread-run=${RepeatDetailsRun}"
            )
            val now: Long = System.currentTimeMillis()
            val date = Date(now)
            val nowTimeDateFormatHH = SimpleDateFormat("HH")
            val nowTimeDateFormatmm = SimpleDateFormat("mm")

            val nowTimeHH = nowTimeDateFormatHH.format(date).toInt()
            val nowTimemm = nowTimeDateFormatmm.format(date).toInt()
            if ((startHH * 100 + startmm) - (endHH * 100 + endmm) < 0) {
                if (startHH <= nowTimeHH && nowTimeHH <= endHH) {
                    if (nowTimeHH == endHH && nowTimemm > endmm) {
                        RepeatDetailsRun = false
                        Log.e(
                            MACRO_THREAD_TAG,
                            "작동 시간이 아님 code:1\n작동 시간: ${startHH}:${startmm}~${endHH}:${endmm}\n현재 시간: ${nowTimeHH}:${nowTimemm}"
                        )
                        return
                    } else if (nowTimeHH == startHH && nowTimemm < startmm) {
                        RepeatDetailsRun = false
                        Log.e(
                            MACRO_THREAD_TAG,
                            "작동 시간이 아님 code:2\n작동 시간: ${startHH}:${startmm}~${endHH}:${endmm}\n현재 시간: ${nowTimeHH}:${nowTimemm}"
                        )
                        return
                    }
                } else {
                    RepeatDetailsRun = false
                    Log.e(
                        MACRO_THREAD_TAG,
                        "작동 시간이 아님 code:3\n작동 시간: ${startHH}:${startmm}~${endHH}:${endmm}\n현재 시간: ${nowTimeHH}:${nowTimemm}"
                    )
                    return
                }
            } else {
                if (startHH <= nowTimeHH) {//시작 시간보다 큰지 확인
                    if (startHH == nowTimeHH) {
                        if (!(startmm <= nowTimemm)) {
                            RepeatDetailsRun = false
                            Log.e(
                                MACRO_THREAD_TAG,
                                "작동 시간이 아님 code:4\n작동 시간: ${startHH}:${startmm}~${endHH}:${endmm}\n현재 시간: ${nowTimeHH}:${nowTimemm}"
                            )
                            return
                        }
                    }
                } else if (nowTimeHH <= endHH) {
                    if (nowTimeHH == endHH) {
                        if (!(nowTimemm <= endmm)) {
                            RepeatDetailsRun = false
                            Log.e(
                                MACRO_THREAD_TAG,
                                "작동 시간이 아님 code:5\n작동 시간: ${startHH}:${startmm}~${endHH}:${endmm}\n현재 시간: ${nowTimeHH}:${nowTimemm}"
                            )
                            return
                        }
                    }
                } else {
                    RepeatDetailsRun = false
                    Log.e(
                        MACRO_THREAD_TAG,
                        "작동 시간이 아님 code:6\n작동 시간: ${startHH}:${startmm}~${endHH}:${endmm}\n현재 시간: ${nowTimeHH}:${nowTimemm}"
                    )
                    return
                }
            }

            ImageCaptureThread().start()
        }
    }

    private fun captureStart() {
        val linearLayoutStart = findViewById<LinearLayout>(R.id.linearLayoutStart)
        val linearLayoutRunning = findViewById<LinearLayout>(R.id.linearLayoutRunning)
        linearLayoutStart.isVisible = false
        linearLayoutRunning.isVisible = true
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val viewFinder = findViewById<PreviewView>(R.id.viewFinder)
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
                val cameraControl = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                //줌 리스너 추가
                val seekBar = findViewById<SeekBar>(R.id.seekBar)
                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        cameraControl.cameraControl.setLinearZoom(progress / 100.toFloat())
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding faild", exc)
            }
        }, ContextCompat.getMainExecutor(this))

        imageCapture = ImageCapture.Builder().build()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            outputDirectory, SimpleDateFormat(FILENAME_FORMAT, Locale.KOREA)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed ${exception.message}", exception)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.e(TAG, msg)
                }
            }
        )
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }

        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val MACRO_THREAD_TAG = "MacroThread"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        private const val TYPE_SIMPLE_ITERATION = 1
        private const val TYPE_REPEAT_THE_DAY_OF_THE_WEEK = 2
        private const val TYPE_REPEAT_DETAILS = 3
    }
}