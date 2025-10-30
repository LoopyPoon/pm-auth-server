-- Включаем ротацию refresh токенов и корректируем TTL под стандартный сценарий:
-- - access_token: 15 минут (PT15M)
-- - refresh_token: 30 дней (P30D)
-- Также добавляем offline_access в scopes клиента pm-spa (если отсутствует)

update oauth2_registered_client
set token_settings =
        jsonb_set(
                jsonb_set(
                        jsonb_set(token_settings::jsonb,
                                  '{settings.token.access-token-time-to-live}', '"PT15M"', true),
                        '{settings.token.refresh-token-time-to-live}', '"P30D"', true
                ),
                '{settings.token.reuse-refresh-tokens}', 'false', true
        )
where client_id = 'pm-spa';

-- Добавляем offline_access в scopes при необходимости
update oauth2_registered_client
set scopes = case
                 when scopes like '%offline_access%' then scopes
                 else concat(scopes, ',offline_access')
    end
where client_id = 'pm-spa';

-- Убеждаемся, что у клиента есть grant_type refresh_token (на всякий случай)
-- В 1.5.x grant types хранятся строкой с запятыми, но форматируем через JSONB для надёжности, если это JSON.
-- Если у тебя строка, строковый append ниже сработает безопасно.
update oauth2_registered_client
set authorization_grant_types = case
                                    when authorization_grant_types like '%refresh_token%' then authorization_grant_types
                                    else concat(authorization_grant_types, ',refresh_token')
    end
where client_id = 'pm-spa';