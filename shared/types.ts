// Shared type definitions for AI Language Learning Platform

export interface User {
  id: string;
  email?: string;
  phone?: string;
  nickname?: string;
  avatar?: string;
  currentLevel: string;
  hasCompletedDiagnostic: boolean;
  createdAt: Date;
  lastLoginAt?: Date;
}

export interface DiagnosticResult {
  id: string;
  userId: string;
  overallScore: number;
  speakingScore: number;
  listeningScore: number;
  readingScore: number;
  writingScore: number;
  vocabularyScore: number;
  grammarScore: number;
  cefrLevel: string;
  taskResults: Record<string, unknown>;
  completedAt: Date;
}

export interface LearningPath {
  id: string;
  userId: string;
  progress: number;
  currentLevel: string;
  nodes: PathNode[];
  createdAt: Date;
  updatedAt: Date;
}

export interface PathNode {
  id: string;
  pathId: string;
  nodeType: 'vocabulary' | 'dialogue' | 'shadowing' | 'grammar';
  title: string;
  description?: string;
  status: 'locked' | 'available' | 'in_progress' | 'completed';
  difficulty: 'easy' | 'medium' | 'hard';
  estimatedMinutes: number;
  score?: number;
  completedAt?: Date;
  order: number;
}

export interface PracticeSession {
  id: string;
  userId: string;
  nodeId?: string;
  sessionType: 'dialogue' | 'shadowing';
  duration: number;
  score?: number;
  details?: Record<string, unknown>;
  startedAt: Date;
  endedAt?: Date;
}

export interface DialogueMessage {
  id: string;
  sessionId: string;
  speaker: 'user' | 'ai';
  content: string;
  audioUrl?: string;
  feedback?: {
    grammarErrors?: string[];
    suggestions?: string[];
  };
  timestamp: Date;
}

export interface ShadowingAttempt {
  id: string;
  sessionId: string;
  sentenceId: string;
  targetText: string;
  userTranscript?: string;
  overallScore?: number;
  errorDetails?: {
    word: string;
    expected: string;
    actual: string;
    phonemes: string[];
  }[];
  attemptedAt: Date;
}

export interface UserStatistics {
  id: string;
  userId: string;
  period: 'week' | 'month' | 'all';
  totalMinutes: number;
  totalSessions: number;
  totalRounds: number;
  averageScore: number;
  dailyData?: DailyData[];
  skillTrends?: SkillTrend[];
  updatedAt: Date;
}

export interface DailyData {
  date: string;
  minutes: number;
  sessions: number;
  score: number;
}

export interface SkillTrend {
  date: string;
  speaking: number;
  listening: number;
  reading: number;
  writing: number;
}

// API Request/Response Types
export interface RegisterRequest {
  phone?: string;
  email?: string;
  password: string;
  nickname?: string;
}

export interface LoginRequest {
  account: string;
  password: string;
}

export interface AuthResponse {
  success: boolean;
  data?: {
    userId: string;
    token: string;
    hasCompletedDiagnostic: boolean;
  };
  error?: string;
}

export interface DiagnosticTask {
  taskId: string;
  type: 'speech' | 'text';
  content: {
    prompt: string;
    target?: string;
    questions?: DiagnosticQuestion[];
  };
  order: number;
}

export interface DiagnosticQuestion {
  questionId: string;
  type: 'choice' | 'fill' | 'translate';
  question: string;
  options?: string[];
}

export interface SubmitDiagnosticRequest {
  taskId: string;
  type: 'speech' | 'text';
  response: {
    audioUrl?: string;
    transcript?: string;
    answers?: {
      questionId: string;
      answer: string | string[];
    }[];
  };
}

export interface StartDialogueRequest {
  nodeId: string;
  sceneId: string;
  difficulty: 'easy' | 'medium' | 'hard';
}

export interface DialogueMessageRequest {
  sessionId: string;
  audioUrl: string;
  transcript: string;
}

export interface SubmitShadowingRequest {
  sentenceId: string;
  audioUrl: string;
  transcript: string;
}

export interface DailyTask {
  taskId: string;
  nodeId: string;
  title: string;
  type: 'vocabulary' | 'dialogue' | 'shadowing';
  difficulty: 'easy' | 'medium' | 'hard';
  estimatedMinutes: number;
  status: 'pending' | 'completed';
  score?: number;
}