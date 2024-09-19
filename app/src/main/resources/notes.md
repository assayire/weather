# Weather App Dev Notes

## First things first

How to clone the project repo

```bash
git clone git@github.com:assayire/weather.git

# Alternatively ...
# - if you use gh cli, run `gh repo clone assayire/weather.git`
# - Meh, download the archive: https://github.com/assayire/weather/archive/refs/heads/main.zip
```

> <small>NOTE: If you wish to read the source file (markdown) of _this_ document, it is available at `<where-you-cloned>/src/main/resources/notes.md`</small> 

## Project Structure

The project is set up as an SBT multi-module project. The server application is in the `/app` folder. If time permits, the plan is to set up Hoverfly tests as a second project.

If you run `projects` command at the SBT shell for this project, you should see the following:

```
[info] 	   app
[info] 	 * weather
```

If you are planning to run the app from the SBT shell, you should switch the project to `app`:

```bash
sbt-shell> project app

# ... which should spit this:
[info] 	 * app
[info] 	   weather
```
If I am able to add the Hoverfly tests project, you should see a third project listed:

```
[info] 	   app
[info] 	 * weather
[info]     tests
```

## How to run the app

```bash
cd <wherever-you-clone-this-repo>
sbt 'project app' run
```

If the server starts without any issues, this document should be accessible at the [root](/) url. If you are reading this document at the root url, you started up the server already 😀

To run unit tests:

```bash
sbt 'project app' test
```

More about APIs in the next section.

The application emits log messages just enough to know that the server is up and running. The logs added are primarily to demonstrate the use of `log4cats`, and to trace the requests and flow of control.

## APIs

The server exposes two APIs:

**Forecast Now**

```
GET /api/weather/now?c=<latitude>,<longitude>
```

This API gives the forecast for now; a single and current forecast datum.

For example, try this request for the coordinates [39.7456,-97.0892](/api/weather/now?c=39.7456,-97.0892)

**Forecast Today**

```
GET /api/weather?c=<latitude>,<longitude>
```

This API gives the forecast data for the current day. Typically, two.

For example, try this request for coordinates [40.730610,-73.935242](/api/weather?c=40.730610,-73.935242)

Not so important, there are a couple of other endpoints available that are not APIs, such as this [documentation](/) you are reading. 

## Dev Notes[^1]

Assumptions, trade-offs, limitations et al.

I wasn't sure how far should I go in building the app for the problem exercise. So, there might be areas that might seem over-engineered while others limited (or sloppy 😕). I have tried to demonstrate the different (_but not all_) ingredients one would throw in when building such a server application.

- I did not find a need to add any POST or PUT endpoints
- Response payload of the NWS APIs are parsed selectively to fit the problem use case.
- I have used [only](https://weather-gov.github.io/api/general-faqs#how-do-i-get-a-forecast-for-a-location-from-the-api) the geo-json, forecast and hourly forecast APIs. I have not used other APIs like the office, point, grid data etc.
- It appears NWS might not return forecast (day / hourly) data for the current day if requested some time late in the night (but still on the same date). I don't fully understand their behavior as it is not documented AFAIK. _I am going with the tradeoff that we return no data from our service if NWS does not return any data for the current day_. This also impacts the unit test for the service class (only if it is run in the late nighttime). The hard part is that the empty data scenario can be realized only when NWS is called during the late hours. 😢 So, I am guessing the tradeoff is alright for the moment. If I get to understand their API behavior better, our API can be fixed accordingly.
- The other part with empty forecast data is that we could probably cache the data for the date and location. I am not particularly thrilled with an in-memory cache since it would be thrown away when the application quits. So, it has got to be a persistence store. I have mixed feelings about using a file based store. Because managing the store is a bit of hassle. Last option is a database. I wasn't sure if I should go as far as integrating with a database for this exercise. Totally possible. But reminding myself not to go haywire with database and such. Going with trade-off of empty forecast data for now. Let me know if a persistent store is expected for the exercise, it just takes a bit more time and is not hard to hook up one to meet the needs.
- I have not used any dependency injection framework. Like I said, I took my best guess from going haywire.
- Unit test coverage is minimal enough to demonstrate the use of assertions, data generation using `Gen`, and mocks.
- Some things are nominal but does not affect program logic.
  - Grabbed the valid range of latitude and longitude values from internet search. Took the first result.
  - As already mentioned in the problem description, temperature characterization logic is totally arbitrary.
- The application is relevant for US-only (at least from NWS standpoint).

## Tech Stack

- Scala 3
- SBT (v1.9.9)
- http4s (server)
- cats: effect, log4cats
- sttp (client) (Used for calling NWS APIs)
- ScalaTest, ScalaCheck, ScalaMock (For unit testing)

## About Scala 3

At work, I use Scala 2.12/13. I am using, and still learning, Scala 3 for personal/pet/fun projects. I used Scala 3 for the Weather app exercise to have a little fun. I haven't used advanced features such as context functions et al. In any case, the version of Scala does not impact the solution.

While I personally like to see significant indentation in Scala 3 at least for smaller _function_s, in a team[^2] setting, I am flexible to adopt whatever is chosen together as a standard for the team.

## About `http4s`

At work, we have been using the Play framework for our services since the beginning. Recently, we have started experimenting pro bono with `http4s` for a couple of services. Because upgrading Play does not buy us anything but more trouble.

So, I have beginner level but hands-on experience with `http4s`. I chose `http4s` instead of Play because Play is really boring. Or I have become bored with it. Also, the set-up for Play services is verbose compared to `http4s`. In any case, choice of the web server framework / library should not impact the solution. 

[^1]: You will same or similar notes split across various places in the code base. This is one place to see all notes giving an overview of the implementation.
[^2]: I forgot to ask if your team uses Scala 3 / significant indentation.
