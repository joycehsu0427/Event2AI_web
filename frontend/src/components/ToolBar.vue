<template>
  <div class="toolbar">
    <button
      class="tool-button"
      :class="{ active: activeTool === 'select' }"
      @click="selectTool('select')"
    >
      <span class="icon">🔍</span> Select
    </button>
    <button
      class="tool-button"
      :class="{ active: activeTool === 'pen' }"
      @click="selectTool('pen')"
    >
      <span class="icon">✏️</span> Pen
    </button>
    <button
      class="tool-button"
      :class="{ active: activeTool === 'text' }"
      @click="selectTool('text')"
    >
      <span class="icon">📝</span> Text
    </button>
    <button
      class="tool-button"
      :class="{ active: activeTool === 'sticky-note' }"
      @click="selectTool('sticky-note')"
    >
      <span class="icon">🗒️</span> Sticky Note
    </button>
    <!-- Add more tools as needed -->
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  activeTool: string
}>()

const emit = defineEmits(['select-tool'])

const selectTool = (toolName: string) => {
  emit('select-tool', toolName)
}
</script>

<style scoped>
.toolbar {
  position: absolute;
  top: 20px;
  left: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  z-index: 1000; /* Ensure toolbar is above the board */
}

.tool-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 15px;
  border: none;
  background-color: transparent;
  cursor: pointer;
  font-size: 16px;
  color: #333;
  border-radius: 6px;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.tool-button:hover {
  background-color: #f0f0f0;
}

.tool-button.active {
  background-color: #e0e0e0;
  color: #000;
  font-weight: bold;
}

.icon {
  font-size: 20px;
}
</style>
