(defproject monarch "0.2.2"
  :description "Rails style migrations for Clojure projects."
  :url "https://github.com/mcramm/monarch"
  :dependencies [[org.clojure/java.jdbc "0.3.3"]
                 [postgresql "9.3-1101.jdbc4"]
                 [org.clojure/tools.reader "0.8.4"]
                 [environ "0.5.0"]]
  :plugins [[lein-environ "0.5.0"]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true);
