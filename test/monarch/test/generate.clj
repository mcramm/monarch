(ns monarch.test.generate
  (:require [clojure.test :refer :all]
            [monarch.generate :refer [current-time-string create-migration-file]]))

(def test-files-dir "tmp/migrations")

(defn remove-test-files [f]
  (f)
  (doseq [file (reverse (file-seq (java.io.File. test-files-dir)))]
    (.delete file)))

(use-fixtures :once remove-test-files)

(deftest test-current-time-string
  (is (= "201401011355200"
         (current-time-string
           (java.util.Date. (- 2014 1900)
                            00
                            01
                            13
                            55
                            20)))))

(deftest test-create-migration-file
  (create-migration-file test-files-dir "my_test_migration")

    (let [dir (java.io.File. test-files-dir)
          file (second (file-seq dir))]

      (is (not (nil? (re-find (re-pattern "my_test_migration")
                              (.getPath file)))))
      (is (= "{:up [] :down []}"
             (slurp (.getPath file))))))
