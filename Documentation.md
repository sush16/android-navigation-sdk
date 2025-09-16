# Ola-Maps-Navigation SDK Documentation

## Introduction

The Ola-Maps-Navigation SDK provides developers with features related to maps, route preview and turn-by-turn navigation for Android applications.

## Purpose

The primary purpose of the Ola-Maps-Navigation SDK is to enable developers to integrate mapping, route preview and turn-by-turn navigation functionalities into their Android applications seamlessly.

## Pre-Requirement Setup

### 1. Version Required:
- **minSdkVersion**: 21
- **Java sourceCompatibility**: JavaVersion.VERSION_11

### 2. GPS Permission:
- Ensure that your application has the necessary permissions to access the device's GPS.
- GPS permission is required to load maps effectively.


## Setup

1. **Download SDK**: Obtain the `.aar` SDK file from the provided link.
2. **Integration**:
   - Place the SDK file in your project's `libs` directory.
   - Implement the SDK into your app-level Gradle file.

## Code Changes

1. **XML Integration**:
   - Add `OlaMapView` to your XML layout file.
   - We integrate the OlaMapView widget into the XML layout of your activity.
    ```bash
   <!-- Step 1: Integrate OlaMapView Widget -->
   <androidx.constraintlayout.widget.ConstraintLayout
   android:layout_width="match_parent"
   android:layout_height="match_parent">

   <!-- Your existing layout code -->

   <!-- Add OlaMapView -->
        <com.ola.maps.navigation.v5.navigation.OlaMapView
        android:id="@+id/olaMapView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
   
    </androidx.constraintlayout.widget.ConstraintLayout>


2. **Java Integration**:
   - Obtain an instance of `OlaMapView` using binding or `findViewById`.
   - Implement `MapStatusCallback` listener in your Activity.

3. **Initialization**:
   - Initialize the OlaMaps SDK using your custom configuration via the `initialize()` method of `OlaMapView`.

4. **Map Ready**:
   - Upon receiving the `OnMapReady` callback, the Ola-Map is loaded, and further functionalities can be utilized.

In the Kotlin code:
- We implement the MapStatusCallback interface in your activity.
- Override the onMapReady() method to define actions to be performed when the map is ready.

    ```bash
  // Step 3: Implement MapStatusCallback Interface
  class MainActivity : AppCompatActivity(), MapStatusCallback {
  
  var isMapReady = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        //Your existing code 

        //call onCreate funtion of OlaMapView after layout initialize
        olaMapView?.onCreate(savedInstanceState)
    
      //call initialize funtion of OlaMapView with custom configuration
        olaMapView?.initialize(
            mapStatusCallback = this,
            olaMapsConfig = OlaMapsConfig.Builder()
                .setApplicationContext(applicationContext) //pass the application context here, it is mandatory
                .setMapBaseUrl("<Base URL of Ola-Maps>") // pass the Base URL of Ola-Maps here (Stage/Prod URL), it is mandatory
                .setInterceptor(AccessTokenInterceptor()) // Instance of okhttp3.Interceptor for with Bearer access token, it is mandatory
                .setProjectId(<Orgination ID>) //pass the Orgination ID here, it is mandatory
                .setMapTileStyle(MapTileStyle.<Your Prefered Style>) //pass the MapTileStyle here, it is Optional.
                .setUniqueId(<Unique User/Device Id>) //pass the Unique User/Device ID here, it is Optional.
                .setMinZoomLevel(3.0)
                .setMaxZoomLevel(21.0)
                .setZoomLevel(14.0)
                .build()
        )
    }
   
   // Override onMapReady method
    override fun onMapReady() {
      isMapReady = true
      // Implement your Ola-Maps logic here when the map is ready
      }
   }
    //call onStop() of SDK before removing the OlaMaps
    override fun onStop() {
        super.onStop()
        olaMapView?.onStop()
    }

    //call onDestroy() of SDK to remove the Maps
    override fun onDestroy() {
        olaMapView?.onDestroy()
        super.onDestroy()
    }



## Route Preview

Route preview offers users a comprehensive overview of multiple routes between destinations, presenting ETA, distance, and potential traffic conditions. This allows users to compare and select the most optimal route tailored to their preferences, ensuring a smooth and efficient journey.


1. **API Call**:
   - Call the route API to obtain route information.
2. **Transformation**:
   - Transform the API response route data into the SDK's `DirectionsRoute` class asynchronously.
3. **NavigationMapRoute**:
   - Create an instance of `NavigationMapRoute` using `getNavigationMapRoute()` method.
4. **Route Display**:
   - Use `addRoutesForRoutePreview()` method of `NavigationMapRoute` to display the routes.
5. **Route Removal**:
   - To remove route preview, call `removeRoute()` method.
6. **Marker Removal**:
   - Remove all markers using `removeAllMarkers()` method.

### Code Snippet Show Route Preview
- Transform Route Data, Create an instance of NavigationMapRoute, and addRoutesForRoutePreview() method to display the routes.
    ```bash
   import com.ola.maps.navigation.v5.navigation.direction.transform
  
  // initialize this variable after onMapReady callback
   val navigationRoute = isMapReady ?: olaMapView?.getNavigationMapRoute() : null 
  
     val directionsRouteList = arrayListOf<DirectionsRoute>()
      //For asynchronous call we can plan for coroutines or any other async call
      lifecycleScope.launch(Dispatchers.IO) {
            directionsRouteList.add(transform(routeInfoData))
      }
  
    //Once we get the transformed response from SDK, we can call addRoutesForRoutePreview method to show the routes
     navigationRoute?.addRoutesForRoutePreview(directionsRouteList)
    
- Remove Route Preview and All Markers
    ```bash
    // Remove Route Preview
    navigationRoute?.removeRoute()
    
    // Remove All Markers
    olaMapView?.removeAllMarkers()


