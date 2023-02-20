# SGTheAPIGradle
> SkyGames API to manage players, team, and discord link.

[![API Version][jar-image]][jar-downloads]
[![Build Status][build-image]][jar-downloads]
[![Downloads File][jar-image]][jar-downloads]

SkyGames API to manage players, team, and discord link.
It is a REST API with a database.

## Installation

ALL OS:

```sh
java -jar SGTheAPI-VERSION.jar
```

## Usage example

```sh
DISCORD ROUTES:
    http://url:8686/api/v1/discord/:player/token GET
    http://url:8686/api/v1/discord/:player/token POST
    
    http://url:8686/api/v1/discord/:token/verify GET
    http://url:8686/api/v1/discord/:token/id PATCH
    
    http://url:8686/api/v1/discord/:player/id GET
    
PLAYER ROUTES:
    http://url:8686/api/v1/players GET
    http://url:8686/api/v1/players POST
    
    http://url:8686/api/v1/players/:player GET
    http://url:8686/api/v1/players/:player PATCH
    http://url:8686/api/v1/players/:player DELETE
    
    http://url:8686/api/v1/players/:player/teams GET
    http://url:8686/api/v1/players/:player/teams PATCH
    
TEAM ROUTES:
    http://url:8686/api/v1/teams GET
    http://url:8686/api/v1/teams POST
    
    http://url:8686/api/v1/teams/:team GET
    http://url:8686/api/v1/teams/:team PATCH
    http://url:8686/api/v1/teams/:team DELETE
    
    http://url:8686/api/v1/teams/:team/players GET
    
    http://url:8686/api/v1/teams/:team/point GET
    http://url:8686/api/v1/teams/:team/point PATCH
    http://url:8686/api/v1/teams/:team/point/history DELETE
````

_For more examples and usage, please refer to the [Wiki][wiki]._

## Development setup

Download the project and open it in your IDE.


## Release History

* 1.0.0
    * Add all routes
    * Link discord to player
    * Add team
    * Add player
    * Add point to team
    * Add history to point
    * Add player to team
    * Add TOKEN for secure routes
    

## Meta

FullGreen.GN – [@FullGreen.GN](https://www.instagram.com/fullgreen.gn/) – hi@skygames.fr

Distributed under the GNU GPL v3.0 license. See ``LICENSE`` for more information.

[https://github.com/yourname/github-link](https://github.com/FullGreenDev)

## Contributing

1. Fork it (<https://github.com/SkyGamesFR/SGTheAPIGradle/fork>)
2. Create your feature branch (`git checkout -b feature/fooBar`)
3. Commit your changes (`git commit -am 'Add some fooBar'`)
4. Push to the branch (`git push origin feature/fooBar`)
5. Create a new Pull Request

<!-- Markdown link & img dfn's -->
[maven-image]: https://img.shields.io/badge/build-passing-brightgreen
[build-image]: https://img.shields.io/badge/version-1.0--SNAPSHOT-blue
[jar-image]: https://img.shields.io/badge/download-SGTheAPI-blue
[jar-downloads]: https://repo.skygames.fr/#/snapshots/fr/skygames/sgtheapi
[wiki]: https://discord.skygames.fr