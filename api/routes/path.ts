/**
 * 学习路径服务API路由
 */
import { Router, type Response } from 'express'
import db from '../lib/db'
import { authMiddleware, type AuthenticatedRequest } from '../middleware/auth'

const router = Router()

const generatePathNodes = () => {
  return [
    { id: 'n1', pathId: '', nodeType: 'vocabulary', title: '基础词汇', description: '学习基础词汇', status: 'available', difficulty: 'easy', estimatedMinutes: 15, order: 1 },
    { id: 'n2', pathId: '', nodeType: 'dialogue', title: '日常对话', description: '练习日常对话', status: 'locked', difficulty: 'medium', estimatedMinutes: 20, order: 2 },
    { id: 'n3', pathId: '', nodeType: 'shadowing', title: '跟读练习', description: '发音纠正', status: 'locked', difficulty: 'medium', estimatedMinutes: 15, order: 3 }
  ]
}

router.get('/', authMiddleware, async (req: AuthenticatedRequest, res: Response): Promise<void> => {
  try {
    let path = await db.findLearningPathByUserId(req.user!.userId)
    
    if (!path) {
      const nodes = generatePathNodes()
      path = await db.createLearningPath({
        userId: req.user!.userId,
        progress: 0,
        currentLevel: 'B1',
        nodes: nodes as any
      })
    }

    res.json({
      success: true,
      data: {
        pathId: path.id,
        nodes: path.nodes,
        progress: path.progress,
        currentLevel: path.currentLevel
      }
    })
  } catch (error) {
    res.status(500).json({ success: false, error: '获取学习路径失败' })
  }
})

router.get('/daily-tasks', authMiddleware, async (req: AuthenticatedRequest, res: Response): Promise<void> => {
  try {
    res.json({
      success: true,
      data: {
        tasks: [
          { taskId: 't1', nodeId: 'n1', title: '基础词汇练习', type: 'vocabulary', difficulty: 'easy', estimatedMinutes: 15, status: 'pending' },
          { taskId: 't2', nodeId: 'n2', title: '日常对话练习', type: 'dialogue', difficulty: 'medium', estimatedMinutes: 20, status: 'pending' }
        ],
        totalMinutes: 35,
        completedTasks: 0
      }
    })
  } catch (error) {
    res.status(500).json({ success: false, error: '获取今日任务失败' })
  }
})

export default router