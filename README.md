# jvast
A Java library for video ad processing, e.g. insert pixels, etc.

## Overview
This is a Java library that supports VAST video ad processing, currently supporting pixel insertion. We are using a purely string manipulation based on index to achieve a better performance than typical DOM or SAX approaches.

## Usage
```java
//get VAST ad, e.g. from an HTTP response, a file, etc.
String ad = ...;

//construct input data for ad processing, e.g. insert pixels and tracking events
Multimap<PixelElementType, String> pixelMap = ArrayListMultimap.create();
final String impressionPixel = "https://adclick.com/impression";
pixelMap.put(PixelElementType.Impression, impressionPixel);

Multimap<TrackingEventElementType, String> trackingEventMap = ArrayListMultimap.create();
final String startTrackingEvent = "https://adclick.com/start";
trackingEventMap.put(TrackingEventElementType.start, startTrackingEvent);

InputData inputData = InputData.builder()
    .pixelMap(pixelMap)
    .trackingEventMap(trackingEventMap)
    .build();

//process ad
videoAd = VideoAdProcessor.process(videoAd, inputData);
```

You can clone this repo and build the jar to use. Or you can use the library from Maven Central project: https://mvnrepository.com/artifact/io.github.guozheng/jvast

## Build
```shell
./gradles fatJar
```
The jar file can be located in `build/libs/jvast.jar`.

## Testing
The testing logic has two parts:
   1. the updated VAST XML contains the right updates, e.g. pixels are inserted
   2. the updated VAST XML is a valid XML against IAB XML schema, based on VAST version, etc.

We are using a VAST parser (in `src/test/java/jvast/vastparser`) to implement the tests. It supports XML validation against schema and getting XML elements like pixels and tracking events. You can find sample tests in `VideoAdProcessorTest.java`.

To run the tests:
```shell
./gradlew test
```

## Publishing to Maven Central
```shell
./gradlew publishToSonatype closeSonatypeStagingRepository
```
For more information on how to publish to Maven Central, check out this [Medium article](https://medium.com/@guozheng-ge/how-to-publish-a-library-on-maven-central-88889ba9ff41).

## Todo Tasks
   * Support VAST 4.x
   * Support VMAP