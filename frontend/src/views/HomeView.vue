<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { Board } from '@/types/board'
import axios from 'axios'
// ─── Mock Data (replace with API calls) ──────────────────────────────────────

const BOARD_COLORS = [
  '#4f46e5', '#0ea5e9', '#10b981', '#f59e0b',
  '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6',
]

function randomColor() {
  return BOARD_COLORS[Math.floor(Math.random() * BOARD_COLORS.length)]
}

const boards = ref<Board[]>([]);
const boardOwner = ref('');

onMounted(async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/boards', {
      headers: { Authorization: `Bearer ${token}` }
    })
    console.log(response.data)
  } catch (error) {
    console.error('API 發生錯誤', error)
  }
})

// ─── Search ───────────────────────────────────────────────────────────────────

const searchQuery = ref('')

const filteredBoards = computed(() =>
  boards.value.filter(b =>
    b.title.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
)

// ─── Create Board ─────────────────────────────────────────────────────────────
/*
const showCreateModal = ref(false)
const createName = ref('')
const createError = ref('')

function openCreateModal() {
  createName.value = ''
  createError.value = ''
  showCreateModal.value = true
}

async function submitCreate() {
  const name = createName.value.trim()
  if (!name) {
    createError.value = '請輸入 board 名稱'
    return
  }

  // TODO: replace with API call
  // const res = await api.post('/boards', { name })
  const newBoard: Board = {
    id: Date.now().toString(),
    name,
    color: randomColor(),
    createdAt: nowISO(),
    updatedAt: nowISO(),
  }
  boards.value.unshift(newBoard)
  showCreateModal.value = false
}
*/
// ─── Edit Board ───────────────────────────────────────────────────────────────
/*
const showEditModal = ref(false)
const editTarget = ref<Board | null>(null)
const editName = ref('')
const editError = ref('')

function openEditModal(board: Board) {
  editTarget.value = board
  editName.value = board.name
  editError.value = ''
  showEditModal.value = true
}

async function submitEdit() {
  const name = editName.value.trim()
  if (!name) {
    editError.value = '請輸入 board 名稱'
    return
  }
  if (!editTarget.value) return

  // TODO: replace with API call
  // await api.patch(`/boards/${editTarget.value.id}`, { name })
  const target = boards.value.find(b => b.id === editTarget.value!.id)
  if (target) {
    target.name = name
    target.updatedAt = nowISO()
  }
  showEditModal.value = false
}
*/
// ─── Delete Board ─────────────────────────────────────────────────────────────
/*
const showDeleteModal = ref(false)
const deleteTarget = ref<Board | null>(null)

function openDeleteModal(board: Board) {
  deleteTarget.value = board
  showDeleteModal.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return

  // TODO: replace with API call
  // await api.delete(`/boards/${deleteTarget.value.id}`)
  boards.value = boards.value.filter(b => b.id !== deleteTarget.value!.id)
  showDeleteModal.value = false
}
*/
// ─── Helpers ──────────────────────────────────────────────────────────────────

function closeOnOverlay(e: MouseEvent, closeFn: () => void) {
  if ((e.target as HTMLElement).classList.contains('modal-overlay')) {
    closeFn()
  }
}
</script>

<template>
  <div class="home">
    <!-- ── Navbar ─────────────────────────────────────────────────────────── -->
    <header class="navbar">
      <div class="navbar-brand">
        <svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect width="28" height="28" rx="6" fill="#4f46e5"/>
          <rect x="6" y="6" width="7" height="7" rx="1.5" fill="white"/>
          <rect x="15" y="6" width="7" height="7" rx="1.5" fill="white" opacity="0.7"/>
          <rect x="6" y="15" width="7" height="7" rx="1.5" fill="white" opacity="0.7"/>
          <rect x="15" y="15" width="7" height="7" rx="1.5" fill="white" opacity="0.5"/>
        </svg>
        <span class="brand-name">Event2AI</span>
      </div>

      <div class="navbar-search">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜尋 board..."
          class="search-input"
        />
      </div>

      <button class="btn-primary" @click="openCreateModal">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M12 5v14M5 12h14"/>
        </svg>
        新增 Board
      </button>
    </header>

    <!-- ── Main Content ───────────────────────────────────────────────────── -->
    <main class="content">
      <div class="section-header">
        <h2 class="section-title">所有 Boards</h2>
        <span class="board-count">{{ filteredBoards.length }} 個</span>
      </div>

      <!-- Empty state -->
      <div v-if="filteredBoards.length === 0" class="empty-state">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#c7c7cd" stroke-width="1.5">
          <rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/>
          <rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>
        </svg>
        <p>{{ searchQuery ? '找不到符合的 board' : '還沒有任何 board，點擊上方按鈕新增' }}</p>
      </div>

      <!-- Board grid -->
      <div v-else class="board-grid">
        <div
          v-for="board in filteredBoards"
          :key="board.id"
          class="board-card"
        >
          <!-- Thumbnail -->
          <div class="board-thumbnail" :style="{ background: board.color }">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="1.5">
              <rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/>
              <rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>
            </svg>

            <!-- Action buttons overlay -->
            <div class="card-actions">
              <button class="icon-btn" title="編輯" @click.stop="openEditModal(board)">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
              </button>
              <button class="icon-btn icon-btn--danger" title="刪除" @click.stop="openDeleteModal(board)">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                  <path d="M10 11v6M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- Info -->
          <div class="board-info">
            <p class="board-name">{{ board.title }}</p>
            <p class="board-owner">被 {{ boardOwner }} 擁有</p>
          </div>
        </div>
      </div>
    </main>

    <!-- ── Create Modal ───────────────────────────────────────────────────── -->
    <Teleport to="body">
      <div
        v-if="showCreateModal"
        class="modal-overlay"
        @click="closeOnOverlay($event, () => showCreateModal = false)"
      >
        <div class="modal">
          <div class="modal-header">
            <h3>新增 Board</h3>
            <button class="close-btn" @click="showCreateModal = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6 6 18M6 6l12 12"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <label class="field-label">Board 名稱</label>
            <input
              v-model="createName"
              type="text"
              class="field-input"
              placeholder="輸入 board 名稱..."
              autofocus
              @keyup.enter="submitCreate"
            />
            <p v-if="createError" class="field-error">{{ createError }}</p>
          </div>
          <div class="modal-footer">
            <button class="btn-ghost" @click="showCreateModal = false">取消</button>
            <button class="btn-primary" @click="submitCreate">建立</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ── Edit Modal ─────────────────────────────────────────────────────── -->
    <Teleport to="body">
      <div
        v-if="showEditModal"
        class="modal-overlay"
        @click="closeOnOverlay($event, () => showEditModal = false)"
      >
        <div class="modal">
          <div class="modal-header">
            <h3>編輯 Board</h3>
            <button class="close-btn" @click="showEditModal = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6 6 18M6 6l12 12"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <label class="field-label">Board 名稱</label>
            <input
              v-model="editName"
              type="text"
              class="field-input"
              placeholder="輸入新名稱..."
              autofocus
              @keyup.enter="submitEdit"
            />
            <p v-if="editError" class="field-error">{{ editError }}</p>
          </div>
          <div class="modal-footer">
            <button class="btn-ghost" @click="showEditModal = false">取消</button>
            <button class="btn-primary" @click="submitEdit">儲存</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ── Delete Confirm Modal ───────────────────────────────────────────── -->
    <Teleport to="body">
      <div
        v-if="showDeleteModal"
        class="modal-overlay"
        @click="closeOnOverlay($event, () => showDeleteModal = false)"
      >
        <div class="modal modal--sm">
          <div class="modal-header">
            <h3>刪除 Board</h3>
            <button class="close-btn" @click="showDeleteModal = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6 6 18M6 6l12 12"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <p class="delete-message">
              確定要刪除 <strong>「{{ deleteTarget?.name }}」</strong> 嗎？此操作無法復原。
            </p>
          </div>
          <div class="modal-footer">
            <button class="btn-ghost" @click="showDeleteModal = false">取消</button>
            <button class="btn-danger" @click="confirmDelete">刪除</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
/* ── Reset / base ─────────────────────────────────────────────────────────── */
*, *::before, *::after { box-sizing: border-box; }

.home {
  min-height: 100vh;
  background: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: #1a1a2e;
}

/* ── Navbar ───────────────────────────────────────────────────────────────── */
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 28px;
  height: 60px;
  background: #ffffff;
  border-bottom: 1px solid #e5e5ea;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.brand-name {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.3px;
  color: #1a1a2e;
}

