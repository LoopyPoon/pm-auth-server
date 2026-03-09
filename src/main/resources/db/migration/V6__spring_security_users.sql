-- Таблица ролей
CREATE TABLE IF NOT EXISTS roles (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Предзаполняем роли
INSERT INTO roles (name) VALUES ('ROLE_USER') ON CONFLICT DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT DO NOTHING;

-- Таблица связи user <-> role (many-to-many)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Добавляем username если ещё нет
ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(100);
UPDATE users SET username = split_part(email, '@', 1) WHERE username IS NULL;
ALTER TABLE users ALTER COLUMN username SET NOT NULL;

-- Уникальный индекс вместо CONSTRAINT IF NOT EXISTS
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_username ON users(username);

-- Мигрируем старые authorities -> user_roles
INSERT INTO user_roles (user_id, role_id)
SELECT a.user_id, r.id
FROM authorities a
JOIN roles r ON r.name = a.authority
ON CONFLICT DO NOTHING;
