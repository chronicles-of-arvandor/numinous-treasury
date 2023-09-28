create table numinous_character_profession(
    character_id integer primary key,
    profession_id varchar(36) not null,
    experience integer not null
);

create table numinous_character_stamina(
    character_id integer primary key,
    stamina integer not null
);

create table numinous_node(
    id varchar(36) primary key,
    name varchar(128) not null,
    experience integer not null,
    stamina_cost integer not null,
    drop_table_id varchar(256),
    entrance_world_id varchar(36) not null,
    entrance_min_x integer not null,
    entrance_min_y integer not null,
    entrance_min_z integer not null,
    entrance_max_x integer not null,
    entrance_max_y integer not null,
    entrance_max_z integer not null,
    entrance_warp_destination_world_id varchar(36),
    entrance_warp_destination_x double not null,
    entrance_warp_destination_y double not null,
    entrance_warp_destination_z double not null,
    entrance_warp_destination_yaw real not null,
    entrance_warp_destination_pitch real not null,
    exit_world_id varchar(36) not null,
    exit_min_x integer not null,
    exit_min_y integer not null,
    exit_min_z integer not null,
    exit_max_x integer not null,
    exit_max_y integer not null,
    exit_max_z integer not null,
    exit_warp_destination_world_id varchar(36),
    exit_warp_destination_x double not null,
    exit_warp_destination_y double not null,
    exit_warp_destination_z double not null,
    exit_warp_destination_yaw real not null,
    exit_warp_destination_pitch real not null,
    area_world_id varchar(36) not null,
    area_min_x integer not null,
    area_min_y integer not null,
    area_min_z integer not null,
    area_max_x integer not null,
    area_max_y integer not null,
    area_max_z integer not null
);

create table numinous_node_required_profession(
    node_id varchar(36) not null,
    profession_id varchar(256) not null,
    level integer not null,
    constraint fk_required_profession_node foreign key(node_id) references numinous_node(id) on delete cascade on update cascade,
    primary key(node_id, profession_id)
);
