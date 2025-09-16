package com.ola.maps.sample

import androidx.annotation.DrawableRes

enum class ManeuverImageType(val imageName: String, @DrawableRes val imageResourceId: Int) {
    NULL("null", 0);
    /*NULL("null", 0),
    CONTINUE("continue", R.drawable.ic_straight),
    TURN_SHARP_RIGHT("turn-sharp-right", R.drawable.ic_sharp_right),
    TURN_RIGHT("turn-right", R.drawable.ic_right),
    TURN_SLIGHT_RIGHT("turn-slight-right", R.drawable.ic_slight_right),
    U_TURN("u-turn", R.drawable.ic_uturn_right),
    TURN_SHARP_LEFT("turn-sharp-left", R.drawable.ic_sharp_left),
    TURN_LEFT("turn-left", R.drawable.ic_left),
    TURN_SLIGHT_LEFT("turn-slight-left", R.drawable.ic_slight_left),
    DEPART("depart", R.drawable.ic_direction_depart),
    ENTER_ROUNDABOUT("enter-roundabout", R.drawable.ic_direction_roundabout),
    ARRIVE("arrive", R.drawable.ic_navigation_done),
    ARRIVING("arriving", R.drawable.ic_location);

*/    companion object {
        fun getImageResourceId(imageName: String): Int {
            return values().find { it.imageName == imageName }?.imageResourceId ?: 0
        }
    }
}