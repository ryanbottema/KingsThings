begin transaction;

    drop table if exists users;
    drop table if exists games;

    create table users(
        username text primary key not null
        /* password text not null */
    );

    create table games(
        id integer primary key autoincrement,
        gameSize integer not null,
        numPlayers integer not null,
        user1 text,
        user2 text,
        user3 text,
        user4 text
    );

    create table boardStates(
        gameID integer primary key references games.id
    );

end transaction;
