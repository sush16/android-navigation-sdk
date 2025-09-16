package com.ola.maps.sample

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.OnLocationStaleListener
import com.ola.maps.mapslibrary.interfaces.OnOlaMapLocationUpdateCallback
import com.ola.maps.mapslibrary.manager.InfoWindowsAnchor
import com.ola.maps.mapslibrary.models.OlaLatLng
import com.ola.maps.mapslibrary.models.OlaMapsConfig
import com.ola.maps.mapslibrary.models.OlaMarkerOptions
import com.ola.maps.mapslibrary.models.SnippetPropertiesOptions
import com.ola.maps.mapslibrary.utils.MapTileStyle
import com.ola.maps.navigation.ui.v5.MapStatusCallback
import com.ola.maps.navigation.ui.v5.NavigationStatusCallback
import com.ola.maps.navigation.ui.v5.NavigationViewOptions
import com.ola.maps.navigation.ui.v5.instruction.InstructionModel
import com.ola.maps.navigation.ui.v5.listeners.RouteProgressListener
import com.ola.maps.navigation.v5.MapStateChangeCallback
import com.ola.maps.navigation.v5.model.route.NavigationErrorInfo
import com.ola.maps.navigation.v5.model.route.RouteInfoData
import com.ola.maps.navigation.v5.navigation.MapboxNavigationOptions
import com.ola.maps.navigation.v5.navigation.NavigationConstants
import com.ola.maps.navigation.v5.navigation.OlaMapView
import com.ola.maps.navigation.v5.navigation.direction.transform
import com.ola.maps.sample.databinding.ActivityMainBinding
import com.olaelectric.navigation.utils.handleVisibility
import com.olaelectric.navigation.utils.hide
import com.olaelectric.navigation.utils.show
import com.olaelectric.navigation.utils.showToast
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset


