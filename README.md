# Unofficial JetBrains Space Java SDK

## Disclaimer

This project is in the earliest stage of its life. Constant
changes to be expected, no backward compatibility guarantees etc.

This project is also unofficial, meaning it's not created nor endorsed
by the JetBrains Space team.

## Overview

This library facilitates working with HTTP API of [JetBrains Space](https://www.jetbrains.com/space/).

### Features

- idiomatic parameter specification
- simplified interface for specifying output fields
- implicit batch queries and parameter list chunking
- built-in OAuth authentication

## Example

To begin, you need to know the domain name of your Space,
e.g. `jetbrains.team`. You also need a service (in particular,
the service ID and the service secret).

```java
SpaceService service = new SpaceService("space.domain.name", "service_id", "service_secret");
List<TD_MemberProfile> profiles = service.getProfiles().addParameter("location", "location_id").execute();
```
This will automatically obtain OAuth token and send several batch queries to retrieve all the profiles
for the given location.

## Tutorial

### How often did each employee work from home in 2020?

We want a map that stores for each employee the number of days logged as "Offsite". 
To answer that question, we'll need a service that has access to absences and can view absence reasons.

```java
// initialize the service instance
SpaceService service = new SpaceService("space.domain.name", "service_id", "service_secret");
// create a request to get absences
var request = service.getAbsences("All");
// add the date range parameters
request.addParameter("since", LocalDate.of(2020, 1, 1));
request.addParameter("till", LocalDate.of(2020, 12, 31));
// execute the request
List<AbsenceRecord> absences = request.execute();

Map<String, Integer> daysWorkedFromHome = new HashMap<>();

for (AbsenceRecord absence : absences) {
    if (absence.reason.name.equals("Offsite")) {
        String employeeName = absence.member.name.lastName + " " + absence.member.name.firstName;
        // until presumes that the end date is exclusive, hence + 1
        int days = (int) (absence.since.until(absence.till, ChronoUnit.DAYS) + 1);
        daysWorkedFromHome.compute(employeeName, (key, value) -> {
            if (value == null) {
                return days;
            } else {
                return value + days;
            }
        });
    }
}
```

If we try to launch this code, however, it will most likely fail with a `NullPointerException`.
The culprit here is the parsimonious Space API approach to returning as little information
as possible. In particular, `absence.member` and `absence.reason` will only have `id` fields.
We have to ask for the necessary fields explicitly, so we have to add the following lines before
`execute()`-ing:

```java
request.addField("member", "name");
request.addField("reason", "name");
```

Now it should work!

This code does have a few issues more (`execute()` can throw an uncaught exception,
we ignore the employees that never worked from home in 2020, we don't deal the employees
with the same name etc., we don't take into account the absences that start before or end after 2020),
but fixing these is left as an exercise to the reader.
