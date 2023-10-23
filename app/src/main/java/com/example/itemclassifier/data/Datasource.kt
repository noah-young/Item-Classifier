package com.example.itemclassifier.data

import com.example.itemclassifier.R
import com.example.itemclassifier.model.Item

class Datasource {
    fun loadItems(): List<Item> {
        return listOf<Item>(
            Item(R.string.book, R.string.bookDesc, R.drawable.book),
            Item(R.string.pencil, R.string.pencilDesc, R.drawable.pencil),
            Item(R.string.mug, R.string.mugDesc, R.drawable.mug),
            Item(R.string.scissors, R.string.scissorsDesc, R.drawable.scissors),
            Item(R.string.remote, R.string.remoteDesc, R.drawable.remote)
        )
    }
}