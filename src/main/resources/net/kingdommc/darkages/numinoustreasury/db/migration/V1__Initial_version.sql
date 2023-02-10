create table numinous_character_profession(
    character_id integer primary key,
    profession_id varchar(36) not null,
    experience integer not null
);

create table numinous_character_stamina(
    character_id integer primary key,
    stamina integer not null
);