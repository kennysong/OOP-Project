## GTFS Tutorial

This GTFS data is separated into several files that describes the schedule and routes for the entire BART train system.

Intuitively, any mass transit system will have its **stops** scattered around a city region. These stops will be connected by a few specific **routes**. These routes are generally static and will have trains travelling back and forth on each route many times a day. Each time a train travels up/down a specific (possibly part of a) route, this entire journey is called a **trip**.

Getting into more detail, an entire trip is comprised of a series of sub-trips between stops. Generally the **trip stop times** will be set to a specific schedule, so each trip is scheduled to run at a specific time each day, and it takes a fixed, predetermined span of time to arrive at every stop along the way. Additionally, a specific route may have different trips depending on the day (weekend, weekday, or holidays). A group of scheduled trips that changes depending on the day is called a **service** for that day.

(Note: The trip ID uniquely identifies the trip path and schedule for a day, so the service ID is only used for clearly identifying the actual day that a trip ID runs on. In the BART data, they change the trip ID to have the weekday appended when the service changes, i.e. *01SFO10* and *01SFO10SUN*.)

All this data is encoded in the CSV files by the GTFS specification. The documentation for the format [is here](https://developers.google.com/transit/gtfs/reference), but for quick reference:

#### stops.txt

This defines a list of all the stop in the BART system, their names, and their lat-lon location.

#### routes.txt

This defines a list of routes along these stops. 

It does not include any other information about a route (trips, stops, endpoints, schedule, etc).

#### trips.txt

This defines a list of trips for each route. The service ID is also listed for the trip. 

It does not include any other information about a trip (stops, endpoints, schedule, etc).

#### stop_times.txt

This defines a list of stops for each trip, along with their scheduled times. 

#### calendar.txt

This defines a list of service IDs (for trips) based on the days (Mon-Sun) in which the service is active.

