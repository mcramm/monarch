(ns monarch.test.util
  (:require [clojure.test :refer :all]
            [monarch.util :refer [get-file-name file->version]]))

(def test-version "20140516161508313")
(def test-file-name (str test-version "_create_users.edn"))

(def test-file
  (let [dir (java.io.File. "tmp/migrations")]
    (java.io.File. dir test-file-name)))


(deftest test-last-path-part
  (is (= test-file-name
         (get-file-name test-file))))

(deftest test-file->version
  (is (= test-version
         (file->version test-file))))
