create table candidates
(
    id            serial primary key,
    name          varchar not null,
    description   varchar not null,
    creation_date timestamp,
    city_id       int references cities (id),
    visible       boolean not null,
    file_id       int references files (id)
);