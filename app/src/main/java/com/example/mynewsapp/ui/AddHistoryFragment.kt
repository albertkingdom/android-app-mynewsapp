package com.example.mynewsapp.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mynewsapp.MainActivity
import com.example.mynewsapp.R
import com.example.mynewsapp.databinding.FragmentAddHistoryBinding
import com.example.mynewsapp.db.InvestHistory
import java.util.*


class AddHistoryFragment: Fragment() {
    private lateinit var binding: FragmentAddHistoryBinding
    private lateinit var viewModel: NewsViewModel
    private val args: AddHistoryFragmentArgs by navArgs()
    var dateSelected: Long? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddHistoryBinding.inflate(inflater, container, false)
        viewModel =(activity as MainActivity).viewModel
        //change toolbar title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "新增一筆紀錄"

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        var dateSelected: Long? = null
        binding.stockNo.editText?.setText(args.stockNo)
        binding.calendar.setOnDateChangeListener { _: View, year:Int, month:Int, day:Int ->
            val c = Calendar.getInstance()
            c.set(year,month,day)
            dateSelected = c.timeInMillis

        }

        binding.switchBuyOrSell.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.switchBuyOrSell.text = "buy"
            } else {
                binding.switchBuyOrSell.text = "sell"

            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_history_fragment_option_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.finishButton -> {
                addHistoryToDB()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addHistoryToDB() {
        val stockNo = binding.stockNo.editText?.text.toString()
        val amount = binding.amount.editText?.text.toString().toIntOrNull()
        val price = binding.price.editText?.text.toString().toDoubleOrNull()
        val date = if (dateSelected != null) dateSelected else binding.calendar.date
        val isBuy = if (binding.switchBuyOrSell.isChecked) 0 else 1
        if (stockNo.isEmpty()) {
            binding.stockNo.error = "不可為空白"
            return
        } else if (amount == 0 || amount == null) {
            binding.amount.error = "必須大於0"
            return
        } else if (price?.equals(0L) == true || price == null) {
            binding.price.error = "必須大於0"
            return
        }
        val newHistory = InvestHistory(0, stockNo = stockNo, amount = amount, price = price, status = isBuy, date = date!!)

        viewModel.insertHistory(newHistory)

        findNavController().popBackStack()
    }
}