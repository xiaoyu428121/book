import { type Request, type Response, type NextFunction } from 'express'
import * as jwt from 'jsonwebtoken'
import type { User } from '../../shared/types'

export interface AuthenticatedRequest extends Request {
  user?: {
    userId: string
    email?: string
    phone?: string
  }
}

export const authMiddleware = (req: AuthenticatedRequest, res: Response, next: NextFunction): void => {
  try {
    const token = req.headers.authorization?.replace('Bearer ', '')

    if (!token) {
      res.status(401).json({
        success: false,
        error: '未提供认证令牌'
      })
      return
    }

    const secret = process.env.JWT_SECRET || 'default-secret'
    const decoded = jwt.verify(token, secret) as {
      userId: string
      email?: string
      phone?: string
    }

    req.user = decoded
    next()
  } catch (error) {
    res.status(401).json({
      success: false,
      error: '无效的认证令牌'
    })
  }
}

export const generateToken = (user: Pick<User, 'id' | 'email' | 'phone'>): string => {
  const secret = process.env.JWT_SECRET || 'default-secret'
  const payload = {
    userId: user.id,
    email: user.email,
    phone: user.phone
  }
  
  return (jwt as any).sign(payload, secret, { expiresIn: '7d' })
}