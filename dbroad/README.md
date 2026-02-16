# Digitraffic road database

## ;TL;TR

````bash
docker-compose rm db && docker-compose build && docker-compose up
````

## Directory structure

    sql
    └ base       -> Empty root
    |   └ common -> Common initializations for all environments, including all table creations before V2 versions
    |   └ dev    -> Initializations used in development environment
    |   └ prod   -> V1 initializations run in production and test, not needed in development
    |               These cannot be discarded because Flyway requires history to be preserved
    |
    └ update     -> Maintenance scripts that are always executed (in root)
        └ V2     -> All database updates after V1 version

## Running db instance

Execute commands

````bash
docker-compose build && docker-compose up
````

PostgreSql db is running at localhost:54322

[PgHero](https://github.com/ankane/pghero) is running at [http://localhost:8082](http://localhost:8082)

List containers
``````bash
docker-compose ps
``````

Removing containers
``````bash
docker-compose rm db
``````

# Running database migrations manually

## Help

Run [update-db.sh](update-db.sh) with -h or --help to get help.

    ./update-db.sh -h

## Migrate

This will execute all pending migrations in the database.

    ./update-db.sh dev migrate

## Mark missing migrations as executed

This is useful when you have added new migrations to db by hand,
but you don't want to execute them yet. This will mark all missing migrations as executed in the database, so that they won't be executed when you run the application.

    ./update-db.sh dev migrate -skipExecutingMigrations=true

