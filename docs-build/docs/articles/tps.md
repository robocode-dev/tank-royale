# TPS (Turns Per Second)

## Introduction

_Turns Per Second_ is a term used in Robocode that can be compared to [FPS] (Frames Per Second). So TPS is the _turn
rate_ defined as the number of turns in a battle that the server can execute per second.

## Turn timeout

The game has a _turn timeout_ defined as a part of the game rules. The turn timeout is specified in microseconds (µs),
where a microsecond (µs) is a millionth of a second. The turn timeout is the amount of time that each bot is allowed to
use for a single turn.

### Skipped turns

If the bot spends more time than allowed by the turn timeout, then the bot will be _skipping the turn_, and have missed
the opportunity to send its _intent_ to the server. This means that the bot cannot fire the cannot, turn or change the
speed, etc. in that turn. This happens because the server receives a _turn timeout_ event, and hence concludes the turn,
and starts processing the next turn.

### Intents sent within the timeout time

A well-functioning bot will make sure to send its _intent_ before the turn timeout occurs. So as soon as all bots have
sent their _intent_ in time, the server can conclude the turn. This will happen when the server receives the intent from
the last and "slowest" bot sending the last intent. But this will still be faster than the turnout time.

## Turn rate is limited by the turn timeout

So why is the turn timeout influencing the turn rate?

This is because the turn rate is constrained by how fast the turns are being processed. The faster the turns are
processed, the higher the turn rate can go. So if a high turn rate is wanted, this will require a turn timeout that is
low enough to allow it.

## Maximum turn rate

The maximum turn rate can be calculated from the turn timeout:

$TPS_{max} >= \frac{1,000,000}{timeout_{turn}}$

So if the turn timeout is set to 30,000 µs, the maximum turn rate will be:

$TPS_{max} >= \frac{1,000,000}{30,000} = 33.33333$

The maximum turn rate, TPS<sub>max</sub> might be slightly higher due to the bots, all sending their intents before the
timeout occurs.

## Note about maximum turn rate

Be careful when setting the maximum turn rate! Setting the turn rate below 1 ms (1000 µs) is currently causing issues on
some systems, meaning that the bots might not behave the same way as usual and seem to skip turns etc.
This can be caused by both hardware limitations and OS limitations.


[FPS]: https://en.wikipedia.org/wiki/Frame_rate "Frame Rate and frames per second (FPS)"
