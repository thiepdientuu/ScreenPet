package com.ls.petfunny.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding


abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel> :
    Fragment(), IFragmentOperator {


    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding!!

    abstract fun getLayoutId(): Int

    abstract fun observersSomething()

    abstract fun bindingAction()

    abstract fun viewCreated()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCreated()
        bindingAction()
        observersSomething()
    }


    override fun getCurrentFragment(containerId: Int): BaseFragment<*, *>? {
        return childFragmentManager.findFragmentById(containerId) as BaseFragment<*, *>
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IFragmentOperator) {
            (context as IFragmentOperator).onFragmentAttach(this)
        }
        if (parentFragment is IFragmentOperator) {
            (parentFragment as IFragmentOperator).onFragmentAttach(this)
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (activity is IFragmentOperator) {
            (activity as IFragmentOperator).onFragmentDetach(this)
        }
        if (parentFragment is IFragmentOperator) {
            (parentFragment as IFragmentOperator).onFragmentDetach(this)
        }
    }

    override fun onFragmentAttach(fragment: BaseFragment<*, *>) {

    }

    override fun onFragmentDetach(fragment: BaseFragment<*, *>) {

    }

    open fun onBackPressed(): Boolean {
        return false
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    companion object{
        const val TAG_NAME = "BaseFragment"

        // share
        const val SHARE_CODE = 100

        // download
        const val DOWNLOAD_CODE = 101

        // SET
        const val SET_DATA_CODE = 102

        //Local Storage
        const val LOCAL_STORAGE_CODE = 104

        // open settings
        const val REQUEST_CODE_SETTINGS = 105

        const val REQUEST_CODE_CONTACTS = 106
    }
}