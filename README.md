# Assignment - TrackingApp
Sample App that tracks a user's hike

Assignment overview
The main requirements could be extracted easily from the assignment description, and they can be split into functional and non-functional requirements as listed below.

Functional requirements:
Start / Stop tracking
Track location while the app is in background or foreground
Every 100m search for a public image on Flickr from around the user’s location
Display the images in a scrolling list on the main app screen

Non-functinal requirements:
The user is able to return to the app if the tracking is in progress
The app keeps tracking even when the user locks the phone/sends the app to the background
The app should continue tracking if the device loses internet connectivity. All the images will be loaded once the connectivity is regained.
The app will retrieve the images in “real time” (even if the user is not actively checking the app) 

Considering the app is aimed towards being used outdoors where internet connectivity might be limited or unavailable, I’ve decided to implement the assignment in a way that will try to fetch the images as soon as possible (rather than waiting for the user to interact with the app).

The overall image fetching strategy is as follows.

The app listens to new location updates from the FusedLocationClient (configured with a displacement of 100m) and immediately tries to search for an image at that location (based on latitude and longitude). If an image is found on Flickr, the app will attempt to immediately download the image file and store it in the local cache folder. If an image file is successfully downloaded, a new entry in the DataStore containing the local path of the image will be created; If the request or image download fails, then the datastore entry will only contain the latitude and longitude.

Whenever the app is brought to the foreground and there are image files missing, the app will try to download the missing images. The same happens whenever the devices regains internet connectivity - the app will try to download any missing images and update the user’s view.

For the sake of keeping the assignment solution as simple as possible deliberately a trivial strategy for offline availability, but I believe it showcases a starting point for a more complex approach. 
Scalability
Downloading images can be costly both for bandwidth and local storage. In a real world scenario special consideration would have to be taken when downloading images (perhaps only download very small thumbnails ahead of time and then fetch the full/larger size image once the user taps to expand the image.


For the assignment scope the app is only able to track a single activity. Whenever a new activity tracking session is started, the data from the previous session will be deleted. Keeping this scope in mind the app is downloading images of ~50KB.
As an example: a tracking session of 2 hours, for a user walking at an average speed of 5 KM/H would essentially use around 5MB of local storage (assuming an image is downloaded every 100m).

Architecture overview
The app is using a very minimal version of MVVM with a single Activity, ViewModel and Repository.

For location tracking the app uses a foreground service that interacts with the FusedLocationClient to receive location updates, which it then proceeds to process and store them into a DataStore.

For the networking layer the app is using the Retrofit library.
The UI of the app is built using Jetpack Compose. 

Final note
Considering this is a sample app rather than a production ready app, there are some things that I’ve omitted such as (definately not an exhaustive list): 
Setting up different build variants/flavours
Configuring proguard rules
Setting up ktlint
CI/CD pipelines
UI tests
UI components 



