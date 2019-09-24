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

package at.guger.moneybook.ui.home.accounts.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseFragment
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.Transaction
import kotlinx.android.synthetic.main.fragment_account_detail.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

/**
 * Fragment displaying the [transactions][Transaction] of an [account][Account].
 */
class AccountDetailFragment : BaseFragment() {

    //region Variables

    private val args: AccountDetailFragmentArgs by navArgs()

    private val viewModel: AccountDetailViewModel by inject { parametersOf(args.accountId) }

    //endregion

    //region Fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.account.observe(viewLifecycleOwner, Observer { requireAppCompatActivity().supportActionBar!!.title = it.name })

        with(mAccountDetailRecyclerView) {
            setHasFixedSize(true)

            layoutManager = LinearLayoutManager(requireContext())
            adapter = AccountsDetailTransactionsListAdapter(viewModel).apply { viewModel.transactions.observe(viewLifecycleOwner, Observer(::submitList)) }
        }
    }

    //endregion
}