.navbar-search {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  max-width: 400px;
  background: #f5f5f7;
  border: 1px solid #e5e5ea;
  border-radius: 8px;
  padding: 0 12px;
  color: #8e8e9a;
  transition: border-color .2s;
}

.navbar-search:focus-within {
  border-color: #4f46e5;
  background: #fff;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  height: 36px;
  font-size: 14px;
  color: #1a1a2e;
}

.search-input::placeholder { color: #9999aa; }

/* ── Buttons ──────────────────────────────────────────────────────────────── */
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
  white-space: nowrap;
  transition: background .2s, transform .1s;
}

.btn-primary:hover { background: #4338ca; }
.btn-primary:active { transform: scale(.97); }

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

/* ── Content ──────────────────────────────────────────────────────────────── */
.content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 36px 28px;
}

.section-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 24px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
}

.board-count {
  font-size: 14px;
  color: #8e8e9a;
}

/* ── Board Grid ───────────────────────────────────────────────────────────── */
.board-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.board-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e5ea;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow .2s, transform .2s;
}

.board-card:hover {
  box-shadow: 0 8px 24px rgba(0,0,0,.1);
  transform: translateY(-2px);
}

.board-thumbnail {
  position: relative;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: opacity .15s;
}

.board-card:hover .card-actions {
  opacity: 1;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  background: rgba(255,255,255,.9);
  border: none;
  border-radius: 6px;
  color: #1a1a2e;
  cursor: pointer;
  transition: background .15s;
}

.icon-btn:hover { background: #fff; }

.icon-btn--danger:hover { color: #ef4444; }

.board-info {
  padding: 12px 14px;
}

.board-name {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.board-date {
  margin: 0;
  font-size: 12px;
  color: #8e8e9a;
}

/* ── Empty State ──────────────────────────────────────────────────────────── */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 80px 0;
  color: #9999aa;
  font-size: 15px;
}

/* ── Modal ────────────────────────────────────────────────────────────────── */
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

.modal--sm { width: 360px; }

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
</style>
