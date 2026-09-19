-- SALOMÃO — schema PostgreSQL de referência (V1).
-- Em runtime o DDL é gerenciado pelo Hibernate (ddl-auto=update);
-- este arquivo documenta o modelo canônico e serve para revisão/auditoria.
-- Banco: salomao  (CREATE DATABASE salomao;)

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(40) NOT NULL UNIQUE,
    email VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(120) NOT NULL,
    display_name VARCHAR(80),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users (lower(username));
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users (lower(email));

CREATE TABLE IF NOT EXISTS notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL DEFAULT '',
    content TEXT NOT NULL DEFAULT '',
    category VARCHAR(80) NOT NULL DEFAULT '',
    tags_csv VARCHAR(500) NOT NULL DEFAULT '',
    favorite BOOLEAN NOT NULL DEFAULT FALSE,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_notes_user ON notes (user_id);
CREATE INDEX IF NOT EXISTS idx_notes_user_updated ON notes (user_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS characters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(160) NOT NULL DEFAULT '',
    alias VARCHAR(160) NOT NULL DEFAULT '',
    nickname VARCHAR(80) NOT NULL DEFAULT '',
    age INT CHECK (age IS NULL OR (age >= 0 AND age <= 100000)),
    birth_date VARCHAR(40) NOT NULL DEFAULT '',
    gender VARCHAR(40) NOT NULL DEFAULT '',
    species VARCHAR(80) NOT NULL DEFAULT '',
    nationality VARCHAR(80) NOT NULL DEFAULT '',
    occupation VARCHAR(120) NOT NULL DEFAULT '',
    status VARCHAR(40) NOT NULL DEFAULT '',
    physical_desc TEXT NOT NULL DEFAULT '',
    height VARCHAR(20) NOT NULL DEFAULT '',
    weight VARCHAR(20) NOT NULL DEFAULT '',
    eye_color VARCHAR(40) NOT NULL DEFAULT '',
    hair_color VARCHAR(40) NOT NULL DEFAULT '',
    special_traits TEXT NOT NULL DEFAULT '',
    clothing TEXT NOT NULL DEFAULT '',
    marks TEXT NOT NULL DEFAULT '',
    scars TEXT NOT NULL DEFAULT '',
    other_details TEXT NOT NULL DEFAULT '',
    personality TEXT NOT NULL DEFAULT '',
    likes TEXT NOT NULL DEFAULT '',
    dislikes TEXT NOT NULL DEFAULT '',
    fears TEXT NOT NULL DEFAULT '',
    goals TEXT NOT NULL DEFAULT '',
    motivations TEXT NOT NULL DEFAULT '',
    weaknesses TEXT NOT NULL DEFAULT '',
    virtues TEXT NOT NULL DEFAULT '',
    flaws TEXT NOT NULL DEFAULT '',
    backstory TEXT NOT NULL DEFAULT '',
    skills TEXT NOT NULL DEFAULT '',
    tags_csv VARCHAR(500) NOT NULL DEFAULT '',
    favorite BOOLEAN NOT NULL DEFAULT FALSE,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_characters_user ON characters (user_id);
CREATE INDEX IF NOT EXISTS idx_characters_user_updated ON characters (user_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS powers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL DEFAULT '',
    description TEXT NOT NULL DEFAULT '',
    category VARCHAR(80) NOT NULL DEFAULT '',
    level VARCHAR(40) NOT NULL DEFAULT '',
    limitations TEXT NOT NULL DEFAULT '',
    weaknesses TEXT NOT NULL DEFAULT '',
    origin TEXT NOT NULL DEFAULT '',
    observations TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_powers_user ON powers (user_id);

CREATE TABLE IF NOT EXISTS character_powers (
    character_id UUID NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    power_id UUID NOT NULL REFERENCES powers(id) ON DELETE CASCADE,
    CONSTRAINT uq_character_power UNIQUE (character_id, power_id)
);

CREATE TABLE IF NOT EXISTS stories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(220) NOT NULL DEFAULT '',
    subtitle VARCHAR(300) NOT NULL DEFAULT '',
    content TEXT NOT NULL DEFAULT '',
    tags_csv VARCHAR(500) NOT NULL DEFAULT '',
    status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO'
        CHECK (status IN ('RASCUNHO','EM_REVISAO','CONCLUIDA')),
    favorite BOOLEAN NOT NULL DEFAULT FALSE,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_stories_user ON stories (user_id);
CREATE INDEX IF NOT EXISTS idx_stories_user_updated ON stories (user_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS story_character_mentions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    story_id UUID NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
    character_id UUID NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    CONSTRAINT uq_story_character UNIQUE (story_id, character_id)
);

CREATE TABLE IF NOT EXISTS mental_maps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL DEFAULT '',
    description TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_maps_user ON mental_maps (user_id);

CREATE TABLE IF NOT EXISTS mental_map_nodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    map_id UUID NOT NULL REFERENCES mental_maps(id) ON DELETE CASCADE,
    node_type VARCHAR(20) NOT NULL DEFAULT 'TEXTO'
        CHECK (node_type IN ('TEXTO','PERSONAGEM','PODER','HISTORIA','ANOTACAO','CATEGORIA','LIVRE')),
    label VARCHAR(200) NOT NULL DEFAULT '',
    content TEXT NOT NULL DEFAULT '',
    ref_type VARCHAR(20) CHECK (ref_type IS NULL OR ref_type IN ('CHARACTER','POWER','STORY','NOTE')),
    ref_id UUID,
    pos_x DOUBLE PRECISION NOT NULL DEFAULT 0,
    pos_y DOUBLE PRECISION NOT NULL DEFAULT 0,
    color VARCHAR(20) NOT NULL DEFAULT '',
    collapsed BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_nodes_map ON mental_map_nodes (map_id);

CREATE TABLE IF NOT EXISTS mental_map_edges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    map_id UUID NOT NULL REFERENCES mental_maps(id) ON DELETE CASCADE,
    source_id UUID NOT NULL REFERENCES mental_map_nodes(id) ON DELETE CASCADE,
    target_id UUID NOT NULL REFERENCES mental_map_nodes(id) ON DELETE CASCADE,
    label VARCHAR(120) NOT NULL DEFAULT '',
    CHECK (source_id <> target_id)
);
CREATE INDEX IF NOT EXISTS idx_edges_map ON mental_map_edges (map_id);

CREATE TABLE IF NOT EXISTS tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(40) NOT NULL DEFAULT '',
    color VARCHAR(20) NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_tags_user_name UNIQUE (user_id, name)
);

CREATE TABLE IF NOT EXISTS entity_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_type VARCHAR(20) NOT NULL DEFAULT '',
    entity_id UUID NOT NULL,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    CONSTRAINT uq_entity_tag UNIQUE (user_id, entity_type, entity_id, tag_id)
);
CREATE INDEX IF NOT EXISTS idx_entitytags_lookup ON entity_tags (entity_type, entity_id);

CREATE TABLE IF NOT EXISTS activity_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action VARCHAR(40) NOT NULL DEFAULT '',
    entity_type VARCHAR(20) NOT NULL DEFAULT '',
    entity_id UUID,
    description VARCHAR(300) NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_activity_user ON activity_log (user_id, created_at DESC);
