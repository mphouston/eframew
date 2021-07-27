/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */



CREATE TABLE IF NOT EXISTS public.field_extension (
    uuid              uuid PRIMARY KEY,
    domain_class_name varchar(255) NOT NULL,
    field_name        varchar(30)  NOT NULL,
    field_label       varchar(80),
    field_format      varchar(30)  NOT NULL,
    max_length        integer,
    sequence          integer      NOT NULL,
    value_class_name  varchar(255),
    gui_hints         varchar(1024),
    version           integer      NOT NULL,
    UNIQUE (domain_class_name, field_name),
    required          boolean default false,
    history_tracking  varchar(30)

);

CREATE TABLE public.field_gui_extension (
    uuid             uuid PRIMARY KEY,
    domain_name      varchar(255) NOT NULL,
    adjustments_text jsonb,
    date_created     timestamp with time zone,
    date_updated     timestamp with time zone,
    version          integer      NOT NULL,
    UNIQUE (domain_name)
);

CREATE TABLE public.flex_type (
    uuid              uuid PRIMARY KEY,
    category          varchar(255) NOT NULL,
    flex_type         varchar(30)  NOT NULL,
    title             varchar(80),
    default_flex_type boolean      NOT NULL,
    date_created      timestamp with time zone,
    date_updated      timestamp with time zone,
    version           integer      NOT NULL,
    default_choice    boolean      NOT NULL,
    UNIQUE (flex_type)
);

CREATE TABLE public.flex_field (
    uuid             uuid PRIMARY KEY,
    flex_type_id     uuid REFERENCES flex_type,
    field_name       varchar(30) NOT NULL,
    field_label      varchar(80),
    field_format     varchar(30) NOT NULL,
    max_length       integer,
    sequence         integer     NOT NULL,
    value_class_name varchar(255),
    gui_hints        varchar(1024),
    history_tracking varchar(30),
    required         boolean default false
);
CREATE INDEX IF NOT EXISTS fki_flex_field_flex_type_id_fkey
    ON public.flex_field(flex_type_id);

CREATE TABLE IF NOT EXISTS public.refresh_token (
    uuid              uuid PRIMARY KEY,
    refresh_token     text        NOT NULL,
    user_name         varchar(30) NOT NULL,
    enabled           boolean     NOT NULL,
    expiration_date   timestamp with time zone,
    use_attempt_count integer     NOT NULL,
    request_source    text,
    date_created      timestamp with time zone,
    date_updated      timestamp with time zone,
    UNIQUE (refresh_token)
);

CREATE INDEX IF NOT EXISTS fki_refresh_token_user_name
    ON public.refresh_token(user_name);

CREATE TABLE IF NOT EXISTS public.role (
    uuid      uuid PRIMARY KEY,
    authority varchar(30) NOT NULL,
    title     varchar(80) NOT NULL,
    UNIQUE (authority)
);

CREATE TABLE IF NOT EXISTS public.usr (
    uuid             uuid PRIMARY KEY,
    user_name        varchar(30)  NOT NULL,
    display_name     varchar(80),
    encoded_password varchar(128) NOT NULL,
    enabled          boolean      NOT NULL,
    account_expired  boolean      NOT NULL,
    account_locked   boolean      NOT NULL,
    password_expired boolean      NOT NULL,
    email            varchar(255),
    version          integer      NOT NULL,
    date_created     timestamp with time zone,
    date_updated     timestamp with time zone,
    UNIQUE (user_name)
);

CREATE TABLE IF NOT EXISTS public.user_role (
    user_id uuid NOT NULL REFERENCES usr,
    role_id uuid NOT NULL REFERENCES role
);

CREATE INDEX IF NOT EXISTS fki_user_role_role_id_fkey
    ON public.user_role(role_id);


CREATE TABLE public.user_preference (
    uuid             uuid PRIMARY KEY,
    user_name        varchar(30)  NOT NULL,
    page             varchar(100) NOT NULL,
    preferences_text jsonb,
    date_created     timestamp with time zone,
    date_updated     timestamp with time zone,
    version          integer      NOT NULL,
    UNIQUE (user_name, page)
);


