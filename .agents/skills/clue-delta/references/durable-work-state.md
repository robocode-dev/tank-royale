## Durable work state

An agent's private memory is never where work lives. Anything needed to implement, continue, review, or hand off work belongs in a corpus artifact, the change workspace, or the pull request; private conversation does not survive a change of agent.

A suggestion raised mid-change is triaged immediately, into one of two carriers, never into memory. If the change is wrong or incomplete without it, it becomes a task in `tasks.md`, handled before merge. Otherwise it becomes a goal with `status: proposed`, written in the digest so it survives the workspace's deletion. Neither carrier is optional, and "I will remember" is not a third: a suggestion that is neither actioned nor recorded has been declined without anyone deciding to decline it. State which carrier a suggestion went to, and why, when the triage happens.

A durable record never states a figure a command computes — an artifact count, a coverage percentage, a reported population size. Name the command instead. A number written into prose becomes a hand-maintained obligation that goes stale on the next change and that every later reviewer re-derives, and repairing one writes new prose carrying new numbers, so the finding regenerates instead of converging. Measurements that are the point of a record — an analysis's own results, a milestone's observed evidence — are stated with what produced them and when.

After a human reports a Cliewen change's merge, orient before starting anything else: describe the plan's next unfinished step in plain language and ask whether to start it, or say that the plan has nothing left and ask what comes next.
