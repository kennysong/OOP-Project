This GTFS data is separated into several files that describes the schedule and routes for the entire BART train system.

Basically, any mass transit system will have its **stops** scattered around a city region. These stops will be connected by a few specific **routes**. These routes are generally static and will have trains travelling back and forth on each route many times a day. Each time a train travels on a specific (potentially sub-)route, this entire journey is called a **trip**.

Getting into more detail, an entire trip is comprised of a series of sub-trips between stops. Generally the **trip stop times** will be specified to run on a specific schedule, so each trip is schedule to run at a specific time daily, and it takes a fixed, predetermined amount of time to arrive at every stop along the way. Additionally, a specific route may have different trips depending on the day (weekend or weekday or holidays). A scheduled trip on a specific day is called a **service** on that day.

(Note: For the BART GTFS data, the trip ID uniquely identifies a service, so the service ID is only used for identifying the day that a trip ID runs on. They change the trip ID to have the weekday appended when the service changes.)

All this data is encoded in the CSV files in the GTFS format. The documentation for the format is here, but for quick reference:

#### stops.txt

This defines a list of all the stop in the BART system, their names, and their lat-lon location.

#### routes.txt

This defines a list of routes along these stops. 

It does not include any other information about a route (trips, stops, endpoints, schedule, etc).

#### trips.txt

This defines a list of trips for each route. The service ID is also listed but is extraneous. 

It does not include any other information about a trip (stops, endpoints, schedule, etc).

#### stop_times.txt

This defines a list of stops for each trip, along with their scheduled times. 

#### calendar.txt

This defines a list of services (for trips) based on the days (Mon-Sun) in which the service is active.

