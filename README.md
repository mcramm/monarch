# Monarch

A Leiningen plugin to handle rails-style migrations.

## Usage

Use this for user-level plugins:

Put `[monarch "0.2.1"]` into the `:plugins` vector of your
`:user` profile, or if you are on Leiningen 1.x do `lein plugin install
monarch 0.2.1`.

Use this for project-level plugins:

Put `[monarch "0.2.1"]` into the `:plugins` vector of your project.clj.

The default config will look for migration files in `data/migrations`, and will
track which versions have been applied in the `schema_versions` table. You can
override these defaults by adding a `:migrations` key pointing at a map in your
project.clj like this:

```clj
:migrations {:table       "schema_versions"
             :location    "data/migrations"
             :config-lens :helloworld}
```

* `:location` identifies where to look for migrations.
* `:table` is the name of the table that tracks which migrations have been
applied.
* `:config-lens` is a path inside the `:env` key of `~/.lein/profiles.clj` where
  monarch will look for the necessary configuration variables.

## Caveats

This library has only been tested on Unix with Postgres. If you encounter any
issues with your platform/database, please open an issue.

## Setup

Monarch uses [environ](https://github.com/weavejester/environ) for
environment configuration. It needs to know how to connect to your database, and
does this by looking for a `DATABASE_URL` environment variable.

It is recommended that you export this variable as such (Unix):

`export DATABASE_URL="postgresql://localhost:5432/helloworld"`

This can get cumbersome for development, so you can also specify this in
`~/.lein/profiles.clj`.

```clj

{:user
    {:env
        {:helloworld {:database-url "postgresql://localhost:5432/helloworld"}}}}

```

You will also need to add the `[lein-enviorn "0.5.0"]` to the `:plugins` vector
in project.clj.

> If you are working with multiple projects with conflicting keys, then it's
> recommended that you scope `DATABASE_URL` underneath a project identifier.
> This identifier can be configured in project.clj, and is mentioned above. If
> you don't need to worry about this, then you can simply leave the var
> underneath `:env`. Monarch will check the project scoped key first. After that
> it will look for the exported environment variable, then a `:database-url` key
> in profiles.clj.

Once you've done that, run `lein monarch setup` to create the table that tracks
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
2. `cd` into the project and add `[monarch "0.2.1"]` to the `:plugins` key in
   project.clj
3. Run `lein deps`
4. Startup Postgres and create a database named "helloworld".
5. Export the following environment variable. Note that your connection
   information might be different:

   `export DATABASE_URL="postgresql://localhost:5432/helloworld"`

5. Run `lein monarch setup`.
6. Run `lein monarch generate create_users`
7. Edit the resulting migration in data/migrations to look like the migration
   above.
8. run `lein monarch up`

There is an [example app](https://github.com/mcramm/monarch-blog) too.
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
