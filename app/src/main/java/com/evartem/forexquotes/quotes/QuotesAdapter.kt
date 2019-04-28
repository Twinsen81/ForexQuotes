package com.evartem.forexquotes.quotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.evartem.forexquotes.R
import com.evartem.forexquotes.remote.model.Quote
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_quote.*

class QuotesAdapter : RecyclerView.Adapter<QuotesAdapter.QuoteViewHolder>() {

    private var quotes = listOf<Quote>()

    fun setData(newData: List<Quote>) {

        val oldData = quotes
        quotes = newData
        notifyChanges(oldData, newData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder =
        QuoteViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_quote, parent, false)
        )

    override fun getItemCount() = quotes.size

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) = holder.onBind(quotes[position])

    fun getSymbolsForPositions(range: Pair<Int, Int>): List<String> =
        if (quotes.isNotEmpty())
            quotes.subList(
                range.first,
                if (range.second > quotes.size) quotes.size else (range.second + 1)
            ).map { it.symbol }
        else
            emptyList()

    private fun notifyChanges(oldList: List<Quote>, newList: List<Quote>) {

        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldList[oldItemPosition].symbol == newList[newItemPosition].symbol

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                return oldItem.price == newItem.price &&
                        oldItem.ask == newItem.ask &&
                        oldItem.bid == newItem.bid &&
                        oldItem.timestamp == newItem.timestamp
            }

            override fun getOldListSize() = oldList.size

            override fun getNewListSize() = newList.size
        })

        diff.dispatchUpdatesTo(this)
    }

    class QuoteViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun onBind(item: Quote) {

            itemQuoteSymbol.text = item.symbol

            if (item.timestamp != 0L) {
                itemQuoteBidAsk.text = containerView.context.resources.getString(
                    R.string.bid_ask,
                    item.bid.toPlainString(),
                    item.ask.toPlainString()
                )
                itemQuotePrice.text = item.price.toPlainString()
                itemQuoteTimestamp.text = getUpdatedTime(item.timestamp)
            } else
            {
                val placeholderText = containerView.context.resources.getString(R.string.data_placeholder)
                itemQuoteBidAsk.text = placeholderText
                itemQuotePrice.text = placeholderText
                itemQuoteTimestamp.text = placeholderText
            }

        }

        private fun getUpdatedTime(timestamp: Long): String {
            var secondsPassedString = "\u221E"
            val secondsPassed = System.currentTimeMillis() / 1000 - timestamp

            if (secondsPassed < 0)
                secondsPassedString = "0"

            if (secondsPassed < 1000)
                secondsPassedString = secondsPassed.toString()

            return containerView.context.resources.getString(R.string.updated, secondsPassedString)
        }
    }
}