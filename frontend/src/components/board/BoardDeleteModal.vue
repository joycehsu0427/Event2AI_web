<script setup lang="ts">
const props = defineProps<{
  modelValue: boolean
  boardTitle: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm'): void
}>()

function close() {
  emit('update:modelValue', false)
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
          <h3>刪除 Board</h3>
          <button class="close-btn" @click="close">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6 6 18M6 6l12 12"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <p class="delete-message">
            確定要刪除 <strong>「{{ boardTitle }}」</strong> 嗎？此操作無法復原。
          </p>
        </div>
        <div class="modal-footer">
          <button class="btn-ghost" @click="close">取消</button>
          <button class="btn-danger" @click="emit('confirm')">刪除</button>
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
  width: 360px;
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

.delete-message {
  margin: 0;
  font-size: 15px;
  color: #5a5a72;
  line-height: 1.6;
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

.btn-danger {
  padding: 8px 16px;
  background: #ef4444;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background .2s;
}

.btn-danger:hover { background: #dc2626; }
</style>
