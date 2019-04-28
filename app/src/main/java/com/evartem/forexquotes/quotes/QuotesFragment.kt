package com.evartem.forexquotes.quotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.quotes_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class QuotesFragment : Fragment() {

    companion object {
        const val MAX_SYMBOLS_PER_REQUEST = 20
    }

    private val viewModel by viewModel<QuotesViewModel>()

    lateinit var viewStateDisposable: Disposable

    private val recyclerViewAdapter = QuotesAdapter()
    private lateinit var recyclerViewLayoutManager: LinearLayoutManager

    var visibleRangeInitialized = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(com.evartem.forexquotes.R.layout.quotes_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {

        recyclerViewLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        quotesRecyclerView.layoutManager = recyclerViewLayoutManager

        quotesRecyclerView.adapter = recyclerViewAdapter

        quotesRecyclerView.addItemDecoration(
            DividerItemDecoration(
                quotesRecyclerView.context, recyclerViewLayoutManager.orientation
            )
        )

        quotesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    updateVisibleRange()
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!visibleRangeInitialized) {
                    visibleRangeInitialized = true
                    updateVisibleRange()
                }
            }
        })
    }

    private fun updateVisibleRange() =
        viewModel.getEventsSubject().onNext(QuotesEvent.ChangeVisibleSymbols(getVisibleSymbols()))

    private fun getVisibleRange(): Pair<Int, Int>? {
        val first = recyclerViewLayoutManager.findFirstVisibleItemPosition()

        var last = recyclerViewLayoutManager.findLastVisibleItemPosition()
        if (last < recyclerViewAdapter.itemCount - 1) last++

        if (first >= 0 && last >= 0 && first <= last) {
            if (last - first > MAX_SYMBOLS_PER_REQUEST) last = first + MAX_SYMBOLS_PER_REQUEST
            return first to last
        }
        return null
    }

    private fun getVisibleSymbols(): List<String> {

        val visibleRange = getVisibleRange()
        if (visibleRange != null)
            return recyclerViewAdapter.getSymbolsForPositions(visibleRange)

        return emptyList()
    }

    override fun onStart() {
        super.onStart()

        viewStateDisposable = viewModel.getViewStateSubject()
            .subscribe { renderViewState(it) }
    }

    private fun renderViewState(state: QuotesViewState) {
        recyclerViewAdapter.setData(state.quotes)
    }

    override fun onStop() {
        super.onStop()

        viewModel.viewIsStopped()
        viewStateDisposable.dispose()
    }
}
