create table career_request(
    id bigint auto_increment primary key,
    body text,
    user_id bigint,
    foreign key (user_id) references users(id),
    role_id bigint,
    foreign key (role_id) references roles(id),
    is_checked boolean
);