package com.olaelectric.navigation.utils

import android.content.Context
import android.location.Location
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.mapbox.geojson.Point


/*
Visisble View for handling visibilty of a view  visibilty = VISIBLE
 */
fun View.show() {
    this.visibility = View.VISIBLE
}

/*
INVISIBLE View for handling visibilty of a view  visibilty = INVISIBLE
 */
fun View.invisible() {
    this.visibility = View.VISIBLE
}

/*
HIDE View for handling visibilty of a view  visibilty = GONE
 */
fun View.hide() {
    this.visibility = View.GONE
}

fun View.handleVisibility(flag: Boolean) {
    if (flag)
        this.visibility = View.VISIBLE
    else
        this.visibility = View.GONE
}


fun ImageView.manageAlpha(zoomLevel: Double, isPlusIcon: Boolean = false,
                          minZoomLevel: Double, maxZoomLevel: Double) {
    if (isPlusIcon) {
        this.isActivated = zoomLevel >= maxZoomLevel
    } else {
        this.isActivated = zoomLevel <= minZoomLevel
    }
}

fun Location.distanceFromEndPosition(endLatitude: Double?, endLongitude: Double?,) : Float {
    val locationEnd = Location("")
    endLatitude?.let {
        endLongitude?.let {
            locationEnd.latitude = endLatitude
            locationEnd.longitude = endLongitude
        }
    }
    return this.distanceTo(locationEnd)
}

//inline fun <reified T : Enum<T>> Intent.putExtra(victim: T): Intent =
//    putExtra(T::class.java.name, victim.ordinal)
//
//inline fun <reified T: Enum<T>> Intent.getEnumExtra(): T? =
//    getIntExtra(T::class.java.name, -1)
//        .takeUnless { it == -1 }
//        ?.let { T::class.java.enumConstants?.get(it) }


fun Location?.toPoint(): Point? {
    return Point.fromLngLat(this?.longitude ?: 0.0, this?.latitude ?: 0.0)
}

fun Context?.showToast(message: String) {
    this?.let {
        Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
    }
}
