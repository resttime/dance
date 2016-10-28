# ![Dance.gif](/res/drawable/ppd.gif)

Fun Android application written in pure Clojure.

# Builds

#Debug

lean droid doall

#Release

lein with-profile release droid doall

#Skummet Release

lein with-profile lean do clean, droid doall
