# Weather App

Write an HTTP server that serves the current weather. Your server should expose an endpoint that:

1. Accepts latitude and longitude coordinates
2. Returns the short forecast for that area for Today (“Partly Cloudy” etc.)
3. Returns a characterization of whether the temperature is “hot”, “cold”, or “moderate” (use your discretion on mapping temperatures to each type)
4. Use the National Weather Service [API](https://www.weather.gov/documentation/services-web-api) Web Service as a data source.

---

### Instructions

```bash
cd <wherever-you-cloned-this-repo>
sbt 'project app' run
```

Hit localhost:8080 to load the home page that has the extensive development notes for this exercise.
