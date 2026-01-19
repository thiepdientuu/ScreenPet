package com.ls.petfunny.base

interface IFragmentOperator {

	fun onFragmentAttach(fragment: BaseFragment<*, *>)
	fun onFragmentDetach(fragment: BaseFragment<*, *>)
	fun getCurrentFragment(containerId: Int): BaseFragment<*, *>?
}