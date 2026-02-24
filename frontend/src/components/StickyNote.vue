<template>
  <div
    class="sticky-note"
    :style="{ left: `${x}px`, top: `${y}px`, zIndex: zIndex, width: `${width}px`, height: `${height}px` }"
    @mousedown.stop="startDrag"
    @dblclick="startEditing"
  >
    <button class="delete-button" @mousedown.stop.prevent="deleteNote">×</button>
    <textarea
      v-if="isEditing"
      v-model="internalContent"
      ref="textareaRef"
      @blur="stopEditing"
      @keydown.enter.prevent="stopEditing"
    ></textarea>
    <div v-else class="note-content">
      {{ content }}
    </div>
    <div class="resize-handle" @mousedown.stop="startResize"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'

const props = defineProps<{
  id: string
  x: number
  y: number
  width: number
  height: number
  content: string
  zIndex: number
}>()

const emit = defineEmits(['update:position', 'update:content', 'update:size', 'delete'])

const isEditing = ref(false)
const internalContent = ref(props.content)
const textareaRef = ref<HTMLTextAreaElement | null>(null)

watch(() => props.content, (newContent) => {
  internalContent.value = newContent
})

let startDragX = 0
let startDragY = 0
let startNoteX = 0
let startNoteY = 0

const startDrag = (event: MouseEvent) => {
  if (isEditing.value) return

  startDragX = event.clientX
  startDragY = event.clientY
  startNoteX = props.x
  startNoteY = props.y

  document.addEventListener('mousemove', drag)
  document.addEventListener('mouseup', stopDrag)
}

const drag = (event: MouseEvent) => {
  const deltaX = event.clientX - startDragX
  const deltaY = event.clientY - startDragY
  emit('update:position', props.id, startNoteX + deltaX, startNoteY + deltaY)
}

const stopDrag = () => {
  document.removeEventListener('mousemove', drag)
  document.removeEventListener('mouseup', stopDrag)
}

const startEditing = () => {
  isEditing.value = true
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.focus()
      textareaRef.value.select()
    }
  })
}

const stopEditing = () => {
  isEditing.value = false
  if (internalContent.value !== props.content) {
    emit('update:content', props.id, internalContent.value)
  }
}

let startResizeX = 0
let startResizeY = 0
let startNoteWidth = 0
let startNoteHeight = 0

const startResize = (event: MouseEvent) => {
  startResizeX = event.clientX
  startResizeY = event.clientY
  startNoteWidth = props.width
  startNoteHeight = props.height

  document.addEventListener('mousemove', resize)
  document.addEventListener('mouseup', stopResize)
}

const resize = (event: MouseEvent) => {
  const deltaX = event.clientX - startResizeX
  const deltaY = event.clientY - startResizeY

  const newWidth = Math.max(50, startNoteWidth + deltaX) // Minimum width of 50px
  const newHeight = Math.max(50, startNoteHeight + deltaY) // Minimum height of 50px

  emit('update:size', props.id, newWidth, newHeight)
}

const stopResize = () => {
  document.removeEventListener('mousemove', resize)
  document.removeEventListener('mouseup', stopResize)
}

const deleteNote = () => {
  emit('delete', props.id)
}
</script>

<style scoped>
.sticky-note {
  position: absolute;
  background-color: #ffc; /* Classic sticky note yellow */
  border: 1px solid #e0e0a0;
  box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.2);
  padding: 10px;
  font-family: 'Reenie Beanie', cursive; /* A handwriting-like font */
  font-size: 1.2rem;
  line-height: 1.3;
  overflow: auto; /* Scroll if content overflows */
  cursor: grab;
  box-sizing: border-box; /* Include padding and border in element's total width and height */
  display: flex; /* For resize handle positioning */
  flex-direction: column;
  transition: box-shadow 0.2s; /* Add transition for hover effect */
}

.sticky-note:hover {
  box-shadow: 4px 4px 16px rgba(0, 0, 0, 0.3);
}

.sticky-note:active {
  cursor: grabbing;
}

.sticky-note textarea {
  flex-grow: 1; /* Allow textarea to fill available space */
  width: 100%;
  height: 100%; /* Take full height of its container */
  border: none;
  background-color: transparent;
  resize: none;
  font-family: inherit;
  font-size: inherit;
  line-height: inherit;
  outline: none;
  box-sizing: border-box;
}

.note-content {
  flex-grow: 1;
  white-space: pre-wrap; /* Preserve whitespace and allow wrapping */
  word-wrap: break-word; /* Break long words */
}

.resize-handle {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 15px;
  height: 15px;
  background-color: rgba(0, 0, 0, 0.2);
  cursor: nwse-resize;
  border-bottom-right-radius: 5px;
}

.delete-button {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #ff6b6b;
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  line-height: 1;
  opacity: 0; /* Hidden by default */
  transition: opacity 0.2s;
}

.sticky-note:hover .delete-button {
  opacity: 1; /* Show on hover */
}

.delete-button:hover {
  background-color: #ff4d4d;
}
</style>
