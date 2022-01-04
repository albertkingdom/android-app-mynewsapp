package com.example.mynewsapp.ui

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.mynewsapp.MainActivity
import com.example.mynewsapp.R

class CustomDialogFragment : DialogFragment() {
    /**
     * this dialog is for adding new stockNo
     */

    private lateinit var viewModel: NewsViewModel
    private lateinit var editText: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = (activity as MainActivity).viewModel

        //editText = binding.inputStockNo.text.toString()
        return AlertDialog.Builder(requireContext())
            .setView(R.layout.fragment_addnewstock)
            .setTitle("加入新股票")
            .setPositiveButton(R.string.dialog_addStockNo,
                DialogInterface.OnClickListener { dialog, id ->
                    editText =
                        (dialog as Dialog).findViewById<EditText>(R.id.input_stockNo).text.toString()
                    //Log.d("dialog edittext",editText)
                    viewModel.addToStockList(editText)
                })
            .setNegativeButton(R.string.dialog_cancel,
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })
            // Create the AlertDialog object and return it
            .create()
    }

    override fun onStart() {
        super.onStart()
        val positiveButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE)
        positiveButton.setTextColor(Color.LTGRAY)
        negativeButton.setTextColor(Color.LTGRAY)

    }


}