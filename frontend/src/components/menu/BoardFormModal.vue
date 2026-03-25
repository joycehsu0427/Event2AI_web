<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  initialTitle?: string
  initialDescription?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'submit', payload: { title: string; description: string }): void
}>()

const title = ref('')
const description = ref('')
const error = ref('')

watch(() => props.modelValue, (open) => {
  if (open) {
    title.value = props.initialTitle ?? ''
    description.value = props.initialDescription ?? ''
    error.value = ''
  }
})

function close() {
  emit('update:modelValue', false)
}

function submit() {
  const trimmed = title.value.trim()
  if (!trimmed) {
    error.value = '請輸入 board 名稱'
    return
  }
  emit('submit', { title: trimmed, description: description.value.trim() })
}

function closeOnOverlay(e: MouseEvent) {
  if ((e.target as HTMLElement).classList.contains('modal-overlay')) close()
}
</script>

<template>
  <Teleport to="body">
    <div v-if="modelValue" class="modal-overlay" @click="closeOnOverlay">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ mode === 'create' ? '新增 Board' : '編輯 Board' }}</h3>
          <button class="close-btn" @click="close">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6 6 18M6 6l12 12"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <label class="field-label">Board 名稱</label>
          <input
            v-model="title"
            type="text"
            class="field-input"
            :placeholder="mode === 'create' ? '輸入 board 名稱...' : '輸入新名稱...'"
            autofocus
            @keyup.enter="submit"
          />
          <label class="field-label" style="margin-top: 12px;">描述（選填）</label>
          <textarea
            v-model="description"
            class="field-input"
            placeholder="輸入 board 描述..."
            rows="3"
            style="resize: vertical;"
          />
          <p v-if="error" class="field-error">{{ error }}</p>
        </div>
        <div class="modal-footer">
          <button class="btn-ghost" @click="close">取消</button>
          <button class="btn-primary" @click="submit">
            {{ mode === 'create' ? '建立' : '儲存' }}
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
  width: 440px;
  max-width: calc(100vw - 32px);
  box-shadow: 0 20px 60px rgba(0,0,0,.2);
  animation: modal-in .2s ease;
}

@keyframes modal-in {
  from { opacity: 0; transform: scale(.95) translateY(8px); }
  to   { opacity: 1; transform: scale(1)  translateY(0); }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f0f5;
}

.modal-header h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
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
}

.close-btn:hover { background: #f5f5f7; color: #1a1a2e; }

.modal-body {
  padding: 20px 24px;
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
}

.field-input:focus { border-color: #4f46e5; }

.field-error {
  margin: 8px 0 0;
  font-size: 13px;
  color: #ef4444;
}

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
  padding: 8px 16px;
  background: #4f46e5;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background .2s, transform .1s;
}

.btn-primary:hover { background: #4338ca; }
.btn-primary:active { transform: scale(.97); }
</style>
