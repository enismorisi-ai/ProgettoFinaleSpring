create table users(
    id bigint auto_increment primary key,
    username varchar(100),
    email varchar(100) not null unique,
    password varchar(100) not null,
    created_at timestamp default current_timestamp
);

create table roles(
    id bigint auto_increment primary key,
    name varchar(100) not null
);

create table categories (
    id bigint auto_increment primary key,
    name varchar(50)
);

create table articles (
    id bigint auto_increment primary key,
    title varchar(100),
    subtitle varchar(100),
    body text,
    publish_date date,
    created_at timestamp default current_timestamp,
    user_id bigint,
    category_id bigint,
    foreign key(user_id) references users(id),
    foreign key(category_id) references categories(id)
);

create table users_roles(
    id bigint auto_increment primary key,
    user_id bigint,
    role_id bigint,
    foreign key(user_id) references users(id),
    foreign key(role_id) references roles(id)
);