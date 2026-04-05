import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'
import type { User } from '@/types/user'

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
      const response = await axios.post('http://localhost:8080/api/auth/login', {
        username: username,
        password: password
      })
      // 儲存 token 與 user
      token.value = response.data.accessToken
      user.value = response.data.user ?? null
      localStorage.setItem('token', response.data.accessToken)
      localStorage.setItem('user', JSON.stringify(response.data.user))
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
      const response = await axios.post('http://localhost:8080/api/auth/register', {
        username: username,
        email: email,
        password: password
      })
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
      // const response = await axios.get('/api/auth/me', {
      //   headers: { Authorization: `Bearer ${token.value}` },
      // })
      // user.value = response.data
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
