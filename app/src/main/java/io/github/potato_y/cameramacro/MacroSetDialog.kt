package io.github.potato_y.cameramacro

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.WindowManager
import android.widget.*
import androidx.core.view.isVisible

class MacroSetDialog(private val context: Context) {
    private val dialog = Dialog(context)

    fun start() {
        dialog.setContentView(R.layout.activity_macro_set_dialog)

        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        //xml kt 연결
        val buttonStart = dialog.findViewById<Button>(R.id.buttonStart)
        val cycleSecond = dialog.findViewById<EditText>(R.id.cycleSecond)
        val switchDateSet = dialog.findViewById<Switch>(R.id.switchDateSet)
        val linearLayoutDateSet = dialog.findViewById<LinearLayout>(R.id.LinearLayoutDateSet)
        val checkBoxAll = dialog.findViewById<CheckBox>(R.id.checkBox_All)
        val checkBoxSun = dialog.findViewById<CheckBox>(R.id.checkBox_Sun)
        val checkBoxMon = dialog.findViewById<CheckBox>(R.id.checkBox_Mon)
        val checkBoxTue = dialog.findViewById<CheckBox>(R.id.checkBox_Tue)
        val checkBoxWen = dialog.findViewById<CheckBox>(R.id.checkBox_Wen)
        val checkBoxThu = dialog.findViewById<CheckBox>(R.id.checkBox_Thu)
        val checkBoxFri = dialog.findViewById<CheckBox>(R.id.checkBox_Fri)
        val checkBoxSat = dialog.findViewById<CheckBox>(R.id.checkBox_Sat)
        val switchActivityTime = dialog.findViewById<Switch>(R.id.switchActivityTime)
        val tableLayoutActivityTime = dialog.findViewById<TableLayout>(R.id.TableLayoutActivityTime)
        val startTimeHH = dialog.findViewById<EditText>(R.id.StartTimeHH)
        val startTimemm = dialog.findViewById<EditText>(R.id.StartTimemm)
        val endTimeHH = dialog.findViewById<EditText>(R.id.EndTimeHH)
        val endTimemm = dialog.findViewById<EditText>(R.id.EndTimemm)

        //check박스를 다른 함수로 전달하기 위해 배열로 전달한다.
        var dayCheckBoxArray = arrayOf<CheckBox>(
            checkBoxAll,
            checkBoxSun,
            checkBoxMon,
            checkBoxTue,
            checkBoxWen,
            checkBoxThu,
            checkBoxFri,
            checkBoxSat
        )

        //리스너 연결
        // 스위치를 통해 설정을 활성화 비활성화 한다.
        switchDateSet.setOnClickListener {
            if (switchDateSet.isChecked == false) {
                linearLayoutDateSet.isVisible = false
            } else {
                linearLayoutDateSet.isVisible = true
            }
            Log.e("switch", linearLayoutDateSet.visibility.toString())
        }

        //전체 선택을 하면 작동한다.
        checkBoxAll.setOnClickListener {
            if (checkBoxAll.isChecked == true) {
                allDateCheck(dayCheckBoxArray)
            } else {
                notAllDateCheck(dayCheckBoxArray)
            }
        }

        //각각의 체크박스 리스너를 추가한다.
        checkBoxSun.setOnClickListener { dateSettingCheck(dayCheckBoxArray) }
        checkBoxMon.setOnClickListener { dateSettingCheck(dayCheckBoxArray) }
        checkBoxTue.setOnClickListener { dateSettingCheck(dayCheckBoxArray) }
        checkBoxWen.setOnClickListener { dateSettingCheck(dayCheckBoxArray) }
        checkBoxThu.setOnClickListener { dateSettingCheck(dayCheckBoxArray) }
        checkBoxFri.setOnClickListener { dateSettingCheck(dayCheckBoxArray) }
        checkBoxSat.setOnClickListener { dateSettingCheck(dayCheckBoxArray) }

        switchActivityTime.setOnClickListener {
            if (switchActivityTime.isChecked == true) {
                tableLayoutActivityTime.isVisible = true
            } else {
                tableLayoutActivityTime.isVisible = false
            }
        }

        buttonStart.setOnClickListener {
            //기본 작업
            //cycle 값 확인
            if (cycleSecond.text.length == 0) {
                Toast.makeText(
                    context,
                    "The cycle value must be greater than 1.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (cycleSecond.text.toString().toLong() < 1) {
                Toast.makeText(
                    context,
                    "The cycle value must be greater than 1.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            var cycleTemp = cycleSecond.text.toString().toLong()

            if (!switchDateSet.isChecked && !switchActivityTime.isChecked) {
                onClickedListener.onClicked(cycleTemp)
            } else if (switchDateSet.isChecked && !switchActivityTime.isChecked) {
                if (!checkBoxSun.isChecked && !checkBoxMon.isChecked && !checkBoxTue.isChecked && !checkBoxWen.isChecked && !checkBoxThu.isChecked && !checkBoxFri.isChecked && !checkBoxSat.isChecked) {
                    Toast.makeText(context,"Day error",Toast.LENGTH_SHORT).show()
                    Log.e("DayError","1")
                    return@setOnClickListener
                }

                if (checkBoxAll.isChecked && !switchActivityTime.isChecked) { //매일이지만 시간 조건이 없으면 단순 반복과 동일
                    onClickedListener.onClicked(cycleTemp)
                } else {
                    onClickedListener.onClicked(
                        cycleTemp,
                        checkBoxSun.isChecked,
                        checkBoxMon.isChecked,
                        checkBoxTue.isChecked,
                        checkBoxWen.isChecked,
                        checkBoxThu.isChecked,
                        checkBoxFri.isChecked,
                        checkBoxSat.isChecked
                    )
                }
            } else if (switchDateSet.isChecked && switchActivityTime.isChecked) {
                if (!checkBoxSun.isChecked && !checkBoxMon.isChecked && !checkBoxTue.isChecked && !checkBoxWen.isChecked && !checkBoxThu.isChecked && !checkBoxFri.isChecked && !checkBoxSat.isChecked) {
                    Toast.makeText(context,"Day error",Toast.LENGTH_SHORT).show()
                    Log.e("DayError","2")
                    return@setOnClickListener
                }
                if (startTimeHH.text.length == 0) {
                    Toast.makeText(context, "Start time error (HH)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (startTimemm.text.length == 0) {
                    Toast.makeText(context, "Start time error (mm)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (endTimeHH.text.length == 0) {
                    Toast.makeText(context, "End time error (HH)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (endTimemm.text.length == 0) {
                    Toast.makeText(context, "End time error (mm)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!(-1 < startTimeHH.text.toString().toInt() && startTimeHH.text.toString()
                        .toInt() < 25)
                ) {
                    Toast.makeText(context, "Start time error (HH)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!(-1 < startTimemm.text.toString().toInt() && startTimemm.text.toString()
                        .toInt() < 60)
                ) {
                    Toast.makeText(context, "Start time error (mm)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!(-1 < endTimeHH.text.toString().toInt() && endTimeHH.text.toString()
                        .toInt() < 25)
                ) {
                    Toast.makeText(context, "End time error (HH)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!(-1 < endTimemm.text.toString().toInt() && endTimemm.text.toString()
                        .toInt() < 60)
                ) {
                    Toast.makeText(context, "End time error (mm)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (startTimeHH.text == endTimeHH.text && startTimemm.text == endTimemm.text) {
                    Toast.makeText(
                        context,
                        "The start time and end time are the same.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                onClickedListener.onClicked(
                    cycleTemp,
                    checkBoxSun.isChecked,
                    checkBoxMon.isChecked,
                    checkBoxTue.isChecked,
                    checkBoxWen.isChecked,
                    checkBoxThu.isChecked,
                    checkBoxFri.isChecked,
                    checkBoxSat.isChecked,
                    startTimeHH.text.toString().toInt(),
                    startTimemm.text.toString().toInt(),
                    endTimeHH.text.toString().toInt(),
                    endTimemm.text.toString().toInt()
                )
            } else {
                Toast.makeText(context, "Value Error", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            dialog.dismiss()
        }


    }

    //모든 날이 체크되면 All 체크박스가 작동하도록 한다.
    fun allDateCheck(dayCheckBoxArray: Array<CheckBox>) {
        for (i in 1..dayCheckBoxArray.size - 1) {
            dayCheckBoxArray[i].isChecked = true
            dayCheckBoxArray[i].isEnabled = false
        }
    }

    //모든 체크박스를 해제한다.
    fun notAllDateCheck(dayCheckBoxArray: Array<CheckBox>) {
        for (i in 1..dayCheckBoxArray.size - 1) {
            dayCheckBoxArray[i].isChecked = false
            dayCheckBoxArray[i].isEnabled = true
        }

        dayCheckBoxArray[1].isChecked = false;
    }

    //모든 체크박스가 체크되어있는지 확인한다.
    fun dateSettingCheck(dayCheckBoxArray: Array<CheckBox>) {
        var value = true
        for (i in 1..dayCheckBoxArray.size - 1) {
            if (dayCheckBoxArray[i].isChecked == false) {
                value = false
            }
        }
        if (value == true) {
            allDateCheck(dayCheckBoxArray)
            dayCheckBoxArray[0].isChecked = true
        }
    }

    interface ButtonClickListener {
        fun onClicked(cycle: Long) //단순 주기 반복
        fun onClicked( //특정 요일마다 주기 반복
            cycle: Long,
            sun: Boolean,
            mon: Boolean,
            tue: Boolean,
            wen: Boolean,
            thu: Boolean,
            fri: Boolean,
            sat: Boolean
        )

        fun onClicked( //특정 요일 설정 시간부터 시간까지 주기 반복
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
        )
    }

    private lateinit var onClickedListener: ButtonClickListener

    fun setOnClickListener(listener: ButtonClickListener) {
        onClickedListener = listener
    }

}