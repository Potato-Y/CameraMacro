package io.github.potato_y.cameramacro

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager

class OpenSourcePage(private val context: Context) {
    private val dialog = Dialog(context)

    fun start(){
        dialog.setContentView(R.layout.activity_open_source_page)

        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }
}