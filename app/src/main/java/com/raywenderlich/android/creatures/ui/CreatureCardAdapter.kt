package com.raywenderlich.android.creatures.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.android.creatures.R
import com.raywenderlich.android.creatures.app.Constants
import com.raywenderlich.android.creatures.app.inflate
import com.raywenderlich.android.creatures.model.Creature
import kotlinx.android.synthetic.main.list_item_creature_card.view.creatureCard
import kotlinx.android.synthetic.main.list_item_creature_card.view.creatureImage
import kotlinx.android.synthetic.main.list_item_creature_card.view.fullName
import kotlinx.android.synthetic.main.list_item_creature_card.view.nameHolder
import kotlinx.android.synthetic.main.list_item_creature_card_jupiter.view.*
import java.lang.IllegalArgumentException

class CreatureCardAdapter(private val creatures: MutableList<Creature>) : RecyclerView.Adapter<CreatureCardAdapter.ViewHolder>() {

    enum class ViewType {
        JUPITER, OTHER
    }

    enum class ScrollDirection {
        UP, DOWN
    }

    var scrollDirection = ScrollDirection.DOWN

    var jupiterSpanSize = 2

    // Inner class to access variables in the outter class: In this case scroll direction
    inner class ViewHolder (itemView: View) : View.OnClickListener, RecyclerView.ViewHolder(itemView) {
        private lateinit var creature: Creature

        init {
            /* Set the onclick listener in the itemView*/
            itemView.setOnClickListener(this)
        }

        fun bind(creature: Creature) {
            this.creature = creature
            val context = itemView.context
            val imageResource = context.resources.getIdentifier(creature.uri, null, context.packageName)
            itemView.creatureImage.setImageResource(imageResource)
            itemView.fullName.text = creature.fullName
            setBackgroundColors(context, imageResource)
            animateView(itemView)
        }

        override fun onClick(view: View?) {
            view?.let {
                val context = it.context
                val intent = CreatureActivity.newIntent(context, creature.id)
                context.startActivity(intent)
            }
        }

        // HELPER FOR DYNAMICALLY SETTING BACK GROUND COLOR BASED ON DOMINANT IMAGE COLOR
        private fun setBackgroundColors(context: Context, imageResource: Int) {
            val image = BitmapFactory.decodeResource(context.resources, imageResource)
            Palette.from(image).generate { palette ->
                palette?.let {
                    val backgroundColor = it.getDominantColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    itemView.creatureCard.setCardBackgroundColor(backgroundColor)
                    itemView.nameHolder.setBackgroundColor(backgroundColor)
                    val textColor = if (isColorDark(backgroundColor)) Color.WHITE else Color.BLACK
                    itemView.fullName.setTextColor(textColor)
                    if (itemView.slogan != null) {
                        itemView.slogan.setTextColor(textColor)
                    }
                }
            }
        }

        private fun animateView(viewToAnimate: View) {
            if (viewToAnimate.animation == null) {
                val animId = if (scrollDirection == ScrollDirection.DOWN) R.anim.slide_from_bottom else R.anim.slide_from_top
                val animation = AnimationUtils.loadAnimation(viewToAnimate.context, animId)
                viewToAnimate.animation = animation
            }
        }

        private fun isColorDark(color: Int): Boolean {
            val darkness = 1 - (0.299 * Color.red(color)) +
                                0.587 * Color.green(color) +
                                0.114 * Color.blue(color) / 255
            return darkness >= 0.5
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            ViewType.OTHER.ordinal -> ViewHolder(parent.inflate(R.layout.list_item_creature_card))
            ViewType.JUPITER.ordinal -> ViewHolder(parent.inflate(R.layout.list_item_creature_card_jupiter))
            else -> throw IllegalArgumentException()
        }

    }

    override fun getItemCount(): Int = creatures.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Call bind on the view holder passing in the item (creature) in the list view position
        holder.bind(creatures[position])
    }

    override fun getItemViewType(position: Int): Int {
        val creature = creatures[position]
        return if (creature.planet == Constants.JUPITER) ViewType.JUPITER.ordinal
        else ViewType.OTHER.ordinal
    }

    fun spanSizeAtPosition(position: Int): Int {
        return if (creatures[position].planet == Constants.JUPITER) {
            jupiterSpanSize
        } else {
            1
        }
    }
}