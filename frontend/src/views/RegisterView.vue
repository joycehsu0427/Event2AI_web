<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const errors = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  general: '',
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const isSubmitting = ref(false)

function validate(): boolean {
  errors.username = ''
  errors.email = ''
  errors.password = ''
  errors.confirmPassword = ''
  errors.general = ''

  if (!form.username.trim()) {
    errors.username = '請輸入使用者名稱'
    return false
  }
  if (form.username.trim().length < 3) {
    errors.username = '使用者名稱至少需要 3 個字元'
    return false
  }
  if (!form.email.trim()) {
    errors.email = '請輸入電子郵件'
    return false
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = '請輸入有效的電子郵件格式'
    return false
  }
  if (!form.password) {
    errors.password = '請輸入密碼'
    return false
  }
  if (form.password.length < 8) {
    errors.password = '密碼至少需要 8 個字元'
    return false
  }
  if (form.password !== form.confirmPassword) {
    errors.confirmPassword = '兩次輸入的密碼不一致'
    return false
  }
  return true
}

async function handleSubmit() {
  if (!validate()) return
  isSubmitting.value = true
  errors.general = ''
  try {
    await authStore.register(form.username, form.email, form.password)
    router.push({ name: 'home' })
  } catch {
    errors.general = authStore.error ?? '註冊失敗，請再試一次'
  } finally {
    isSubmitting.value = false
  }
}

function getPasswordStrength(pw: string): { level: number; label: string; color: string } {
  if (!pw) return { level: 0, label: '', color: '' }
  let score = 0
  if (pw.length >= 8) score++
  if (pw.length >= 12) score++
  if (/[A-Z]/.test(pw)) score++
  if (/[0-9]/.test(pw)) score++
  if (/[^A-Za-z0-9]/.test(pw)) score++
  if (score <= 1) return { level: 1, label: '弱', color: '#ef4444' }
  if (score <= 3) return { level: 2, label: '中', color: '#f59e0b' }
  return { level: 3, label: '強', color: '#10b981' }
}
</script>

<template>
  <div class="auth-page">
    <!-- Background decoration -->
    <div class="bg-orb bg-orb--1"></div>
    <div class="bg-orb bg-orb--2"></div>

    <div class="auth-card">
      <!-- Logo -->
      <div class="auth-logo">
        <svg width="36" height="36" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect width="28" height="28" rx="6" fill="#4f46e5"/>
          <rect x="6" y="6" width="7" height="7" rx="1.5" fill="white"/>
          <rect x="15" y="6" width="7" height="7" rx="1.5" fill="white" opacity="0.7"/>
          <rect x="6" y="15" width="7" height="7" rx="1.5" fill="white" opacity="0.7"/>
          <rect x="15" y="15" width="7" height="7" rx="1.5" fill="white" opacity="0.5"/>
        </svg>
        <span class="brand-name">Event2AI</span>
      </div>

      <h1 class="auth-title">建立帳號</h1>
      <p class="auth-subtitle">加入 Event2AI，開始管理您的 Boards</p>

      <form class="auth-form" @submit.prevent="handleSubmit" novalidate>
        <!-- General Error -->
        <div v-if="errors.general" class="alert-error">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          {{ errors.general }}
        </div>

        <!-- Username -->
        <div class="field">
          <label class="field-label" for="reg-username">使用者名稱</label>
          <div class="input-wrapper" :class="{ 'input-error': errors.username }">
            <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
            </svg>
            <input
              id="reg-username"
              v-model="form.username"
              type="text"
              class="field-input"
              placeholder="您的名稱"
              autocomplete="username"
            />
          </div>
          <p v-if="errors.username" class="field-error">{{ errors.username }}</p>
        </div>

        <!-- Email -->
        <div class="field">
          <label class="field-label" for="reg-email">電子郵件</label>
          <div class="input-wrapper" :class="{ 'input-error': errors.email }">
            <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="2" y="4" width="20" height="16" rx="2"/><path d="m22 7-10 7L2 7"/>
            </svg>
            <input
              id="reg-email"
              v-model="form.email"
              type="email"
              class="field-input"
              placeholder="you@example.com"
              autocomplete="email"
            />
          </div>
          <p v-if="errors.email" class="field-error">{{ errors.email }}</p>
        </div>

        <!-- Password -->
        <div class="field">
          <label class="field-label" for="reg-password">密碼</label>
          <div class="input-wrapper" :class="{ 'input-error': errors.password }">
            <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <input
              id="reg-password"
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="field-input"
              placeholder="至少 8 個字元"
              autocomplete="new-password"
            />
            <button
              type="button"
              class="toggle-pw"
              @click="showPassword = !showPassword"
              :aria-label="showPassword ? '隱藏密碼' : '顯示密碼'"
            >
              <svg v-if="!showPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </button>
          </div>
          <!-- Password strength meter -->
          <div v-if="form.password" class="pw-strength">
            <div class="pw-bars">
              <span
                v-for="i in 3"
                :key="i"
                class="pw-bar"
                :style="{
                  background: i <= getPasswordStrength(form.password).level
                    ? getPasswordStrength(form.password).color
                    : '#e5e5ea'
                }"
              ></span>
            </div>
            <span class="pw-label" :style="{ color: getPasswordStrength(form.password).color }">
              {{ getPasswordStrength(form.password).label }}
            </span>
          </div>
          <p v-if="errors.password" class="field-error">{{ errors.password }}</p>
        </div>

        <!-- Confirm Password -->
        <div class="field">
          <label class="field-label" for="reg-confirm-password">確認密碼</label>
          <div class="input-wrapper" :class="{ 'input-error': errors.confirmPassword }">
            <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <input
              id="reg-confirm-password"
              v-model="form.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              class="field-input"
              placeholder="再次輸入密碼"
              autocomplete="new-password"
            />
            <button
              type="button"
              class="toggle-pw"
              @click="showConfirmPassword = !showConfirmPassword"
              :aria-label="showConfirmPassword ? '隱藏密碼' : '顯示密碼'"
            >
              <svg v-if="!showConfirmPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </button>
          </div>
          <p v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</p>
        </div>

        <!-- Submit -->
        <button
          type="submit"
          class="btn-submit"
          :class="{ 'btn-loading': isSubmitting }"
          :disabled="isSubmitting"
        >
          <svg v-if="isSubmitting" class="spinner" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
          </svg>
          {{ isSubmitting ? '建立中...' : '建立帳號' }}
        </button>
      </form>

      <p class="auth-switch">
        已有帳號？
        <router-link :to="{ name: 'login' }" class="auth-link">立即登入</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
