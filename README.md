# MapsCacheAndroid
This is an application to load a Local website which utilizes Google Maps javascript API
to show a mapview stored in the assets file over and android webview

The flow is as follows:

1. The webview loads the asset file and intercepts the requests
2. It calculates the hashcode of the URL and checks if a cache file exists with that name
3. If it doesn't exist then Null is returned causing the webview to proceed with default communication. Parallely, an ExecutorService fetches the stream of data from the URL and stores it in a file. The request headers of Content-Type and Cache max-age are stored in a separate file with .ds extension to the existing cache
4. In the subsequent requests (Example, when offline), if cache file exists then it reads the cache file as well as the headers file and returns respective parameters
5. If cache file is outdated it deletes the cache file and reads from network again
6. The Cache File Response is returned as a Wrapped response with a lock mechanism so that the responses are processed synchronously (Requested)


## NOTE

1. Use with API Key. If API key is empty, then the map is displayed as for development. This is determined by server sending requests and cannot be cached. As a result, when offline, the cache is loaded for original tiles without development overlay and unable to find the cache, The map doesn't load correctly. Using with API key, caches the correct coordinated responses which are the same ones called when offline as well
2. The URL is reloaded for the first time as the Maps API makes the first request with a different token and subsequently uses a different token for all the requests henceforth. This cannot be cached correctly and hence website is reloaded again

## Resources
### Video of Caching
![https://github.com/JohnX4321/MapsCacheAndroid/raw/main/readme_res/a.mp4](https://github.com/JohnX4321/MapsCacheAndroid/raw/main/readme_res/a.mp4)

### Video of Synchronous Loading
![https://github.com/JohnX4321/MapsCacheAndroid/raw/main/readme_res/b.webm](https://github.com/JohnX4321/MapsCacheAndroid/raw/main/readme_res/b.webm)