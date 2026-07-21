// 内存数据库存储(用于演示,生产环境应使用真实数据库)
import { v4 as uuidv4 } from 'uuid'

interface User {
  id: string
  email?: string
  phone?: string
  passwordHash: string
  nickname?: string
  avatar?: string
  currentLevel: string
  hasCompletedDiagnostic: boolean
  createdAt: Date
  lastLoginAt?: Date
}

interface DiagnosticResult {
  id: string
  userId: string
  overallScore?: number
  speakingScore?: number
  listeningScore?: number
  readingScore?: number
  writingScore?: number
  vocabularyScore?: number
  grammarScore?: number
  cefrLevel?: string
  taskResults?: any
  completedAt: Date
}

interface PathNode {
  id: string
  pathId: string
  nodeType: string
  title: string
  description?: string
  status: string
  difficulty: string
  estimatedMinutes: number
  score?: number
  completedAt?: Date
  order: number
}

interface LearningPath {
  id: string
  userId: string
  progress: number
  currentLevel: string
  nodes: PathNode[]
  createdAt: Date
  updatedAt: Date
}

interface PracticeSession {
  id: string
  userId: string
  nodeId?: string
  sessionType: string
  duration: number
  score?: number
  details?: any
  startedAt: Date
  endedAt?: Date
}

interface DialogueMessage {
  id: string
  sessionId: string
  speaker: string
  content: string
  audioUrl?: string
  feedback?: any
  timestamp: Date
}

class MemoryDatabase {
  users: Map<string, User> = new Map()
  diagnosticResults: Map<string, DiagnosticResult> = new Map()
  learningPaths: Map<string, LearningPath> = new Map()
  practiceSessions: Map<string, PracticeSession> = new Map()
  dialogueMessages: Map<string, DialogueMessage[]> = new Map()

  // User operations
  async createUser(data: Omit<User, 'id' | 'createdAt' | 'hasCompletedDiagnostic' | 'currentLevel'>): Promise<User> {
    const user: User = {
      ...data,
      id: uuidv4(),
      currentLevel: 'A1',
      hasCompletedDiagnostic: false,
      createdAt: new Date()
    }
    this.users.set(user.id, user)
    return user
  }

  async findUserById(id: string): Promise<User | null> {
    return this.users.get(id) || null
  }

  async findUserByEmailOrPhone(email?: string, phone?: string): Promise<User | null> {
    for (const user of this.users.values()) {
      if (email && user.email === email) return user
      if (phone && user.phone === phone) return user
    }
    return null
  }

  async updateUser(id: string, data: Partial<User>): Promise<User | null> {
    const user = this.users.get(id)
    if (!user) return null
    Object.assign(user, data)
    this.users.set(id, user)
    return user
  }

  // Diagnostic operations
  async createDiagnosticResult(data: Omit<DiagnosticResult, 'id' | 'completedAt'>): Promise<DiagnosticResult> {
    const result: DiagnosticResult = {
      ...data,
      id: uuidv4(),
      completedAt: new Date()
    }
    this.diagnosticResults.set(result.id, result)
    return result
  }

  async findDiagnosticResultsByUserId(userId: string): Promise<DiagnosticResult[]> {
    return Array.from(this.diagnosticResults.values()).filter(r => r.userId === userId)
  }

  async updateDiagnosticResults(userId: string, data: Partial<DiagnosticResult>): Promise<void> {
    for (const result of this.diagnosticResults.values()) {
      if (result.userId === userId) {
        Object.assign(result, data)
      }
    }
  }

  // Learning path operations
  async createLearningPath(data: Omit<LearningPath, 'id' | 'createdAt' | 'updatedAt'>): Promise<LearningPath> {
    const path: LearningPath = {
      ...data,
      id: uuidv4(),
      createdAt: new Date(),
      updatedAt: new Date()
    }
    this.learningPaths.set(path.id, path)
    return path
  }

  async findLearningPathByUserId(userId: string): Promise<LearningPath | null> {
    for (const path of this.learningPaths.values()) {
      if (path.userId === userId) return path
    }
    return null
  }

  // Practice session operations
  async createPracticeSession(data: Omit<PracticeSession, 'id' | 'startedAt'>): Promise<PracticeSession> {
    const session: PracticeSession = {
      ...data,
      id: uuidv4(),
      startedAt: new Date()
    }
    this.practiceSessions.set(session.id, session)
    return session
  }

  async findPracticeSessionById(id: string): Promise<PracticeSession | null> {
    return this.practiceSessions.get(id) || null
  }

  async updatePracticeSession(id: string, data: Partial<PracticeSession>): Promise<PracticeSession | null> {
    const session = this.practiceSessions.get(id)
    if (!session) return null
    Object.assign(session, data)
    this.practiceSessions.set(id, session)
    return session
  }

  // Dialogue messages operations
  async addDialogueMessage(sessionId: string, message: Omit<DialogueMessage, 'id' | 'timestamp'>): Promise<DialogueMessage> {
    const msg: DialogueMessage = {
      ...message,
      id: uuidv4(),
      timestamp: new Date()
    }
    if (!this.dialogueMessages.has(sessionId)) {
      this.dialogueMessages.set(sessionId, [])
    }
    this.dialogueMessages.get(sessionId)!.push(msg)
    return msg
  }

  async findDialogueMessagesBySessionId(sessionId: string): Promise<DialogueMessage[]> {
    return this.dialogueMessages.get(sessionId) || []
  }
}

const db = new MemoryDatabase()
export default db