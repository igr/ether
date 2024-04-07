create table todo_list(
    id uuid primary key,
    name text null default null
);

create table todo_item(
    id uuid primary key,
    list_id uuid not null,
    name text not null ,
    done boolean not null default false,
    foreign key (list_id) references todo_list(id)
);