## Turn-By-Turn
Turn-By-Turn navigation provides users with step-by-step guidance throughout their journey, offering clear and concise directions at each intersection or maneuver point. This feature ensures users stay on track by indicating upcoming turns, lane changes, and points of interest along the way, enhancing navigation accuracy and convenience for a seamless travel experience.

1. **Initialization**:
   - Ensure that the route API response is transformed into `DirectionsRoute` data.
2. **Listener Implementation**:
   - Implement `NavigationStatusCallback` and `RouteProgressListener`.
3. **Listener Registration**:
   - Register listeners using `addRouteProgressListener()` and `registerNavigationStatusCallback()` methods.
4. **NavigationViewOptions**:
   - Create an instance of `NavigationViewOptions` using `getNavigationViewOptions(directionsRoute)` method.
5. **Start Navigation**:
   - Initiate turn-by-turn navigation using `startNavigation(NavigationViewOptions)` method.
6. **Route Removal**:
   - To remove routes, call `removeRoute()` method.
7. **Listener Removal**:
   - Remove listeners using `removeListeners()` method.
8. **Marker Removal**:
   - Remove all markers using `removeAllMarkers()` method.

### Code Snippet for Turn-By-Turn

- Transform Route Data, Create an instance of NavigationMapRoute, and addRoutesForRoutePreview() method to display the routes.

   ```kotlin
   import com.ola.maps.navigation.v5.navigation.direction.transform

   // initialize this variable after onMapReady callback
   val navigationRoute = isMapReady ?: olaMapView?.getNavigationMapRoute() : null 

   var directionsRoute : DirectionsRoute = null
   //For asynchronous call we can plan for coroutines or any other async call
      lifecycleScope.launch(Dispatchers.IO) {
      directionsRoute = transform(routeInfoData)
   }

   //Once we get the transformed response from SDK, we can get the NavigationViewOptions instance
   val navigationViewOptions = getNavigationViewOptions(directionsRoute)
   //register the listener and start the Navigation for Turn-by-Turn Mode
   if (navigationViewOptions != null) {
       olaMapView?.addRouteProgressListener(this@MainActivity)
       olaMapView?.registerNavigationStatusCallback(this@MainActivity)
       olaMapView?.startNavigation(navigationViewOptions)
   }

    
- Remove Turn-By-Turn, Route and All Markers
    ```bash
    // Remove Route Preview
    navigationRoute?.removeRoute()

    // Remove Turn-By-Turn Listeners
    olaMapView?.removeListeners()
    
    // Remove All Markers
    olaMapView?.removeAllMarkers()


### Code Snippet Turn-By-Turn Instructions
- Implement RouteProgressListener to get the Turn-By-Turn instructions.
    ```kotlin
    import com.ola.maps.navigation.v5.navigation.listeners.NavigationStatusCallback
    import com.ola.maps.navigation.v5.navigation.listeners.RouteProgressListener

    override fun onRouteProgressChange(instructionModel: InstructionModel?) {
        instructionModel?.let { it ->
            val distance = it.currentInstructionModel.getDistance()
            val imageType = it.currentInstructionModel.getImageType()
            val updatedInstructionText = it.currentInstructionModel.getTextInstruction()
        }
    }

    override fun onOffRoute(location: Location?) {
  //Need to call Route-API again for Re-Route state wih current location  and destination as getting from onOffRoute      
         val routeInfoData: RouteInfoData = get from the response of Route-API
        
        val directionsRoute = transform(routeInfoData) // transform should be called asynchronously
        olaMapView?.updateNavigation(directionsRoute)
    }

    override fun onArrival() {
        // Show UI of once User is reached at destination
    }


## Dependencies

Include these dependencies in your project's build.gradle file. These dependencies are required for the Ola-MapsSdk as well as used in the sample app. Adjust the versions as necessary for your project.
    
    ```gradle
    // Required for Ola-MapsSdk
    implementation "com.moengage:moe-android-sdk:12.6.01"
    implementation "org.maplibre.gl:android-sdk:10.2.0"
    implementation "org.maplibre.gl:android-sdk-directions-models:5.9.0"
    implementation "org.maplibre.gl:android-sdk-services:5.9.0"
    implementation "org.maplibre.gl:android-sdk-turf:5.9.0"
    implementation "org.maplibre.gl:android-plugin-markerview-v9:1.0.0"
    implementation "org.maplibre.gl:android-plugin-annotation-v9:1.0.0"

    // Used in sample app
    implementation "androidx.lifecycle:lifecycle-extensions:2.0.0"
    implementation "androidx.lifecycle:lifecycle-compiler:2.0.0"
    implementation "androidx.core:core-ktx:1.10.1"
    implementation "androidx.appcompat:appcompat:1.5.1"
    implementation "com.google.android.material:material:1.7.0"
    implementation "androidx.constraintlayout:constraintlayout:2.1.4"


    