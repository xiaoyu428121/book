/**
 * 用户认证API路由
 * 处理用户注册、登录、令牌管理等功能
 */
import { Router, type Request, type Response } from 'express'
import bcrypt from 'bcrypt'
import { v4 as uuidv4 } from 'uuid'
import db from '../lib/db'
import { generateToken, authMiddleware, type AuthenticatedRequest } from '../middleware/auth'
import type { RegisterRequest, LoginRequest, AuthResponse } from '../../shared/types'

const router = Router()

/**
 * 用户注册
 * POST /api/auth/register
 */
router.post('/register', async (req: Request, res: Response): Promise<void> => {
  try {
    const { phone, email, password, nickname } = req.body as RegisterRequest

    // 验证输入
    if (!phone && !email) {
      res.status(400).json({
        success: false,
        error: '手机号或邮箱至少需要提供一个'
      } as AuthResponse)
      return
    }

    if (!password || password.length < 8) {
      res.status(400).json({
        success: false,
        error: '密码至少需要8位字符'
      } as AuthResponse)
      return
    }

    // 检查用户是否已存在
    const existingUser = await db.findUserByEmailOrPhone(email, phone)

    if (existingUser) {
      res.status(400).json({
        success: false,
        error: '该账号已存在'
      } as AuthResponse)
      return
    }

    // 加密密码
    const passwordHash = await bcrypt.hash(password, 10)

    // 创建用户
    const user = await db.createUser({
      email,
      phone,
      passwordHash,
      nickname: nickname || (email?.split('@')[0] || phone?.slice(-4))
    })

    // 生成令牌
    const token = generateToken(user)

    res.status(201).json({
      success: true,
      data: {
        userId: user.id,
        token,
        hasCompletedDiagnostic: user.hasCompletedDiagnostic
      }
    } as AuthResponse)
  } catch (error) {
    console.error('注册失败:', error)
    res.status(500).json({
      success: false,
      error: '注册失败,请稍后重试'
    } as AuthResponse)
  }
})

/**
 * 用户登录
 * POST /api/auth/login
 */
router.post('/login', async (req: Request, res: Response): Promise<void> => {
  try {
    const { account, password } = req.body as LoginRequest

    if (!account || !password) {
      res.status(400).json({
        success: false,
        error: '账号和密码不能为空'
      } as AuthResponse)
      return
    }

    // 查找用户(支持邮箱或手机号登录)
    const user = await db.findUserByEmailOrPhone(
      account.includes('@') ? account : undefined,
      account.includes('@') ? undefined : account
    )

    if (!user) {
      res.status(401).json({
        success: false,
        error: '账号或密码错误'
      } as AuthResponse)
      return
    }

    // 验证密码
    const isValidPassword = await bcrypt.compare(password, user.passwordHash)

    if (!isValidPassword) {
      res.status(401).json({
        success: false,
        error: '账号或密码错误'
      } as AuthResponse)
      return
    }

    // 更新最后登录时间
    await db.updateUser(user.id, { lastLoginAt: new Date() })

    // 生成令牌
    const token = generateToken(user)

    res.json({
      success: true,
      data: {
        userId: user.id,
        token,
        hasCompletedDiagnostic: user.hasCompletedDiagnostic
      }
    } as AuthResponse)
  } catch (error) {
    console.error('登录失败:', error)
    res.status(500).json({
      success: false,
      error: '登录失败,请稍后重试'
    } as AuthResponse)
  }
})

/**
 * 用户登出
 * POST /api/auth/logout
 */
router.post('/logout', authMiddleware, async (req: AuthenticatedRequest, res: Response): Promise<void> => {
  res.json({
    success: true,
    message: '登出成功'
  })
})

/**
 * 获取当前用户信息
 * GET /api/auth/me
 */
router.get('/me', authMiddleware, async (req: AuthenticatedRequest, res: Response): Promise<void> => {
  try {
    const user = await db.findUserById(req.user?.userId || '')

    if (!user) {
      res.status(404).json({
        success: false,
        error: '用户不存在'
      })
      return
    }

    res.json({
      success: true,
      data: {
        id: user.id,
        email: user.email,
        phone: user.phone,
        nickname: user.nickname,
        avatar: user.avatar,
        currentLevel: user.currentLevel,
        hasCompletedDiagnostic: user.hasCompletedDiagnostic,
        createdAt: user.createdAt,
        lastLoginAt: user.lastLoginAt
      }
    })
  } catch (error) {
    console.error('获取用户信息失败:', error)
    res.status(500).json({
      success: false,
      error: '获取用户信息失败'
    })
  }
})

export default router