*, *::before, *::after { box-sizing: border-box; }

/* ── Page ────────────────────────────────────────────────────────────────── */
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  position: relative;
  overflow: hidden;
  padding: 24px;
}

/* ── Background Orbs ─────────────────────────────────────────────────────── */
.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}
.bg-orb--1 {
  width: 500px; height: 500px;
  background: rgba(79, 70, 229, 0.12);
  top: -120px; left: -100px;
}
.bg-orb--2 {
  width: 400px; height: 400px;
  background: rgba(16, 185, 129, 0.1);
  bottom: -80px; right: -80px;
}

/* ── Card ────────────────────────────────────────────────────────────────── */
.auth-card {
  position: relative;
  z-index: 1;
  background: #ffffff;
  border-radius: 20px;
  padding: 36px 44px;
  width: 100%;
  max-width: 460px;
  box-shadow: 0 8px 40px rgba(0,0,0,.10), 0 1px 3px rgba(0,0,0,.06);
  animation: card-in .35s cubic-bezier(.22,1,.36,1);
}

@keyframes card-in {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ── Logo ────────────────────────────────────────────────────────────────── */
.auth-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
}
.brand-name {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  letter-spacing: -0.3px;
}

/* ── Titles ──────────────────────────────────────────────────────────────── */
.auth-title {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 800;
  color: #1a1a2e;
  letter-spacing: -0.5px;
}
.auth-subtitle {
  margin: 0 0 24px;
  font-size: 14px;
  color: #8e8e9a;
}

/* ── Alert ───────────────────────────────────────────────────────────────── */
.alert-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 10px;
  color: #dc2626;
  font-size: 13.5px;
  margin-bottom: 12px;
}

/* ── Fields ──────────────────────────────────────────────────────────────── */
.auth-form { display: flex; flex-direction: column; gap: 16px; }

.field { display: flex; flex-direction: column; gap: 6px; }

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: #3a3a4a;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  border: 1.5px solid #e5e5ea;
  border-radius: 10px;
  background: #fafafa;
  transition: border-color .2s, background .2s, box-shadow .2s;
}
.input-wrapper:focus-within {
  border-color: #4f46e5;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(79,70,229,.12);
}
.input-wrapper.input-error { border-color: #f87171; }
.input-wrapper.input-error:focus-within { box-shadow: 0 0 0 3px rgba(248,113,113,.15); }

.input-icon {
  flex-shrink: 0;
  margin-left: 12px;
  color: #aaa;
  pointer-events: none;
}

.field-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  padding: 11px 12px;
  font-size: 14.5px;
  color: #1a1a2e;
}
.field-input::placeholder { color: #bbb; }

.toggle-pw {
  display: flex;
  align-items: center;
  padding: 0 12px;
  height: 100%;
  background: none;
  border: none;
  cursor: pointer;
  color: #aaa;
  transition: color .15s;
}
.toggle-pw:hover { color: #4f46e5; }

.field-error {
  margin: 0;
  font-size: 12px;
  color: #dc2626;
}

/* ── Password strength ───────────────────────────────────────────────────── */
.pw-strength {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
}
.pw-bars {
  display: flex;
  gap: 4px;
  flex: 1;
}
.pw-bar {
  flex: 1;
  height: 4px;
  border-radius: 99px;
  transition: background .3s;
}
.pw-label {
  font-size: 11px;
  font-weight: 600;
  min-width: 20px;
  text-align: right;
}

/* ── Submit Button ───────────────────────────────────────────────────────── */
.btn-submit {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 13px;
  margin-top: 4px;
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  letter-spacing: 0.1px;
  transition: opacity .2s, transform .1s, box-shadow .2s;
  box-shadow: 0 4px 14px rgba(79,70,229,.35);
}
.btn-submit:hover:not(:disabled) {
  opacity: .92;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(79,70,229,.4);
}
.btn-submit:active:not(:disabled) { transform: scale(.98); }
.btn-submit:disabled { opacity: .7; cursor: not-allowed; }

/* ── Spinner ─────────────────────────────────────────────────────────────── */
.spinner { animation: spin .7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Switch Link ─────────────────────────────────────────────────────────── */
.auth-switch {
  margin: 24px 0 0;
  text-align: center;
  font-size: 13.5px;
  color: #8e8e9a;
}
.auth-link {
  color: #4f46e5;
  font-weight: 600;
  text-decoration: none;
  transition: color .15s;
}
.auth-link:hover { color: #4338ca; text-decoration: underline; }
</style>
