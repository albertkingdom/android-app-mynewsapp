package com.example.mynewsapp.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mynewsapp.R
import com.example.mynewsapp.db.FollowingList
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs

class AddFollowingListDialogFragment: DialogFragment() {
    private val viewModel: NewsViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.AddFollowingListDialogTheme)

        return builder.setView(R.layout.dialog_add_following_list)
            .setTitle("新增追蹤清單")
            .setPositiveButton("新增", DialogInterface.OnClickListener { dialog, id ->
                val newListName = (dialog as Dialog).findViewById<EditText>(R.id.edit_text_new_list_name)?.text.toString()
                val newFollowingList = FollowingList(followingListId = 0, listName = newListName)
                viewModel.createFollowingList(newFollowingList)
//                dismiss()
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })
            .create()
    }



}