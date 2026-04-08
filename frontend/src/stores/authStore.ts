import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'
import type { User } from '@/types/User'

export const useAuthStore = defineStore('auth', () => {
  // ─── State ────────────────────────────────────────────────────────────────
  const user = ref<User | null>(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref<string | null>(localStorage.getItem('token'))
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // ─── Getters ──────────────────────────────────────────────────────────────
  const isAuthenticated = computed(() => !!token.value)
  const currentUser = computed(() => user.value)

  // ─── Actions ──────────────────────────────────────────────────────────────
  async function login(username: string, password: string): Promise<void> {
    isLoading.value = true
    error.value = null
    try {
      const data = await authApi.login(username, password)
      // 儲存 token 與 user
      token.value = data.accessToken
      user.value = (data.user as User | null) ?? null
      localStorage.setItem('token', data.accessToken)
      localStorage.setItem('user', JSON.stringify(data.user))
    } catch (err) {
      // Rethrow so the view can display the right message
      const e = err as { response?: { data?: { message?: string } }; message?: string }
      error.value = e?.response?.data?.message ?? e.message ?? '登入失敗，請再試一次'
      console.log(err)
    } finally {
      isLoading.value = false
    }
  }

  async function register(
    username: string,
    email: string,
    password: string,
  ): Promise<void> {
    isLoading.value = true
    error.value = null
    try {
      await authApi.register(username, email, password)
    } catch (err) {
      const e = err as { response?: { data?: { message?: string } }; message?: string }
      error.value = e?.response?.data?.message ?? e.message ?? '註冊失敗，請再試一次'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  function logout(): void {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function restoreSession(): Promise<void> {
    if (!token.value) return
    try {
      // ── TODO: replace with real API call ────────────────────────────────
      // const response = await authApi.me(token.value)
      //   headers: { Authorization: `Bearer ${token.value}` },
      // })
      // user.value = response
      // ─────────────────────────────────────────────────────────────────────
    } catch {
      logout()
    }
  }

  return {
    user,
    token,
    isLoading,
    error,
    isAuthenticated,
    currentUser,
    login,
    register,
    logout,
    restoreSession,
  }
})