class MainActivity : AppCompatActivity(), MapStatusCallback, NavigationStatusCallback,
    RouteProgressListener, OnLocationStaleListener, OnOlaMapLocationUpdateCallback {
    private var olaMapView: OlaMapView? = null
    private var isMapReady = false

    /**
     * currentLatLng and destinationLatLng
     * location is used with Mock Bangalore data
     * It should be changed as per API call
     * */
    private var currentLocation: Location = Location("current");
    private var destinationLatLng: LatLng = LatLng(12.930448,77.621963)

    //Ui params
    private var binding: ActivityMainBinding? = null


    private var isTbTEnabled = false
    private var isRoutesEnabled = false

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        currentLocation.latitude = 12.931096;
        currentLocation.longitude = 77.616287

        olaMapView = binding?.olaMapView
        olaMapView?.onCreate(savedInstanceState)
        //initializeOlaMaps()
        setOnClickListeners()

        //Check for location permission
        if (!isLocationPermissionGranted()) {
            requestLocationPermission()
        } else {
            initializeOlaMaps()
            checkLocationEnabled()
        }

    }

    /**
     * Initialize Ola Maps SDK once you have the location permission and location is enabled
     *
     */
    private fun initializeOlaMaps() {
        olaMapView?.initialize(
            mapStatusCallback = this,
            olaMapsConfig = OlaMapsConfig.Builder()
                .setApplicationContext(applicationContext) //pass the application context here, it is mandatory
                .setMapBaseUrl("https://api.olamaps.io") // pass the Base URL of Ola-Maps here (Stage/Prod URL), it is mandatory
                .setApiKey("API KEY")
                .setProjectId("<Orgination-ID>") //Pass the Origination ID here, it is mandatory
                .setMapTileStyle(MapTileStyle.DEFAULT_LIGHT_STANDARD) //pass the MapTileStyle here, it is Optional.
                .setMinZoomLevel(3.0)
                .setMaxZoomLevel(21.0)
                .setZoomLevel(14.0)
                .build()
        )
    }

    /**
     * Lifecycle methods onResume for Ola Maps SDK
     *
     */
    override fun onResume() {
        super.onResume()
        olaMapView?.onResume()
    }

    /**
     * Lifecycle methods onStart for Ola Maps SDK
     *
     */
    override fun onStart() {
        super.onStart()
        olaMapView?.onStart()
        registerCurrentLocationUpdate()
    }


    private fun setOnClickListeners() {
        binding?.apply {
            previewButton?.setOnClickListener {

                    if (isMapReady) {
                        if (isRoutesEnabled) {
                            isRoutesEnabled = false
                            endTbTRoutes()
                            previewButton.text = "Show Routes"
                        }
                        else {
                            isRoutesEnabled = true
                            showRoutePreview()
                            previewButton.text = "Stop Routes"
                        }
                    }
            }

            tbtRoutesButton?.setOnClickListener {
                if (isMapReady) {
                    if (isTbTEnabled) {
                        isTbTEnabled = false
                        endTbTRoutes()
                        tbtRoutesButton.text = "Start Navigation"
                    } else {
                        tbtRoutesButton.text = "Stop Navigation"
                        showTurnByTurn()
                        isTbTEnabled = true
                    }
                }
            }

            imgZoomOut.setOnClickListener {
                olaMapView.cancelAllVelocityAnimations()
                olaMapView.getMapCameraPositionZoomLevel()?.let { zoom ->
                    var zoomLevel = zoom
                    if (zoomLevel != null && zoomLevel > 3.0) {
                        zoomLevel -= 1
                    }

                    olaMapView.zoomCamera(zoomLevel)
                }
            }

            compassView.setOnClickListener {
                olaMapView?.resetMapBearing()
                Toast.makeText(this@MainActivity, "Compass Clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun recenterMapAtLocation() {
        binding?.imgMapCenter?.setOnClickListener {
            if (isMapReady)
                olaMapView?.moveToCurrentLocation()
        }
    }

    /**
     * This method is called when the map is ready to use
     *
     */
    override fun onMapReady() {
        isMapReady = true
        recenterMapAtLocation()
        olaMapView?.forceLocationUpdate(currentLocation)
        registerCurrentLocationUpdate()
    }


    fun showGeoFencing() {
        if (isMapReady) {
            olaMapView?.setDrawableOverlayOnMap(
                radius = 1000.0f,
                centerLatLng = OlaLatLng(currentLocation.latitude, currentLocation.longitude),
                othersLatLang = listOf(OlaLatLng(12.936839, 77.674448)),
                drawableId = R.drawable.overlay
            )
        }
    }


    private fun showRoutePreview() {
        olaMapView?.getNavigationMapRoute()?.removeRoute()
        olaMapView?.setMapPadding(80,80, 80, 80)
        showRoutePreviewUiChanges()
        setRoutesPreviewData()
    }

    private fun reCenterRoutePreview() {
         olaMapView?.animateCamera(LatLng(currentLocation.latitude, currentLocation.longitude), destinationLatLng)
    }

    private fun showTurnByTurn() {
        //UI changes for Tbt mode
        showTbTUIChanges()

        val routeInfoData: RouteInfoData = Gson().fromJson(getMockData(), RouteInfoData::class.java)
        val directionsRoute = transform(routeInfoData)
        val options = getNavigationViewOptions(directionsRoute)

        olaMapView?.setMapPadding(20,20, 20, 20)

        if (options != null) {
            olaMapView?.setOnLocationStaleListener(this@MainActivity)
            olaMapView?.addRouteProgressListener(this@MainActivity)
            olaMapView?.registerNavigationStatusCallback(this@MainActivity)
            // olaMap?.startWalkingNavigation(options)
            olaMapView?.startNavigation(options)
        }


    }

    private fun showTbTUIChanges() {
        binding?.compassView?.show()
        olaMapView?.registerMapStateChangeListener(mapStateChangeCallback)
        binding?.imgMapCenter?.show()
        binding?.imgMapCenter?.setImageResource(R.drawable.ic_recenter)
        binding?.imgMapCenter?.setOnClickListener { recenterMapForRoutePreviewToggle() }
//        olaMapView?.setUserLocationComponent(useDefault = true)
    }

    private val mapStateChangeCallback: MapStateChangeCallback by lazy {
        object : MapStateChangeCallback {
            override fun onBearingChange(bearing: Double) {
                binding?.compassView?.update(bearing.toFloat())
                binding?.compassView?.handleVisibility(bearing in 2.0..358.0)
            }
        }
    }

    private fun recenterMapForRoutePreviewToggle() {
        val isRouteInPreviewState = olaMapView?.isInRouteOverviewMode()

        if (isRouteInPreviewState == true) {
            binding?.imgMapCenter?.setImageResource(R.drawable.ic_recenter)
        } else {
            binding?.imgMapCenter?.setImageResource(R.drawable.ic_navigation_route_preview)
        }
        olaMapView?.toggleRouteOverview()
    }

    private fun showRoutePreviewUiChanges() {
        binding?.compassView?.hide()
        binding?.imgMapCenter?.show()
        binding?.imgMapCenter?.setImageResource(R.drawable.ic_navigation_route_preview)
        binding?.imgMapCenter?.setOnClickListener { reCenterRoutePreview() }
        olaMapView?.removeDottedLine()
        if (isMapReady) {
            olaMapView?.toggleLocationComponent(true)
        }
    }

    private fun setRoutesPreviewData() {
        val routeInfoData: RouteInfoData = Gson().fromJson(getMockData(), RouteInfoData::class.java)

        val tempRotesList = arrayListOf<DirectionsRoute>()
        routeInfoData.routes?.let {
            //make it asynchronous call
            for (i in it) {
                tempRotesList.add(transform(i))
            }
        }
        olaMapView?.getNavigationMapRoute()?.addRoutesForRoutePreview(tempRotesList)

        olaMapView?.getNavigationMapRoute()?.setOnRouteSelectionChangeListener { route ->
            tempRotesList?.let {
                val index = it.indexOf(element = route)
                if (index != -1) {
                    showToast("Route ${index + 1} selected")
                }
            }
        }
    }


    private fun registerCurrentLocationUpdate() {
        if (isMapReady) {
            olaMapView?.registerCurrentLocationUpdate(this@MainActivity)
        }
    }

    override fun onMapLoadFailed(errorMessage: String?) {
        Log.d("MainActivity", "$errorMessage")
    }

    var markerOptions1: OlaMarkerOptions? = null

    private fun getSnippetPropertiesOptions(): SnippetPropertiesOptions {
        return SnippetPropertiesOptions.Builder()
            .setSubSnippetTextColorHexCode("#000000")
            .setSubSnippetTextSize(12f)
            .setInfoWindowStrokeWidth(1f)
            .setInfoWindowStrokeColor("#000000")
            .setInfoWindowRadius(20f)
            .setTextColorHexCode("#000000")
            .setInfoWindowBackgroundColorHex("#ffffff")
            .setInfoWindowAnchor(InfoWindowsAnchor.TOP)
            .build()
    }

    private fun getNavigationViewOptions(directionsRoute: DirectionsRoute): NavigationViewOptions? {
        return NavigationViewOptions.builder()
            .directionsRoute(directionsRoute)
            .shouldSimulateRoute(true)
            .directionsProfile(DirectionsCriteria.PROFILE_DRIVING)
            .navigationOptions(getMapboxNavigationOptions()) // getMapboxNavigationOptions
            .build()
    }

    private fun getMapboxNavigationOptions(): MapboxNavigationOptions? {
        return MapboxNavigationOptions.builder()
            .snapToRoute(true)
            .enableOffRouteDetection(true)
            .maximumDistanceOffRoute(40.0)
            .roundingIncrement(NavigationConstants.ROUNDING_INCREMENT_ONE)
            .defaultMilestonesEnabled(false)
            .enableBannerNotification(true) //required as true for API level 21 and above
            .build()
    }

    /**
     *  This is a hard coded Route API response from the asset folder
     *
     * @return String of Route API Response
     */
    private fun getMockData(): String {
        return loadJSONFromAsset()
    }


    /**
     * This method is used to read the hard coded response from the asset folder
     *
     * @return String of JSON response
     */
    private fun loadJSONFromAsset(): String {
        val json: String = try {
            val `is`: InputStream = assets!!.open("MOCK_RESPONSE_BANGALORE_waypoints.json")
            val size: Int = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
            return ""
        }
        return json
    }

    override fun onNavigationReady() {
    }

    override fun onNavigationInitError(navigationErrorInfo: NavigationErrorInfo?) {

    }

    override fun onUpdatedWaypoints(location: List<OlaLatLng?>?) {

    }

    /**
     * This method is called when the route progress is changed
     *
     */
    override fun onRouteProgressChange(instructionModel: InstructionModel?) {
//       instructionModel has Route Progress details
        Log.d("MainActivity", "--> onRouteProgressChange() $instructionModel")
        setTbtInfoMenu(instructionModel)
    }

    /**
     * This method is called when the route is off route, need to call Route-API again for Re-Route state and update the Navigation using updateNavigation
     *
     */
    override fun onOffRoute(location: Location?) {
        //Need to call Route-API again for Re-Route state wih current location  and destination as getting from onOffRoute
        val routeInfoData: RouteInfoData? = null //get from the response of Route-API

        routeInfoData?.let {
            // transform should be called asynchronously
            val directionsRoute = transform(routeInfoData)
            olaMapView?.updateNavigation(directionsRoute)
        }

        Log.d("MainActivity", "--> onRouteProgressChange() new current Location$location")
    }

    /**
     * This method is called when the route is completed
     */
    override fun onArrival() {
//       Reached on location
        Log.d("MainActivity", "--> onArrival() , Reached on destination")

    }

    override fun onStaleStateChange(isStale: Boolean) {
        //if true, location is looking for GPS
        Log.d("MainActivity", "--> onStaleStateChange() , Looking for GPS")
    }

    /**
     * This method is called when the map is destroyed
     *
     */
    private fun endTbTRoutes() {
        recenterMapAtLocation()
//        olaMapView?.setUserLocationComponent() // enable the current location icon
        olaMapView?.stopNavigation()
        olaMapView?.getNavigationMapRoute()?.removeRoute()
        olaMapView?.removeAllMarkers()
        olaMapView?.removeOverlayOnMap()
        olaMapView?.removeListeners()
    }

    private fun setTbtInfoMenu(instructionModel: InstructionModel?) {
        instructionModel?.let { it ->

            val distance = it.currentInstructionModel?.getDistance()
            val imageType = it.currentInstructionModel?.getImageType()
            val updatedInstructionText = it.currentInstructionModel?.getTextInstruction()

            //Note Image Type you can match with ManeuverImageType this class is available in the sample app

            showToast("distance : ${distance} ,  $imageType  $updatedInstructionText")
        }

    }


    /**
     * This method is called when the location is updated
     *
     */
    override fun onLocationUpdated(location: Location) {
        //add Log to update the current location
        Log.d("MainActivity", "onLocationUpdated() $location")
        currentLocation = location
    }




    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Handle the location permission request result
     *
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted
                checkLocationEnabled()
                initializeOlaMaps()
            }
        }
    }

    /**
     * Check if location is enabled on the device
     *
     */
    private fun checkLocationEnabled() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(enableLocationIntent)
        }
    }


    override fun onPause() {
        super.onPause()
        olaMapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        olaMapView?.onStop()
    }

    override fun onDestroy() {
        endTbTRoutes()
        olaMapView?.onDestroy()
        olaMapView?.removeCurrentLocationUpdate()
        super.onDestroy()
    }
}
