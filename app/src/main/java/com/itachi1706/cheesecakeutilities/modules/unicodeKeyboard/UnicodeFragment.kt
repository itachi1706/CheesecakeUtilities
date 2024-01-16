package com.itachi1706.cheesecakeutilities.modules.unicodeKeyboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.itachi1706.cheesecakeutilities.databinding.FragmentRecyclerViewBinding
import com.itachi1706.cheesecakeutilities.modules.unicodeKeyboard.recyclerAdapters.UnicodeMenuAdapter

/**
 * Created by Kenneth on 12/1/2020.
 * for com.itachi1706.cheesecakeutilities.modules.unicodeKeyboard in CheesecakeUtilities
 */
class UnicodeFragment : Fragment() {

    private lateinit var stringList: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stringList = arguments?.getStringArray("list") ?: arrayOf()
    }

    private var _binding: FragmentRecyclerViewBinding? = null
    private val binding get() = _binding!!
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRecyclerViewBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainMenuRecyclerView.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(context, 2)
        binding.mainMenuRecyclerView.layoutManager = gridLayoutManager
        binding.mainMenuRecyclerView.itemAnimator = DefaultItemAnimator()
        // Set up layout
        val adapter = UnicodeMenuAdapter(stringList)
        binding.mainMenuRecyclerView.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance(list: Array<String>): UnicodeFragment { return UnicodeFragment().apply { arguments = Bundle().apply { putStringArray("list", list) } } }
    }
}