(ns leiningen.monarch
  (:require [monarch.core :refer [process-command]]))

(defn monarch
  "
   Generate and apply rails-style database migrations.

   Make sure you've set an environment variable, DATABASE_URL.
   Example (Unix):

   export DATABASE_URL=\"postgresql://localhost:5432/helloworld\"

   Available commands:
    $ lein monarch :setup           # Run necessary setup. (only run once)
    $ lein monarch :generate <name> # generate a new migration file

    $ lein monarch
    $ lein monarch :up              # apply all outstanding migrations

    $ lein monarch :rollback        # roll the database back one version
  "
  [project & args]
  (process-command (first args) (:migrations project) (rest args)))
