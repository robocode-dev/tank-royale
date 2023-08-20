# History of Robocode

## Origin

The Robocode game was originally started by Mathew A. Nelson as a personal endeavor in late 2000. Mathew got inspired
from the game named [Robot Battle](https://en.wikipedia.org/wiki/Robot_Battle) and wanted to create a similar game using
the Java programming language for the API.

## IBM AlphaWork

Robocode became a professional game when he brought it to IBM, in the form of an AlphaWorks download in July 2001. IBM
AlphaWork promoted Robocode, as well as the Java programming language, with a great series of articles named
"Rock 'em, sock 'em Robocode!" and "Secrets from the Robocode masters" which made Robocode popular.

## Robocode was Open Sourced

After a couple of years, the development of Robocode stopped at IBM. Luckily, Mathew Nelson managed to convince IBM to
make the source code available as Open Source. At the beginning of 2005, Robocode was brought to SourceForge as Open
Source with Robocode version 1.0.7. At this point, the development of Robocode had somewhat stopped.

## Community-driven

People from the community of Robocode, mainly driven by the [RoboWiki](https://robowiki.net/), began to develop their
own versions of Robocode to get rid of various bugs and put new features into Robocode, the Contributions for Open
Source Robocode and later on the RobocodeNG project by Flemming N. Larsen.

Flemming continued the Open Source project and tool over from Mathew in July 2006. The Robocode forks, named RobocodeNG
and Robocode 2006, contained a lot of contributions from the Robocode community and were merged into the official
Robocode with version 1.1. Since then, a lot of new versions of Robocode have been released with more and more features
and contributions from the community.

## Robocode Tank Royale

The original Robocode runs entirely on Java, where robots musts be made for the Java platform. At some point, a .Net
plugin was available for supporting .Net bots. However, this plugin was hard to maintain, and used a .Net bridge to
convert robot commands written in .Net into code executed in Java.

In addition, a Java security manager is needed as bots runs in the same process inside the Java virtual machine, but
should
not be allowed to disturb each other. The Java security manager has been marked as deprecated for removal
with [JEP-411](https://openjdk.org/jeps/411).

Robocode Tank Royale is a new platform for Robocode that rewritten from fixes some of the flaws with the original game.
Read more about it [here](../articles/tank-royale.md)


[JEP-411]: https://openjdk.org/jeps/411 "JEP 411: Deprecate the Security Manager for Remova"