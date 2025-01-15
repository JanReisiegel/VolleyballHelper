package com.reisiegel.volleyballhelper.ui.home

import android.app.Activity
import android.view.View
import android.widget.AdapterView

class FileSpinnerActivity: Activity(), AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Another interface callback
    }
}