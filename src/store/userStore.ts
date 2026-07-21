import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { User } from '../../shared/types'

interface AuthState {
  token: string | null
  user: User | null
  isAuthenticated: boolean
  setToken: (token: string | null) => void
  setUser: (user: User | null) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      user: null,
      isAuthenticated: false,
      setToken: (token) => set({ token, isAuthenticated: !!token }),
      setUser: (user) => set({ user }),
      logout: () => set({ token: null, user: null, isAuthenticated: false })
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({ token: state.token })
    }
  )
)