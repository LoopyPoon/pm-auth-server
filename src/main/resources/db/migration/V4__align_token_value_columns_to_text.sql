-- Приводим столбцы *_value к типу text под Spring Authorization Server 1.5.x

alter table if exists oauth2_authorization
alter column authorization_code_value type text using convert_from(authorization_code_value, 'UTF8'),
    alter column access_token_value       type text using convert_from(access_token_value,       'UTF8'),
    alter column oidc_id_token_value      type text using convert_from(oidc_id_token_value,      'UTF8'),
    alter column refresh_token_value      type text using convert_from(refresh_token_value,      'UTF8'),
    alter column user_code_value          type text using convert_from(user_code_value,          'UTF8'),
    alter column device_code_value        type text using convert_from(device_code_value,        'UTF8');