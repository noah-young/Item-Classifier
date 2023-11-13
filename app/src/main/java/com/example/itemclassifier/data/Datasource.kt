package com.example.itemclassifier.data

import com.example.itemclassifier.R
import com.example.itemclassifier.model.Item

class Datasource {
    /**
     * Return all items
     */
    fun loadItems(): List<Item> {
        return listOf<Item>(
            Item(R.string.Book, R.string.bookDesc, R.drawable.book),
            Item(R.string.Pencil, R.string.pencilDesc, R.drawable.pencil),
            Item(R.string.Mug, R.string.mugDesc, R.drawable.mug),
            Item(R.string.Scissors, R.string.scissorsDesc, R.drawable.scissors),
            Item(R.string.Remote, R.string.remoteDesc, R.drawable.remote)
        )
    }

    /**
     * Return specific item
     */
    fun loadItem(name: String): Item {
        val nameId: Int
        val descId: Int
        val imageId: Int

        when (name) {
            "Book" -> {
                nameId = R.string.Book
                descId = R.string.bookDesc
                imageId = R.drawable.book
            }
            "Mug" -> {
                nameId = R.string.Mug
                descId = R.string.mugDesc
                imageId = R.drawable.mug
            }
            "Pencil" -> {
                nameId = R.string.Pencil
                descId = R.string.pencilDesc
                imageId = R.drawable.pencil
            }
            "Remote" -> {
                nameId = R.string.Remote
                descId = R.string.remoteDesc
                imageId = R.drawable.remote
            }
            "Scissors" -> {
                nameId = R.string.Scissors
                descId = R.string.scissorsDesc
                imageId = R.drawable.scissors
            }
            else -> {
                nameId = R.string.Book
                descId = R.string.bookDesc
                imageId = R.drawable.book
            }
        }

        return Item(nameId, descId, imageId)
    }
}