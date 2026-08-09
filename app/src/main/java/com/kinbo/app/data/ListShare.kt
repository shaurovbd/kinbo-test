package com.kinbo.app.data

import android.content.Context
import android.content.Intent
import com.kinbo.app.model.ShoppingList

object ListShare {

    fun buildShareText(list: ShoppingList): String {
        val header = "${list.emoji} ${list.name}\n"
        val progress = if (list.items.isEmpty()) {
            "No items yet."
        } else {
            "${list.purchasedCount}/${list.totalItems} bought"
        }
        val total = if (list.estimatedTotal > 0) " · Total: \$${"%.2f".format(list.estimatedTotal)}" else ""
        val items = if (list.items.isEmpty()) {
            "\n(empty)"
        } else {
            list.items.joinToString("\n") { item ->
                val box = if (item.purchased) "✅" else "⬜"
                val qty = if (item.quantity != 1.0) " x${item.quantity.trimTrailing()}" else ""
                val price = if (item.price > 0) " — \$${"%.2f".format(item.price)}" else ""
                "$box ${item.name}$qty$price"
            }
        }
        return header + progress + total + "\n" + items
    }

    fun share(context: Context, list: ShoppingList) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${list.emoji} ${list.name} — Kinbo")
            putExtra(Intent.EXTRA_TEXT, buildShareText(list))
        }
        val chooser = Intent.createChooser(intent, "Share \"${list.name}\"").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}

private fun Double.trimTrailing(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
