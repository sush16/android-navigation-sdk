# Draw Route from List of GPS Coordinates on Map Application

With this information, you can draw the actual route in a Mock Location app using the provided list of GPS coordinates. 

## Requirements

- List of GPS Coordinates (Latitude, Longitude)

   For example:
   ```bash
  latitude,longitude
  12.9314,77.6147489
   12.9314001,77.6147469
   12.9314002,77.6147383
   12.9313986,77.6147138
   12.9313982,77.6146757
   12.9313987,77.6146696
   12.931402,77.6146446

## Setup 
1. Open https://www.gpsvisualizer.com/convert_input,

2. Copy and Paste the provided coordinates into the edit box,
3. Select the toggle for GPX 
4. Click on the convert icon. 
5. Download the GPX file and add it to any Mock Location App. 
6. After conversion, you can also verify the route on Google Maps by clicking on "Google Map" in the "Map this data" section.


## Note:
- The first line should specify the format for the data, as shown in the example ("latitude,longitude").
- Below the format line, provide all the latitude and longitude pairs accordingly.