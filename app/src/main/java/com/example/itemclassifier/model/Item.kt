package com.example.itemclassifier.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Item(
    @StringRes val nameResourceId: Int,
    @StringRes val descResourceId: Int,
    @DrawableRes val imageResourceId: Int
)
