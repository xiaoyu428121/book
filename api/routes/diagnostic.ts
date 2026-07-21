/**
 * 诊断服务API路由
 * 处理入学诊断、能力评估等功能
 */
import { Router, type Response } from 'express'
import { v4 as uuidv4 } from 'uuid'
import db from '../lib/db'
import { authMiddleware, type AuthenticatedRequest } from '../middleware/auth'
import type { DiagnosticTask, SubmitDiagnosticRequest } from '../../shared/types'

const router = Router()

const diagnosticTasks: DiagnosticTask[] = [
  {
    taskId: 'task-1',
    type: 'speech',
    content: {
      prompt: '请朗读以下句子,评估你的发音和流利度',
      target: 'Hello, my name is [your name]. I am a student learning English.'
    },
    order: 1
  },
  {
    taskId: 'task-2',
    type: 'text',
    content: {
      prompt: '选择正确的答案完成句子',
      questions: [
        {
          questionId: 'q1',
          type: 'choice',
          question: 'She ___ to school every day.',
          options: ['go', 'goes', 'going', 'went']
        }
      ]
    },
    order: 2
  }
]

router.get('/tasks', authMiddleware, async (req: AuthenticatedRequest, res: Response): Promise<void> => {
  try {
    const user = await db.findUserById(req.user?.userId || '')
    
    if (user?.hasCompletedDiagnostic) {
      res.json({ success: true, data: { completed: true } })
      return
    }

    res.json({ success: true, data: { completed: false, tasks: diagnosticTasks } })
  } catch (error) {
    res.status(500).json({ success: false, error: '获取诊断任务失败' })
  }
})

router.post('/submit', authMiddleware, async (req: AuthenticatedRequest, res: Response): Promise<void> => {
  try {
    const { taskId, type, response } = req.body as SubmitDiagnosticRequest
    
    await db.createDiagnosticResult({
      userId: req.user!.userId,
      taskResults: { taskId, type, response }
    })

    res.json({
      success: true,
      data: {
        taskScore: 75,
        feedback: '完成良好'
      }
    })
  } catch (error) {
    res.status(500).json({ success: false, error: '提交诊断任务失败' })
  }
})

router.get('/result', authMiddleware, async (req: AuthenticatedRequest, res: Response): Promise<void> => {
  try {
    const profile = {
      overall: 72,
      speaking: 70,
      listening: 75,
      reading: 72,
      writing: 70,
      vocabulary: 68,
      grammar: 75
    }

    await db.updateUser(req.user!.userId, {
      hasCompletedDiagnostic: true,
      currentLevel: 'B1'
    })

    res.json({
      success: true,
      data: {
        profile,
        level: 'B1',
        recommendation: '建议继续加强听说练习'
      }
    })
  } catch (error) {
    res.status(500).json({ success: false, error: '获取诊断结果失败' })
  }
})

export default router