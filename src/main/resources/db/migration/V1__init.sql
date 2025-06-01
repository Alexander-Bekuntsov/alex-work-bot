create table user_responses
(
    user_id      bigint not null
        primary key,
    created_time timestamp(6),
    user_email   varchar(255),
    user_name    varchar(255),
    user_score   integer
);

create table user_state_data
(
    user_id     bigint       not null,
    field_value varchar(255),
    field_key   varchar(255) not null,
    primary key (user_id, field_key)
);

create table user_states
(
    user_id       bigint       not null primary key,
    current_state varchar(255) not null,
    updated_at    timestamp(6) not null
);
