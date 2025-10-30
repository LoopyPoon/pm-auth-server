-- Приводим схему к ожиданиям Spring Authorization Server 1.5.x

-- Таблица авторизаций: переводим JSON-поля из bytea в text
alter table if exists oauth2_authorization
alter column attributes                 type text using convert_from(attributes, 'UTF8'),
    alter column authorization_code_metadata type text using convert_from(authorization_code_metadata, 'UTF8'),
    alter column access_token_metadata       type text using convert_from(access_token_metadata, 'UTF8'),
    alter column oidc_id_token_metadata      type text using convert_from(oidc_id_token_metadata, 'UTF8'),
    alter column refresh_token_metadata      type text using convert_from(refresh_token_metadata, 'UTF8'),
    alter column user_code_metadata          type text using convert_from(user_code_metadata, 'UTF8'),
    alter column device_code_metadata        type text using convert_from(device_code_metadata, 'UTF8');

-- Таблица зарегистрированных клиентов: строки конфигураций и списки переводим в text
alter table if exists oauth2_registered_client
alter column client_authentication_methods type text,
    alter column authorization_grant_types     type text,
    alter column redirect_uris                 type text,
    alter column post_logout_redirect_uris     type text,
    alter column scopes                        type text,
    alter column client_settings               type text,
    alter column token_settings                type text;

-- (опционально) Таблица согласий: список полномочий сделаем text для совместимости
alter table if exists oauth2_authorization_consent
alter column authorities type text;