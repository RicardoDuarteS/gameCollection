package com.wisefoxes.kotlinartbook

import android.graphics.Bitmap

class Globals {

    companion object ChosenImage{

        var chosenImage: Bitmap? = null

        fun returnImage(): Bitmap{
            return chosenImage!!
        }
    }
}