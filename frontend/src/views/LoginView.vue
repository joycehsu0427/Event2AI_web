<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  username: '',
  password: '',
})

const errors = reactive({
  username: '',
  password: '',
  general: '',
})

const showPassword = ref(false)
const isSubmitting = ref(false)

function validate(): boolean {
  errors.username = ''
  errors.password = ''
  errors.general = ''

  if (!form.username.trim()) {
    errors.username = '請輸入使用者名稱'
    return false
  }
  if (!form.password) {
    errors.password = '請輸入密碼'
    return false
  }
  return true
}

async function handleSubmit() {
  if (!validate()) return
  isSubmitting.value = true
  errors.general = ''
  try {
    await authStore.login(form.username, form.password)
    router.push({ name: 'home' })
  } catch {
    errors.general = authStore.error ?? '登入失敗，請再試一次'
  } finally {
    isSubmitting.value = false
  }
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

      <h1 class="auth-title">歡迎回來</h1>
      <p class="auth-subtitle">登入您的帳號，繼續使用 Event2AI</p>

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
          <label class="field-label" for="login-username">使用者名稱</label>
          <div class="input-wrapper" :class="{ 'input-error': errors.username }">
            <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
            </svg>
            <input
              id="login-username"
              v-model="form.username"
              type="text"
              class="field-input"
              placeholder="輸入使用者名稱"
              autocomplete="username"
            />
          </div>
          <p v-if="errors.username" class="field-error">{{ errors.username }}</p>
        </div>

        <!-- Password -->
        <div class="field">
          <div class="field-label-row">
            <label class="field-label" for="login-password">密碼</label>
          </div>
          <div class="input-wrapper" :class="{ 'input-error': errors.password }">
            <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <input
              id="login-password"
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="field-input"
              placeholder="輸入您的密碼"
              autocomplete="current-password"
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
          <p v-if="errors.password" class="field-error">{{ errors.password }}</p>
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
          {{ isSubmitting ? '登入中...' : '登入' }}
        </button>
      </form>

      <p class="auth-switch">
        還沒有帳號？
        <router-link :to="{ name: 'register' }" class="auth-link">立即註冊</router-link>
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
  background: rgba(14, 165, 233, 0.1);
  bottom: -80px; right: -80px;
}

/* ── Card ────────────────────────────────────────────────────────────────── */
.auth-card {
  position: relative;
  z-index: 1;
  background: #ffffff;
  border-radius: 20px;
  padding: 40px 44px;
  width: 100%;
  max-width: 440px;
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
  margin-bottom: 28px;
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
  margin: 0 0 28px;
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
  margin-bottom: 18px;
}

/* ── Fields ──────────────────────────────────────────────────────────────── */
.auth-form { display: flex; flex-direction: column; gap: 18px; }

.field { display: flex; flex-direction: column; gap: 6px; }

.field-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

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
