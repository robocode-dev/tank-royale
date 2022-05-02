# How to contribute

Thank you for taking the time to read this guide for how to contribute to Robocode Tank Royale.

### Motivation behind Robocode

Robocode is a spare time and non-profit project with the following drivers:

- First and foremost, Robocode is about providing a [programming game] that aims to let its users to having fun while
  developing both their programming and AI skills. Robocode can be quite entertaining.
- Robocode is popular in educational institutions. And Robocode is used as a platform to learn about programming. But
  also to have a clear goal of what to code, and an obvious way of benchmarking performance.
- Competitions like e.g. [LiteRumble] (for the [original Robocode game](https://robocode.sourceforge.io/)) is like an
  e-sport for programming games. This is a good way of showing off your skills as a developer.
- Developing Robocode itself is also about having a good time when developing it, and learning new skills as well.

### Things to keep in mind

Big changes to the existing game rules and physics is a no-go.

Here are some of the reasons:

- The rules and physics should not change. If you want new rules or physics, it needs to be forked and given a new name.
- It breaks backward compatibility with older bots making it hard to include it in battles with newer versions of
  Robocode.
- Big changes to the game are plain annoying to people that have spent a long time getting to know all the tiny details
  about Robocode, and knowing all the tips and tricks to create great bots, e.g. used for gaining an edge over other
  bots in competitions.
- It is hard to cope with big changes in the documentation that needs to be changed to explain both new and old behavior
  that must co-exist, which is confusing to people.

But this does not mean that Robocode should not change. It simply means that breaking-changes must be avoided. And the
focus should stay on the goal and expectations from the majority of users of Robocode.

Big additions to the game might occur as long as they are not breaking changes.

## Roadmap

If you want to see what is/are the next step(s) for Robocode, have a look at the [roadmap].

Please note that this roadmap does not contain a timeline with an expected ETA. Robocode is considered a spare time
project, not a profession.

## How to contribute?

- **Support**:
    - If someone has a question, where you have the answer, don't hesitate with answering the question.
    - Be polite when answering questions and providing feedback. :)
    - Please take part in discussions, e.g. give feedback on discussions, bug reports, feature requests, etc.
- **Documentation**:
    - All documentation for Robocode is available in Markdown syntax within the git repository. If you find spelling
      mistakes, bad formulations, missing content, etc., you are very welcome to contribute with changes to the
      documentation by a [pull request].
    - The used language is for documentation and communication is kept to American English, as the original Robocode
      originated from the US.
- **Bug reports**:
    - Make sure to report a bug if you run into something that is broken or does not work as expected.
    - If you reported a bug and have a good understanding of why it occurs, you might as well try to solve it and create
      a [pull request] for it.
    - If you see a bug you think is easy to fix, or is just curious to try out your programming skills, feel free to
      create a [pull request] containing a fix.
- **Adding a feature request**:
    - If you have a good idea for Robocode, please start up a discussion or feature request that describes your idea in
      detail. Keep in mind that people might have a strong opinion about a feature, e.g. if it should be accepted or
      rejected, or how it should be implemented. It might also be a good idea to consult someone else before creating a
      feature request.
    - Note that a feature request should be considered as a "wish" for a feature, not a requirement or "must-do"
      feature. If it is a good idea, it should be implemented eventually. If it is a bad idea, it will properly never
      get implemented, or rejected.
    - Don't expect an ETA for when a feature is implemented, when, or if it gets accepted. If you want the feature to be
      implemented sometime soon, you are welcome to make a [pull request] by yourself and/or get assistance from other
      developers.
- **Implementing a feature request**:
    - If you want to implement a feature request, you need to make sure it is accepted as a feature that should go into
      Robocode. If in any doubt, contact the [maintainer] of Robocode.
    - Note that documentation needs to be added and/or updated for the new feature and that it _should_ be tested as
      well - preferable by unit tests. But sometimes it is only possible to do manual testing.
    - Changes to a [Bot API] are non-trivial as _all_ available Bot APIs provided for the JVM, .Net, web, etc. needs to
      be updated at the same time. Not just a single platform and language.

## Regarding pull requests

### Must be stable

When doing a pull request, make sure it is always in a stable state meaning that it can compile, and that it is also
possible to run the code to try it out to review it. And it should not break existing features.

### Documentation

Make sure to provide a sufficient description for the pull request. For example, a link to the bug or feature this pull
request addresses. But also how to try out the new feature. Also, make sure to put relevant comments on code, where some
choices were made that might not be obvious, and put a `// TODO:` or `// FIXME:` if something still needs to be done and
not forgotten about.

### Prefer small pull requests

Avoid "big bang" pull requests with too many changes made at the same time. It must be easy to figure out what the pull
request is all about. It should not be a struggle to review and approve the pull request.

Keep things separate, and don't mix up changes like e.g. fixing a bug, and implementing two features in the same pull
request. Instead, make a separate pull request for the bug fix, and two separate pull requests, one for each feature.

If the changes are _BIG_ you _should hesitate_ with implementing them. Make sure to coordinate with the [maintainer] to
figure out a good way to do it as other big changes might be worked on in parallel complicating things.

## Regarding Bot APIs

### Similar to the original Robocode API

Currently, two official [Bot API]s have been provided for Robocode Tank Royale. These APIs are based on and inspired by
the API from the [original game]. The idea is to make it easy to shift between using the API from the original game and
the new version. But the intention is not to have a 100% compatible API, which is not possible, as the games are
similar, but not directly compatible.

### APIs must be identical between platforms and languages

The goal with the current (official) Bot APIs and future APIs for other platforms and programming languages is to keep
all the APIs as similar as possible. That is providing the same features 1-to-1 in identical ways. But the APIs must be
designed in a way that is loyal to the conventions used for the platform and/or programming language.

### Alternative Bot APIs?

There are many ways to design an API, and the same is the case for the Bot API for Robocode. If you want the API to be
designed in another way, e.g. with different methods and behavior, you are welcome to create your own Bot API. But
please create it on a separate repository. And you are welcome to put links to it from this site as an alternative Bot
API.

### Can I write my own booter and/or GUI for Robocode?

You are welcome to provide an alternative booter and/or GUI for Robocode. E.g. if you want it to work on other platforms
or want to create an alternative GUI, e.g. for web or similar. But you should put the alternative versions on a separate
repository, and link to it from this site.

### Alternative server is a no-go, but OK to build on top of it

Regarding the server. The server itself is the heart of the game and small glitches in the behavior compared to the
official one can have a big impact on the game. Hence, it is a no-go for now.

But it is okay to build another server (in a separate repository) that builds on top of the Robocode server. That is a
server that works as a "controller" selecting the bots that should battle each other, analyze scores, and store results,
make ranking, etc. As long the alternative server it builds on top of the official Robocode server.

## Tools used for building all modules of Robocode

[Here](buildDocs/docs/dev/tools.md) you can find a list of all the tools required for building all parts of Robocode.

[programming game]: https://www.makeuseof.com/tag/best-programming-games/

[LiteRumble]: https://literumble.appspot.com/

[pull request]: https://github.com/robocode-dev/tank-royale/pulls

[Bot API]: https://robocode-dev.github.io/tank-royale/api/apis.html

[maintainer]: https://github.com/flemming-n-larsen "Mr. Robocode"

[roadmap]: https://github.com/robocode-dev/tank-royale/wiki/Roadmap

[original game]: https://robocode.sourceforge.io/
