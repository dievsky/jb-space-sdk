# Unofficial JetBrains Space SDK library

## Disclaimer

This project is in the earliest stage of its life. Constant
changes to be expected, no backward compatibility guarantees etc.

## Overview

This library facilitates working with HTTP API of JetBrains Space.

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