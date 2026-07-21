/**
 * 练习服务API路由
 */
import { Router, type Response } from 'express'
import db from '../lib/db'
import { authMiddleware, type AuthenticatedRequest } from '../middleware/auth'

const router = Router()

router.post('/dialogue/start', authMiddleware, async (req: AuthenticatedRequest, res: Response): Promise<void> => {
  try {
    const session = await db.createPracticeSession({
      userId: req.user!.userId,
      sessionType: 'dialogue',
      duration: 0
    })

    res.json({
      success: true,
      data: {
        sessionId: session.id,
        scene: { name: '自我介绍', description: '练习自我介绍' },
        greeting: 'Hello! Let\'s practice English together.'
      }
    })
  } catch (error) {
    res.status(500).json({ success: false, error: '开始对话失败' })
  }
})

router.post('/dialogue/message', authMiddleware, async (req: AuthenticatedRequest, res: Response): Promise<void> => {
  try {
    const { sessionId, transcript } = req.body
    
    await db.addDialogueMessage(sessionId, {
      sessionId,
      speaker: 'user',
      content: transcript
    })

    const aiResponse = 'That\'s great! Let\'s continue practicing.'
    
    await db.addDialogueMessage(sessionId, {
      sessionId,
      speaker: 'ai',
      content: aiResponse
    })

    res.json({
      success: true,
      data: {
        aiResponse,
        feedback: { suggestions: ['Keep practicing!'] }
      }
    })
  } catch (error) {
    res.status(500).json({ success: false, error: '发送消息失败' })
  }
})

router.post('/dialogue/end', authMiddleware, async (req: AuthenticatedRequest, res: Response): Promise<void> => {
  try {
    const { sessionId } = req.body
    const messages = await db.findDialogueMessagesBySessionId(sessionId)
    const userMessages = messages.filter(m => m.speaker === 'user')

    await db.updatePracticeSession(sessionId, {
      endedAt: new Date(),
      duration: messages.length * 10,
      score: 75,
      details: { rounds: userMessages.length }
    })

    res.json({
      success: true,
      data: {
        rounds: userMessages.length,
        duration: messages.length * 10,
        score: 75,
        vocabulary: ['hello', 'practice'],
        improvements: ['继续练习']
      }
    })
  } catch (error) {
    res.status(500).json({ success: false, error: '结束对话失败' })
  }
})

router.post('/shadowing/start', authMiddleware, async (req: AuthenticatedRequest, res: Response): Promise<void> => {
  try {
    const session = await db.createPracticeSession({
      userId: req.user!.userId,
      sessionType: 'shadowing',
      duration: 0
    })

    res.json({
      success: true,
      data: {
        sessionId: session.id,
        sentence: {
          id: 's1',
          text: 'Hello, nice to meet you.',
          translation: '你好,很高兴见到你。',
          difficulty: 'easy'
        }
      }
    })
  } catch (error) {
    res.status(500).json({ success: false, error: '开始跟读失败' })
  }
})

router.post('/shadowing/submit', authMiddleware, async (req: AuthenticatedRequest, res: Response): Promise<void> => {
  try {
    const { transcript } = req.body

    res.json({
      success: true,
      data: {
        overallScore: 80,
        pronunciation: 82,
        fluency: 78,
        intonation: 80,
        errors: [],
        suggestion: '发音准确,继续保持!'
      }
    })
  } catch (error) {
    res.status(500).json({ success: false, error: '提交录音失败' })
  }
})

router.get('/user/statistics', authMiddleware, async (req: AuthenticatedRequest, res: Response): Promise<void> => {
  try {
    const period = req.query.period as string || 'week'
    
    res.json({
      success: true,
      data: {
        period,
        totalMinutes: 120,
        totalSessions: 8,
        totalRounds: 15,
        averageScore: 75,
        dailyData: [],
        skillTrends: []
      }
    })
  } catch (error) {
    res.status(500).json({ success: false, error: '获取统计数据失败' })
  }
})

export default router