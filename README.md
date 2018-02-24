# minder

A simple tool to remind our president of his daily duties whenever his stubby
little fingers wander astray. `minder`'s behavior is not complex; whenever The
Cheeto fires off a tweet, `minder` recieves a POST from an
[IFTT](https://ifttt.com/) webhook and checks the presidential agenda for any
in-progress events (courtesy of the folks over at
[factba.se](factba.se/topic/calendar)). If the president's twittatorial
escapades overlap with a currently scheduled meeting, a "polite" reminder is
published.

Follow `minder` on twitter
[@realTrumpMinder](https://twitter.com/realTrumpMinder).
