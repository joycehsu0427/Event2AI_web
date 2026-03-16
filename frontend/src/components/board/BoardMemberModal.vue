<script setup lang="ts">
import { ref, watch } from 'vue'
import axios from 'axios'
import type { Board } from '@/types/board'
import type { BoardMemberRole } from '@/types/board'
import { useAuthStore } from '@/stores/authStore'

const props = defineProps<{
  modelValue: boolean
  board: Board | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const authStore = useAuthStore()

const email = ref('')
const role = ref<BoardMemberRole>('VIEWER')
const error = ref('')
const successMsg = ref('')
const loading = ref(false)

watch(() => props.modelValue, (open) => {
  if (open) {
    email.value = ''
    role.value = 'VIEWER'
    error.value = ''
    successMsg.value = ''
  }
})

function close() {
  emit('update:modelValue', false)
}

function closeOnOverlay(e: MouseEvent) {
  if ((e.target as HTMLElement).classList.contains('modal-overlay')) close()
}

async function submit() {
  error.value = ''
  successMsg.value = ''

  const trimmed = email.value.trim()
  if (!trimmed) {
    error.value = '請輸入使用者 Email'
    return
  }

  if (!props.board) return
  loading.value = true

  try {
    await axios.post('http://localhost:8080/api/boards/board_member', {
      boardId: props.board.id,
      userEmaail: trimmed,   // note: API spec has typo "userEmaail"
      role: role.value
    }, {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    successMsg.value = `已成功將 ${trimmed} 加入為 ${role.value}`
    email.value = ''
    role.value = 'VIEWER'
  } catch (err: unknown) {
    const axiosErr = err as { response?: { data?: { message?: string } }; message?: string }
    const msg = axiosErr?.response?.data?.message ?? axiosErr?.message ?? '新增失敗，請稍後再試'
    error.value = msg
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <div v-if="modelValue" class="modal-overlay" @click="closeOnOverlay">
      <div class="modal">
        <!-- Header -->
        <div class="modal-header">
          <div class="header-left">
            <div class="header-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </div>
            <div>
              <h3>管理成員</h3>
              <p class="header-sub">{{ board?.title }}</p>
            </div>
          </div>
          <button class="close-btn" @click="close">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6 6 18M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <!-- Body -->
        <div class="modal-body">
          <p class="section-label">新增成員</p>

          <label class="field-label">使用者 Email</label>
          <input
            v-model="email"
            type="email"
            class="field-input"
            placeholder="輸入使用者 email..."
            @keyup.enter="submit"
          />

          <label class="field-label" style="margin-top: 14px;">角色</label>
          <div class="role-selector">
            <button
              class="role-btn"
              :class="{ active: role === 'VIEWER' }"
              @click="role = 'VIEWER'"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
              Viewer
              <span class="role-desc">只能查看</span>
            </button>
            <button
              class="role-btn"
              :class="{ active: role === 'EDITOR' }"
              @click="role = 'EDITOR'"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              Editor
              <span class="role-desc">可以編輯</span>
            </button>
          </div>

          <!-- Feedback -->
          <p v-if="error" class="feedback feedback--error">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/>
            </svg>
            {{ error }}
          </p>
          <p v-if="successMsg" class="feedback feedback--success">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
            {{ successMsg }}
          </p>
        </div>

        <!-- Footer -->
        <div class="modal-footer">
          <button class="btn-ghost" @click="close">關閉</button>
          <button class="btn-primary" :disabled="loading" @click="submit">
            <svg v-if="loading" class="spin" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M21 12a9 9 0 1 1-6.22-8.56"/>
            </svg>
            <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M12 5v14M5 12h14"/>
            </svg>
            {{ loading ? '新增中...' : '新增成員' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
*, *::before, *::after { box-sizing: border-box; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.modal {
  background: #fff;
  border-radius: 14px;
  width: 460px;
  max-width: calc(100vw - 32px);
  box-shadow: 0 20px 60px rgba(0,0,0,.2);
  animation: modal-in .2s ease;
}

@keyframes modal-in {
  from { opacity: 0; transform: scale(.95) translateY(8px); }
  to   { opacity: 1; transform: scale(1)  translateY(0); }
}

/* ── Header ──────────────────────────────────────────────────────────────────── */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f0f5;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.modal-header h3 {
  margin: 0 0 2px;
  font-size: 16px;
  font-weight: 700;
}

.header-sub {
  margin: 0;
  font-size: 12px;
  color: #8e8e9a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 260px;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: #8e8e9a;
  cursor: pointer;
  transition: background .15s, color .15s;
  flex-shrink: 0;
}
.close-btn:hover { background: #f5f5f7; color: #1a1a2e; }

/* ── Body ─────────────────────────────────────────────────────────────────────── */
.modal-body {
  padding: 20px 24px;
}

.section-label {
  margin: 0 0 16px;
  font-size: 13px;
  font-weight: 700;
  color: #1a1a2e;
  text-transform: uppercase;
  letter-spacing: .5px;
}

.field-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #5a5a72;
  margin-bottom: 8px;
}

.field-input {
  width: 100%;
  padding: 10px 12px;
  border: 1.5px solid #e5e5ea;
  border-radius: 8px;
  font-size: 15px;
  outline: none;
  transition: border-color .2s;
  font-family: inherit;
}
.field-input:focus { border-color: #4f46e5; }

/* ── Role selector ───────────────────────────────────────────────────────────── */
.role-selector {
  display: flex;
  gap: 10px;
}

.role-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 10px;
  border: 1.5px solid #e5e5ea;
  border-radius: 10px;
  background: #fff;
  color: #5a5a72;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: border-color .2s, background .2s, color .2s;
}
.role-btn:hover {
  border-color: #4f46e5;
  background: #f5f4ff;
}
.role-btn.active {
  border-color: #4f46e5;
  background: #f5f4ff;
  color: #4f46e5;
}
.role-desc {
  font-size: 11px;
  font-weight: 400;
  color: #9999aa;
}
.role-btn.active .role-desc { color: #6366f1; }

/* ── Feedback ─────────────────────────────────────────────────────────────────── */
.feedback {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 14px 0 0;
  font-size: 13px;
  border-radius: 8px;
  padding: 8px 12px;
}
.feedback--error {
  color: #dc2626;
  background: #fef2f2;
  border: 1px solid #fecaca;
}
.feedback--success {
  color: #16a34a;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
}

/* ── Footer ───────────────────────────────────────────────────────────────────── */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px 20px;
  border-top: 1px solid #f0f0f5;
}

.btn-ghost {
  padding: 8px 16px;
  background: transparent;
  color: #5a5a72;
  border: 1px solid #e5e5ea;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background .2s;
}
.btn-ghost:hover { background: #f5f5f7; }

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  background: #4f46e5;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background .2s, transform .1s;
}
.btn-primary:hover:not(:disabled) { background: #4338ca; }
.btn-primary:active:not(:disabled) { transform: scale(.97); }
.btn-primary:disabled {
  background: #a5b4fc;
  cursor: not-allowed;
}

.spin {
  animation: spin .7s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}
</style>
