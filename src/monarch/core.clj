(ns monarch.core
  (:require [clojure.java.jdbc :as sql]
            [monarch.generate :refer [create-migration-file]]
            [monarch.util :refer [file->version get-file-name]]))

(defmulti process-command
  (fn [command config opts]
    (when command
      (read-string command))))

(def default-config {:location "data/schema_migrations"
                     :table "schema_versions"})

(def spec (System/getenv "DATABASE_URL"))

;;==============================================================================
;; Generate

(defmethod process-command :generate [command config opts]
  (if (first opts)
    (create-migration-file (:location (merge default-config config))
                           (first opts))
    (println "no file name specified")))

;;==============================================================================
;; Migrating

(defn get-available-migrations [location]
  (rest (file-seq (java.io.File. location))))

(defn get-migration [file]
  (let [contents (slurp (.getPath file))]
    (read-string contents)))

(defn remove-version [conn table version]
  (sql/delete! conn (keyword table) ["version = ?" version]))

(defn insert-version [conn table version]
  (sql/insert! conn (keyword table) [:version] [version]))

(defn execute-change [migration direction table version]
  (sql/with-db-transaction [conn spec]
    (try
      (doseq [statement (get migration direction)]
        (sql/db-do-commands conn statement))

      (case direction
        :up (insert-version conn table version)
        :down (remove-version conn table version)
        (println "Unknown direction" direction))

      (catch java.sql.BatchUpdateException e
        (println "Error applying" direction "on" version)
        (println (.getMessage e))
        (println (.getMessage (.getNextException e)))))))

;;==============================================================================
;; Migrating Down

(defn get-last-version [table]
  (-> (sql/query spec
                [(str "select max(version) from  " table)])
      first
      :max))

(defn migrate-down [{:keys [location table]} opts]
  (if-let [version (get-last-version table)]
    (let [files (filter #(re-find (re-pattern version)
                                  (get-file-name %)) (get-available-migrations location))]
      (println "Rolling back" version)
      (execute-change (get-migration (first files))
                      :down
                      table
                      version))
    (println "Nothing to roll back.")))

(defmethod process-command :rollback [command config opts]
  (if-not spec
    (println "No DATABASE_URL has been setup.")
    (migrate-down (merge default-config config ) opts)))


;;==============================================================================
;; Migrating Up

(defn applied? [table version]
  (not
    (nil?
     (-> (sql/query spec
                [(str "select version from  " table " where version = ?")
                 version])
         first
         :version))))

(defn migrate-up [{:keys [location table]} opts]
  (if-not spec
    (println "No DATABASE_URL has been setup.")
    (do
      ; get list of files
      (doseq [file (get-available-migrations location)]
        (let [version (file->version file)]
          (when-not (applied? table version)
            (println "Migrating" version)
            (execute-change (get-migration file)
                            :up
                            table
                            version)))))))

(defmethod process-command :up [command config opts]
  (migrate-up (merge default-config config) opts))

(defmethod process-command nil [command config opts]
  (migrate-up (merge default-config config) opts))


;;==============================================================================
;; Setup

(defn create-tracking-table [table]
  (sql/db-do-commands spec
                      (sql/create-table-ddl
                        (keyword table)
                        [:version :varchar "NOT NULL"])))

(defmethod process-command :setup [command config opts]
  (if-not spec
    (println "No DATABASE_URL has been setup.")
    (create-tracking-table (:table (merge default-config config)))))
