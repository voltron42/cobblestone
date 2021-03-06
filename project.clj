(defproject cobblestone "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [http-kit "2.2.0"]
                 [environ "1.0.0"]
                 [metosin/compojure-api "1.1.11"]
                 [org.clojars.pallix/batik "1.7.0"]
                 [ring/ring-mock "0.3.2"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "cobblestone.jar"
  :resource-paths ["resources"]
  :profiles {:production {:env {:production true}
                          :resource-paths ["resources"]}})
