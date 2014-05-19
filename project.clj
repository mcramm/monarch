(defproject monarch "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/java.jdbc "0.3.3"]
                 [postgresql "9.3-1101.jdbc4"]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :migrations {:location "data/migrations"}
  :eval-in-leiningen true);
