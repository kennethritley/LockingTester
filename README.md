# Concurrency Control "Locking Tester"

This is a Java application used that provides a good example of  three
ways you can handle concurrency control:

1. Optimistic locking, e.g. read a version string and only update the DB if the 
version string hasn't changed
2. Pessimistic locking, e.g. use SELECT...FOR UPDATE which forces the DB to
put write locks on the table in question
3. Isolation levels, e.g. you tell the database how it should treat any concurrent
transactions, and it does the rest

There are other ways, such as via stored procedures, but these three are the most
common.

### Benefits for students:

- Nice example of the three approaches in just one little application!

## Table of Contents

- [Installation](#installation)
- [Javadocs](#javadocs)
- [Authors](#authors)
- [License](#license)
- [CHANGELOG.md](CHANGELOG.md)


## Changelog

For a detailed list of changes and version history, please refer to the [CHANGELOG.md](CHANGELOG.md) file.

## Installation

1. Usual Java stuff.  
2. A PostgreSQL is hard-coded into this application so you'll need to either change that
or at least add your own DB credentials. (Be careful of the port: 5432 is usual for PostgreSQL
but Dr. Ken uses 5431 with Docker.)


## Javadocs

Javadocs are the built-in documentation system in Java, so that with one
command you can create beautiful HTML files that show your source code
documentation.

This application does NOT use Javadocs

## Authors

99% of this application was written by Stefan Fischli.
Your friendly, neighborhood Dr. Ken just tweaked it a bit to make it a 
bit more user-friendly as a study tool.

## License

Never operate a motor vehicle without the proper license.
