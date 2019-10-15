/*
 * Copyright 2019 Daniel Guger
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package at.guger.moneybook.ui.home.dues

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseFragment
import at.guger.moneybook.core.ui.recyclerview.decoration.SpacesItemDecoration
import at.guger.moneybook.core.util.ext.dimen
import at.guger.moneybook.core.util.ext.setup
import at.guger.moneybook.ui.home.HomeViewModel
import kotlinx.android.synthetic.main.fragment_dues.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Fragment for [home view pager's][ViewPager2] bills content.
 */
class DuesFragment : BaseFragment() {

    //region Variables

    lateinit var adapter: DuesAdapter

    private val viewModel: HomeViewModel by sharedViewModel()

    //endregion

    //region Fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dues, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DuesAdapter().apply { viewModel.claimsAndDebts.observe(viewLifecycleOwner, Observer(::submitList)) }

        mDuesRecyclerView.setup(LinearLayoutManager(context), adapter) {
            addItemDecoration(SpacesItemDecoration(context.dimen(res = R.dimen.recyclerview_item_spacing).toInt()))
        }
    }

    //endregion

    companion object {
        fun instantiate(): DuesFragment = DuesFragment()
    }
}