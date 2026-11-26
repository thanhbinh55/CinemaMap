package vn.edu.midterm.cinemamap.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import vn.edu.midterm.cinemamap.R
import vn.edu.midterm.cinemamap.model.Seat
import vn.edu.midterm.cinemamap.model.SeatType
import java.text.NumberFormat
import java.util.Locale

internal fun Seat.typeLabelResource(): Int = when (type) {
    SeatType.STANDARD -> R.string.seat_standard
    SeatType.VIP -> R.string.seat_vip
    SeatType.COUPLE -> R.string.seat_couple
}

data class SelectedSeatItem(val seat: Seat, val outsideFilter: Boolean)

class SelectedSeatAdapter(
    private val onLocate: (Seat) -> Unit,
    private val onRemove: (String) -> Unit
) : ListAdapter<SelectedSeatItem, SelectedSeatAdapter.Holder>(object : DiffUtil.ItemCallback<SelectedSeatItem>() {
    override fun areItemsTheSame(old: SelectedSeatItem, new: SelectedSeatItem) = old.seat.id == new.seat.id
    override fun areContentsTheSame(old: SelectedSeatItem, new: SelectedSeatItem) = old == new
}) {
    private val money = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val locate: MaterialButton = view.findViewById(R.id.locate_seat)
        val remove: MaterialButton = view.findViewById(R.id.remove_seat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_selected_seat, parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        val seat = item.seat
        val context = holder.itemView.context
        holder.locate.text = context.getString(
            R.string.seat_item, seat.id, context.getString(seat.typeLabelResource()),
            seat.type.capacity, money.format(seat.type.priceVnd)
        )
        if (item.outsideFilter) {
            holder.locate.append(context.getString(R.string.outside_filter_suffix))
        }
        holder.locate.contentDescription = context.getString(R.string.locate_seat_description, seat.id)
        holder.remove.contentDescription = context.getString(R.string.remove_seat_description, seat.id)
        holder.locate.setOnClickListener { onLocate(seat) }
        holder.remove.setOnClickListener { onRemove(seat.id) }
    }
}
