<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { Board, BoardMemberRole } from '@/types/board'
import axios from 'axios'
import { useAuthStore } from '@/stores/authStore'
import BoardCard from '@/components/board/BoardCard.vue'
import BoardFormModal from '@/components/board/BoardFormModal.vue'
import BoardDeleteModal from '@/components/board/BoardDeleteModal.vue'
import BoardMemberModal from '@/components/board/BoardMemberModal.vue'
// ─── Auth ─────────────────────────────────────────────────────────────────────
const router = useRouter()
const authStore = useAuthStore()
const token = authStore.token

function handleLogout() {
  authStore.logout()
  router.push({ name: 'login' })
}


const BOARD_COLORS = [
  '#4f46e5', '#0ea5e9', '#10b981', '#f59e0b',
  '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6',
]

function randomColor(): string {
  return BOARD_COLORS[Math.floor(Math.random() * BOARD_COLORS.length)] ?? '#4f46e5'
}

const boards = ref<Board[]>([]);

async function getBoardRole(boardId: string): Promise<BoardMemberRole | undefined> {
  try {
    const response = await axios.get(`http://localhost:8080/api/boards/board_member/${boardId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const members: Array<{ boardId: string; userId: string; role: BoardMemberRole }> = Array.isArray(response.data) ? response.data : []
    const currentUserId = authStore.user?.id
    if (!currentUserId) return undefined

    const currentMember = members.find(member => member.userId === currentUserId)
    return currentMember?.role
  } catch (error) {
    console.error('Failed to fetch current user role', error)
    return undefined
  }
}

async function getOwnerName(ownerUserId: string): Promise<string> {
  try {
    const response = await axios.get(`http://localhost:8080/api/users/${ownerUserId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    return response.data.username
  } catch (error) {
    console.error('Failed to fetch owner name', error)
    return 'Unknown'
  }
}

onMounted(async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/boards', {
      headers: { Authorization: `Bearer ${token}` }
    })
    boards.value = response.data
    boards.value.forEach(board => {
      board.color = randomColor()
    })
    await Promise.all(boards.value.map(async (board) => {
      board.ownerName = await getOwnerName(board.ownerUserId)
      board.currentUserRole = await getBoardRole(board.id)
    }))
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
const showCreateModal = ref(false)

function openCreateModal() {
  showCreateModal.value = true
}

async function submitCreate(payload: { title: string; description: string }) {
  const response = await axios.post('http://localhost:8080/api/boards', {
    title: payload.title,
    description: payload.description
  }, {
    headers: { Authorization: `Bearer ${token}` }
  })

  const newBoard: Board = {
    id: response.data.id,
    title: response.data.title,
    description: response.data.description,
    color: randomColor(),
    ownerUserId: response.data.ownerUserId,
    ownerName: await getOwnerName(response.data.ownerUserId),
    currentUserRole: 'OWNER'
  }
  boards.value.unshift(newBoard)
  showCreateModal.value = false
}
// ─── Edit Board ───────────────────────────────────────────────────────────────
const showEditModal = ref(false)
const editTarget = ref<Board | null>(null)

function openEditModal(board: Board) {
  if (board.currentUserRole === 'VIEWER') return
  editTarget.value = board
  showEditModal.value = true
}

async function submitEdit(payload: { title: string; description: string }) {
  if (!editTarget.value) return
  if (editTarget.value.currentUserRole === 'VIEWER') return

  await axios.put(`http://localhost:8080/api/boards/${editTarget.value.id}`, {
    title: payload.title,
    description: payload.description
  }, {
    headers: { Authorization: `Bearer ${token}` }
  })

  const target = boards.value.find(b => b.id === editTarget.value!.id)
  if (target) {
    target.title = payload.title
    target.description = payload.description
  }
  showEditModal.value = false
}
// ─── Delete Board ─────────────────────────────────────────────────────────────
const showDeleteModal = ref(false)
const deleteTarget = ref<Board | null>(null)

function openDeleteModal(board: Board) {
  if (board.currentUserRole === 'VIEWER') return
  deleteTarget.value = board
  showDeleteModal.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  if (deleteTarget.value.currentUserRole === 'VIEWER') return

  await axios.delete(`http://localhost:8080/api/boards/${deleteTarget.value.id}`, {
    headers: { Authorization: `Bearer ${token}` }
  })
  boards.value = boards.value.filter(b => b.id !== deleteTarget.value!.id)
  showDeleteModal.value = false
}
// ─── Board Members ─────────────────────────────────────────────────────────────
const showMemberModal = ref(false)
const memberTarget = ref<Board | null>(null)

function openMemberModal(board: Board) {
  memberTarget.value = board
  showMemberModal.value = true
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

      <button id="btn-create-board" class="btn-primary" @click="openCreateModal">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M12 5v14M5 12h14"/>
        </svg>
        新增 Board
      </button>

      <!-- User & Logout -->
      <div class="navbar-user">
        <div class="user-avatar" :title="authStore.currentUser?.username ?? 'User'">
          {{ (authStore.currentUser?.username ?? 'U').charAt(0).toUpperCase() }}
        </div>
        <button id="btn-logout" class="btn-logout" @click="handleLogout" title="登出">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
          登出
        </button>
      </div>
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
        <BoardCard
          v-for="board in filteredBoards"
          :key="board.id"
          :board="board"
          @members="openMemberModal"
          @edit="openEditModal"
          @delete="openDeleteModal"
        />
      </div>
    </main>

    <!-- ── Modals ────────────────────────────────────────────────────────── -->
    <BoardFormModal v-model="showCreateModal" mode="create" @submit="submitCreate" />
    <BoardFormModal
      v-model="showEditModal"
      mode="edit"
      :initial-title="editTarget?.title"
      :initial-description="editTarget?.description"
      @submit="submitEdit"
    />
    <BoardDeleteModal
      v-model="showDeleteModal"
      :board-title="deleteTarget?.title ?? ''"
      @confirm="confirmDelete"
    />
    <BoardMemberModal
      v-model="showMemberModal"
      :board="memberTarget"
    />
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

/* ── Navbar User / Logout ─────────────────────────────────────────────────── */
.navbar-user {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
  flex-shrink: 0;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: default;
  user-select: none;
}

.btn-logout {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  background: transparent;
  color: #5a5a72;
  border: 1px solid #e5e5ea;
  border-radius: 8px;
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  transition: background .15s, color .15s, border-color .15s;
  white-space: nowrap;
}
.btn-logout:hover {
  background: #fef2f2;
  color: #dc2626;
  border-color: #fecaca;
}

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

</style>
