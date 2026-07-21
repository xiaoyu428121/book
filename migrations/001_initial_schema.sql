-- Migration: 001_initial_schema
-- Created at: 2026-07-21
-- Description: Initial database schema for AI Language Learning Platform MVP

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    avatar VARCHAR(500),
    current_level VARCHAR(10) DEFAULT 'A1',
    has_completed_diagnostic BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_users_level ON users(current_level);

-- Diagnostic results table
CREATE TABLE IF NOT EXISTS diagnostic_results (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    overall_score DECIMAL(5,2),
    speaking_score DECIMAL(5,2),
    listening_score DECIMAL(5,2),
    reading_score DECIMAL(5,2),
    writing_score DECIMAL(5,2),
    vocabulary_score DECIMAL(5,2),
    grammar_score DECIMAL(5,2),
    cefr_level VARCHAR(5),
    task_results JSONB,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_diagnostic_user ON diagnostic_results(user_id);
CREATE INDEX IF NOT EXISTS idx_diagnostic_level ON diagnostic_results(cefr_level);

-- Learning paths table
CREATE TABLE IF NOT EXISTS learning_paths (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE,
    progress DECIMAL(5,2) DEFAULT 0,
    current_level VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_path_user ON learning_paths(user_id);

-- Path nodes table
CREATE TABLE IF NOT EXISTS path_nodes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    path_id UUID NOT NULL,
    node_type VARCHAR(20) NOT NULL, -- vocabulary, dialogue, shadowing, grammar
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'locked', -- locked, available, in_progress, completed
    difficulty VARCHAR(10) DEFAULT 'medium', -- easy, medium, hard
    estimated_minutes INT DEFAULT 15,
    score DECIMAL(5,2),
    completed_at TIMESTAMP,
    order_index INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (path_id) REFERENCES learning_paths(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_node_path ON path_nodes(path_id);
CREATE INDEX IF NOT EXISTS idx_node_status ON path_nodes(status);

-- Practice sessions table
CREATE TABLE IF NOT EXISTS practice_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    node_id UUID,
    session_type VARCHAR(20) NOT NULL, -- dialogue, shadowing
    duration INT DEFAULT 0,
    score DECIMAL(5,2),
    details JSONB,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (node_id) REFERENCES path_nodes(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_session_user ON practice_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_session_node ON practice_sessions(node_id);
CREATE INDEX IF NOT EXISTS idx_session_type ON practice_sessions(session_type);

-- Dialogue messages table
CREATE TABLE IF NOT EXISTS dialogue_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL,
    speaker VARCHAR(10) NOT NULL, -- user, ai
    content TEXT NOT NULL,
    audio_url VARCHAR(500),
    feedback JSONB,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES practice_sessions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_message_session ON dialogue_messages(session_id);

-- Shadowing attempts table
CREATE TABLE IF NOT EXISTS shadowing_attempts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL,
    sentence_id UUID NOT NULL,
    target_text TEXT NOT NULL,
    user_transcript TEXT,
    overall_score DECIMAL(5,2),
    error_details JSONB,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES practice_sessions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_attempt_session ON shadowing_attempts(session_id);

-- User statistics table
CREATE TABLE IF NOT EXISTS user_statistics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    period VARCHAR(10) NOT NULL, -- week, month, all
    total_minutes INT DEFAULT 0,
    total_sessions INT DEFAULT 0,
    total_rounds INT DEFAULT 0,
    average_score DECIMAL(5,2) DEFAULT 0,
    daily_data JSONB,
    skill_trends JSONB,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_period UNIQUE (user_id, period)
);

CREATE INDEX IF NOT EXISTS idx_stats_user ON user_statistics(user_id);

-- Update timestamp trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to tables with updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_learning_paths_updated_at BEFORE UPDATE ON learning_paths
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_statistics_updated_at BEFORE UPDATE ON user_statistics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();