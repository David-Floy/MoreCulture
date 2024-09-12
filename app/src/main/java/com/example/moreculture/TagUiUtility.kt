package com.example.moreculture


import android.content.Context
import androidx.core.content.ContextCompat
import com.example.MoreCulture.R
import com.example.MoreCulture.databinding.ActivityAccountEditBinding
import com.example.moreculture.db.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TagUiUtility(private  var binding : ActivityAccountEditBinding?, private var context: Context, private val mainViewModel: MainViewModel) {

    fun updateTagBackgrounds(selectedTags: MutableList<Int>){

        val deselectedTagColor  = ContextCompat.getColor(context, R.color.deselectedTag)
        val selectedTagColor = ContextCompat.getColor(context, R.color.selectedTag)


        for (i in 1..16) {
            val tagView = when (i) {
                1 -> binding?.tag1
                2 -> binding?.tag2
                3 -> binding?.tag3
                4 -> binding?.tag4
                5 -> binding?.tag5
                6 -> binding?.tag6
                7 -> binding?.tag7
                8 -> binding?.tag8
                9 -> binding?.tag9
                10 -> binding?.tag10
                11 -> binding?.tag11
                12 -> binding?.tag12
                13 -> binding?.tag13
                14 -> binding?.tag14
                15 -> binding?.tag15
                16 -> binding?.tag16
                else -> null
            }

            if (tagView != null) {
                if (selectedTags.contains(i)) {
                    tagView.setBackgroundColor(selectedTagColor)
                } else {
                    tagView.setBackgroundColor(deselectedTagColor)
                }
            }
        }
    }
}