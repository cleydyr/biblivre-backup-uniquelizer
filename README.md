# biblivre-backup-uniquelizer
Removes duplicate rows from a Biblivre backup and generates a valid new one.

## Context
Some backups are being generated with rows that have duplicated ids. However, some tables have the id column as primary key, so they must be unique.

We don't know the root cause yet, but this program will help eliminate duplicate entries.

## Drawbacks
The removal doesn't take into account other constraints, like primary keys composed of multiple columns.
Also, there's not guarantee of data integrity. If the duplicated row is removed the resulting change in the behavior of the application is unknown.

## Usage

1. Clone repository
2. mvn clean package
3. java -jar target/biblivre-backup-uniquelizer-0.0.1-SNAPSHOT.jar <path-to-backup-file>
  
The clean backup file will be created with the same name as the source backup with a suffix in the same directory where the application is being run.
