# forecasts

## Installation

Download from https://github.com/ougfh/forecasts.

## How to run

Build an uberjar:

    $ clojure -A:uberjar

Run that uberjar with darksky API key:

    $ DARKSKY_API_KEY=api_key java -jar forecasts.jar

with passed darksky API key as enviroment variable.

## Options

Options that can be passed:

- `--city` city name
- `--lat` latitude, default 60.59329987
- `--lng` longitude, default -1.44250533

## Test

Run the project's tests:

    $ clojure -A:test:runner

## Examples

To see weaher reports from Moscow by city name, run:

    $ DARKSKY_API_KEY=api_key java -jar forecasts.jar --city Moscow

or by coordinates:

    $ DARKSKY_API_KEY=api_key java -jar forecasts.jar --lat 55.751244 --lng 37.618423


## License

Copyright © 2020 Rodionovaanastasia

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
