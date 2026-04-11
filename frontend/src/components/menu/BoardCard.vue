<script setup lang="ts">
import type { Board } from '@/types/api/board'

defineProps<{ board: Board }>()

const emit = defineEmits<{
  edit: [board: Board]
  delete: [board: Board]
  members: [board: Board]
}>()
</script>

<template>
  <div class="board-card">
    <!-- Thumbnail -->
    <div class="board-thumbnail" :style="{ background: board.color }">
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="1.5">
        <rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/>
        <rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>
      </svg>

      <!-- Action buttons overlay -->
      <div class="card-actions">
        <button class="icon-btn" title="管理成員" @click.stop="emit('members', board)">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </button>
        <button v-if="board.currentUserRole === 'OWNER' || board.currentUserRole === 'EDITOR'" class="icon-btn" title="編輯" @click.stop="emit('edit', board)">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
        </button>
        <button v-if="board.currentUserRole === 'OWNER'" class="icon-btn icon-btn--danger" title="刪除" @click.stop="emit('delete', board)">
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
      <p class="board-owner">被 {{ board.ownerName }} 擁有</p>
    </div>
  </div>
</template>

<style scoped>
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

.board-owner {
  margin: 0;
  font-size: 12px;
  color: #8e8e9a;
}
</style>
