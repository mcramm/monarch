# Monarch

A Leiningen plugin to handle rails-style migrations.

## Usage

Use this for user-level plugins:

Put `[monarch "0.0.2"]` into the `:plugins` vector of your
`:user` profile, or if you are on Leiningen 1.x do `lein plugin install
monarch 0.2.0`.

Use this for project-level plugins:

Put `[monarch "0.2.0"]` into the `:plugins` vector of your project.clj.

The default config will look for migration files in `data/migrations`, and will
track which versions have been applied in the `schema_versions` table. You can
override these defaults by adding a `:migrations` key pointing at a map in your
project.clj like this:

```clj
:migrations {:table     "schema_versions"
             :location  "data/migrations"}
```

* `:location` identifies where to look for migrations.
* `:table` is the name of the table that tracks which migrations have been
applied.

## Caveats

This library has only been tested on Unix with Postgres. If you encounter any
issues with your platform/database, please open an issue.

## Setup

You will need to have set an environment variable, `DATABASE_URL`, that contains
the connection information to your database.

Example (Unix):

`export DATABASE_URL="postgresql://localhost:5432/helloworld"`


Once you've done that, run `lein monarch :setup` to create the table that tracks
the applied migrations. By default this is `schema_versions`, but can be
overridden as mentioned above.

## Migrations

Migrations are currently E.D.N. data, and take the form of a map with an `:up`
and `:down` keys:

`{:up [] :down []}`

You can place an arbitrary sequence of statements in the supplied vector, and they
will be executed in the order defined. All statements are applied in a
transaction.

Example:
```clj
{:up ["CREATE TABLE users (
          id SERIAL PRIMARY KEY,
          email TEXT,
          password TEXT
       )"
      "CREATE TABLE posts (
          id SERIAL,
          user_id INTEGER REFERENCES users(id),
          title TEXT,
          body TEXT
      )"]
 :down ["DROP TABLE posts"
        "DROP TABLE users"]}
```

## Quick Tutorial

1. Create a new project with `lein new hello-world`
2. `cd` into the project and add `[monarch "0.2.0"]` to the `:plugins` key in
   `project.clj`
3. Run `lein deps`
4. Startup Postgres and create a database named "helloworld".
5. Export the following environment variable. Note that your connection
   information might be different:

   `export DATABASE_URL="postgresql://localhost:5432/helloworld"`

5. Run `lein monarch :setup`.
6. Run `lein monarch :generate create_users`
7. Edit the resulting migration in data/migrations to look like the migration
   above.
8. run `lein monarch :up`

## Available Commands

Examples
```bash
$ lein monarch setup           # Run necessary setup.
$ lein monarch generate <name> # generate a new migration file
$ lein monarch
$ lein monarch up              # apply all outstanding migrations
$ lein monarch rollback        # roll the database back one version
```

## License

Copyright © 2014 G. Michael Cramm

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
