(ns monarch.util
  (:require [clojure.string :as string]))

(defn- get-version [file-name]
  (first (string/split file-name #"_")))

(defn get-file-name [file]
  (last (string/split (.getPath file)
                      (re-pattern (java.io.File/separator)))))

(defn file->version [file]
  (-> file
      get-file-name
      get-version))
