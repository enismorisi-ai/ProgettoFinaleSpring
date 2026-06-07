create table images(
    id bigint auto_increment primary key,
    path varchar(255) not null,
    article_id bigint,
    foreign key(article_id) references articles(id)
);