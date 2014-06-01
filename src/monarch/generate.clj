(ns monarch.generate)

(defn- now []
  (java.util.Date.))

(defn current-time-string [date]
  (.format (java.text.SimpleDateFormat. "yyyyMMddHHmmssS")
           date))

(defn create-migration-file [directory file-name]
  (let [dir (java.io.File. directory)
        file (java.io.File.
               dir
               (str (current-time-string (now)) "_" file-name ".edn"))]
    (.mkdirs dir)
    (let [path (.getPath file)]
      (spit path "{:up [] :down []}")
      path)))
