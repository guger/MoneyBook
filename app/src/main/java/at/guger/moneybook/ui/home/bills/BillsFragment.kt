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

package at.guger.moneybook.ui.home.bills

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseFragment
import at.guger.moneybook.core.ui.recyclerview.decoration.SpacesItemDecoration
import at.guger.moneybook.core.util.ext.dimen
import at.guger.moneybook.ui.home.HomeViewModel
import kotlinx.android.synthetic.main.fragment_recyclerview.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Fragment for [home view pager's][ViewPager2] bills content.
 */
class BillsFragment : BaseFragment() {

    //region Variables

    private val viewModel: HomeViewModel by sharedViewModel()

    //endregion

    //region Fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(mAccountsRecyclerView) {
            setHasFixedSize(true)

            layoutManager = LinearLayoutManager(requireContext())
//          TODO  adapter = OverviewAdapter(viewModel)
            addItemDecoration(SpacesItemDecoration(context.dimen(res = R.dimen.recyclerview_item_spacing).toInt()))
        }
    }

    //endregion

    companion object {
        fun instantiate(): BillsFragment = BillsFragment()
    }
}