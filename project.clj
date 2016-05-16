(defproject test/test "0.1.0-SNAPSHOT"
  :description "Funky"
  :url "https://google.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :global-vars {*warn-on-reflection* true}

  :source-paths ["src/clojure" "src"]
  :java-source-paths ["src/java"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :plugins [[lein-droid "0.4.3"]]
  
  :dependencies [[org.clojure-android/clojure "1.7.0-r2"]
                 [neko/neko "4.0.0-alpha5"]
                 [com.android.support/support-v4 "23.3.0" :extension "aar"]
                 [com.koushikdutta.async/androidasync "2.1.6" :extension "aar"]
                 [com.koushikdutta.ion/ion "2.1.6" :extension "aar"]]
  :profiles {:default [:dev]

             :dev
             [:android-common :android-user
              {:dependencies [[org.clojure/tools.nrepl "0.2.12"]]
               :target-path "target/debug"
               :android {:aot :all-with-unused
                         :rename-manifest-package "org.test.debug"
                         :manifest-options {:app-name "test (debug)"}}}]
             :lean
             [:release
              {:dependencies ^:replace [[org.skummet/clojure "1.7.0-r1"]
                                        [neko/neko "4.0.0-alpha5"]
                                        [com.android.support/support-v4 "23.3.0" :extension "aar"]
                                        [com.koushikdutta.async/androidasync "2.1.6" :extension "aar"]
                                        [com.koushikdutta.ion/ion "2.1.6" :extension "aar"]]
               :exclusions [[org.clojure/clojure]
                            [org.clojure-android/clojure]]
               :jvm-opts ["-Dclojure.compile.ignore-lean-classes=true"]
               :global-vars ^:replace {clojure.core/*warn-on-reflection* true}
               :android {:lean-compile true
                         :proguard-execute false
                         :proguard-conf-path "build/proguard-minify.cfg"}}]
             
             :release
             [:android-common
              {:target-path "target/release"
               :android
               {;; :keystore-path "/home/user/.android/private.keystore"
                ;; :key-alias "mykeyalias"
                ;; :sigalg "MD5withRSA"

                :ignore-log-priority [:debug :verbose]
                :aot :all
                :build-type :release}}]}

  :android {;; Specify the path to the Android SDK directory.
            ;; :sdk-path "/home/user/path/to/android-sdk/"

            ;; Try increasing this value if dexer fails with
            ;; OutOfMemoryException. Set the value according to your
            ;; available RAM.
            :dex-opts ["-JXmx4096M" "--incremental"]
            ;:multi-dex true
            ;:multi-dex-proguard-conf-path "build/proguard-multi-dex.cfg"
            :target-version "23"
            :aot-exclude-ns ["clojure.parallel" "clojure.core.reducers"
                             "cider.nrepl" "cider-nrepl.plugin"
                             "cider.nrepl.middleware.util.java.parser"
                             #"cljs-tooling\..+"]})
