![Dance.gif](http://i.imgur.com/fl4YBzC.jpg)

#About

Android application written in pure Clojure for fun.  Flashes colors, plays music loudly, and includes a music visualizer.

#Builds

###Debug

lean droid doall

###Release

lein with-profile release droid doall

###Skummet Release

lein with-profile lean do clean, droid doall
