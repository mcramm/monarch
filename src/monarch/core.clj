(ns monarch.core
  (:require [clojure.java.jdbc :as sql]
            [monarch.generate :refer [create-migration-file]]
            [monarch.util :refer [file->version get-file-name]]
            [clojure.tools.reader.edn :as edn]
            [environ.core :refer :all]))

(defmulti process-command
  (fn [command config opts]
    (when command
      (edn/read-string command))))

(defmethod process-command :default [command config opts]
  (println "Unrecognized command" command))

(def default-config {:location "data/schema_migrations"
                     :table "schema_versions"})

(defn get-spec [config]
  (get-in env
          [(:config-lens config) :database-url]
          (env :database-url)))

;;==============================================================================
;; Generate

(defmethod process-command 'generate [command config opts]
  (if (first opts)
    (let [path (create-migration-file (:location (merge default-config config))
                           (first opts))]
      (println "Generated" path))
    (println "no file name specified")))

;;==============================================================================
;; Migrating

(defn get-available-migrations
  "Takes a directory and returns a list of files inside."
  [location]
  (rest (file-seq (java.io.File. location))))

(defn get-migration
  "Slurps the contents of a file and parses it."
  [file]
  (let [contents (slurp (.getPath file))]
    (edn/read-string contents)))

(defn remove-version
  "Removes a version from the tracking table."
  [conn table version]
  (sql/delete! conn (keyword table) ["version = ?" version]))

(defn insert-version
  "Appends a version to the tracking table."
  [conn table version]
  (sql/insert! conn (keyword table) [:version] [version]))

(defn execute-change
  "Executes the statements associated with a particular direction.
   Will insert/remove the version from the tracking table based on the
   direction.
   All operations are run within a transaction."
  [spec migration direction table version]
  (sql/with-db-transaction [tran spec]
    (try
      (doseq [statement (get migration direction)]
        (sql/db-do-commands tran statement))

      (case direction
        :up (insert-version tran table version)
        :down (remove-version tran table version)
        (println "Unknown direction" direction))

      (catch java.sql.BatchUpdateException e
        (println "Error applying" direction "on" version)
        (println (.getMessage e))
        (println (.getMessage (.getNextException e)))))))

;;==============================================================================
;; Migrating Down

(defn get-last-version
  "Get the most recently applied version."
  [spec table]
  (-> (sql/query spec
                [(str "select max(version) from  " table)])
      first
      :max))

(defn migrate-down
  "Rolls back the most recent version applied to the database."
  [spec {:keys [location table]}]
  (if-let [version (get-last-version spec table)]
    (let [files (filter #(re-find (re-pattern version)
                                  (get-file-name %)) (get-available-migrations location))]
      (println "Rolling back" version)
      (execute-change spec
                      (get-migration (first files))
                      :down
                      table
                      version))
    (println "Nothing to roll back.")))

(defmethod process-command 'rollback [command config opts]
  (if-let [spec (get-spec config)]
      (migrate-down spec (merge default-config config ))
      (println "No DATABASE_URL has been setup.")))

;;==============================================================================
;; Migrating Up

(defn applied?
  "Checks if a specific version exists in a table."
  [spec table version]
  (not
    (nil?
     (-> (sql/query spec
                [(str "select version from  " table " where version = ?")
                 version])
         first
         :version))))

(defn migrate-up
  "Applies all outstanding migrations."
  [spec {:keys [location table]}]
  (if-not spec
    (println "No DATABASE_URL has been setup.")
    (do
      ; get list of files
      (doseq [file (get-available-migrations location)]
        (let [version (file->version file)]
          (when-not (applied? spec table version)
            (println "Migrating" version)
            (execute-change spec
                            (get-migration file)
                            :up
                            table
                            version)))))))

(defmethod process-command 'up [command config opts]
  (migrate-up (get-spec config) (merge default-config config)))

(defmethod process-command nil [command config opts]
  (migrate-up (get-spec config) (merge default-config config)))


;;==============================================================================
;; Setup

(defn create-tracking-table
  "Creates the table that will track the applied migrations."
  [spec table]
  (sql/db-do-commands spec
                      (sql/create-table-ddl
                        (keyword table)
                        [:version :varchar "NOT NULL"])))

(defmethod process-command 'setup [command config opts]
  (if-let [spec (get-spec config)]
    (create-tracking-table spec (:table (merge default-config config)))
    (println "No DATABASE_URL has been setup.")))
