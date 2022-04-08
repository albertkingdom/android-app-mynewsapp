package com.example.mynewsapp.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import com.example.mynewsapp.R
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.fragment.app.activityViewModels



class CustomDialogFragment : DialogFragment() {
    /**
     * this dialog is for adding new stockNo
     */
    val TAG = "CustomDialogFragment"
    private val viewModel: NewsViewModel by activityViewModels()


    override fun onStart() {
        super.onStart()
        // setup the width and height of dialog
        dialog?.window?.setLayout(WRAP_CONTENT, WRAP_CONTENT)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // ===== setup for auto complete text view ====
        val view: View = inflater.inflate(R.layout.fragment_addnewstock, container, false)
        val textView = view.findViewById<View>(R.id.input_stockNo) as AutoCompleteTextView

        val countries: Array<String> = resources.getStringArray(R.array.countries_array)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1, countries
        )

        textView.setAdapter(adapter)
        //=========
        dialog?.setTitle("加入追蹤")

        val confirmButton = view.findViewById<Button>(R.id.confirm_button)
        confirmButton.setOnClickListener { view ->

            val stockNoInput = textView.text.toString().split(" ")[0]
            if(stockNoInput.isNotEmpty()) {
                viewModel.addToStockList(stockNoInput)
                dismiss()

            }
        }
       return view
    }
}