<template>
  <div
    class="miro-board-container"
    @mousedown="onMouseDown"
    @mouseup="onMouseUp"
    @mousemove="onMouseMove"
    @wheel="onMouseWheel"
  >
    <ToolBar @select-tool="handleSelectTool" :active-tool="currentTool" />

    <div
      class="miro-board"
      :style="{ transform: `translate(${translateX}px, ${translateY}px) scale(${scale})` }"
    >
      <!-- Canvas for drawing -->
      <canvas
        ref="drawingCanvas"
        class="drawing-canvas"
        @mousedown.stop="startDrawing"
        @mousemove="draw"
        @mouseup="stopDrawing"
        @mouseleave="stopDrawing"
        :width="boardWidth"
        :height="boardHeight"
      ></canvas>

      <!-- Sticky Notes -->
      <StickyNote
        v-for="element in boardStore.elements.filter(e => e.type === 'sticky-note')"
        :key="element.id"
        :id="element.id"
        :x="element.x"
        :y="element.y"
        :width="element.width || 150"
        :height="element.height || 150"
        :content="element.content || ''"
        :z-index="element.zIndex"
        @update:position="updateElementPosition"
        @update:content="updateElementContent"
        @update:size="updateElementSize"
        @delete="deleteElement"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ToolBar from './ToolBar.vue'
import StickyNote from './StickyNote.vue'
import { useBoardStore, BoardElement } from '../stores/boardStore'

const boardStore = useBoardStore()

const scale = ref(1)
const translateX = ref(0)
const translateY = ref(0)
const isDraggingBoard = ref(false)
const lastMouseX = ref(0)
const lastMouseY = ref(0)

const currentTool = ref('select')

// Drawing specific refs
const drawingCanvas = ref<HTMLCanvasElement | null>(null)
let ctx: CanvasRenderingContext2D | null = null
const isDrawing = ref(false)
let lastX = 0
let lastY = 0

// Dimensions for the drawing canvas (can be expanded later if the board size needs to be infinite)
const boardWidth = ref(window.innerWidth * 2)
const boardHeight = ref(window.innerHeight * 2)

onMounted(() => {
  if (drawingCanvas.value) {
    ctx = drawingCanvas.value.getContext('2d')
    if (ctx) {
      ctx.strokeStyle = '#000000'
      ctx.lineWidth = 2
      ctx.lineCap = 'round'
      ctx.lineJoin = 'round'
    }
  }
})

const handleSelectTool = (tool: string) => {
  currentTool.value = tool
  document.body.style.cursor = tool === 'select' ? 'grab' : (tool === 'pen' ? 'crosshair' : (tool === 'sticky-note' ? 'copy' : 'default'))
}

const onMouseDown = (event: MouseEvent) => {
  // Check if we clicked on a sticky note
  const clickedNoteElement = (event.target as HTMLElement).closest('.sticky-note') as HTMLElement
  if (clickedNoteElement) {
    const noteId = clickedNoteElement.id // StickyNote element's id is the noteId
    boardStore.bringElementToFront(noteId)
    return // Let the sticky note handle its own drag logic
  }

  // If sticky note tool is active, add a new sticky note
  if (currentTool.value === 'sticky-note' && !(event.target as HTMLElement).closest('.toolbar')) {
    addStickyNote(event.clientX, event.clientY)
    return
  }

  // Only start dragging the board if the 'select' tool is active and not clicking on a toolbar
  if (currentTool.value === 'select' && !(event.target as HTMLElement).closest('.toolbar')) {
    isDraggingBoard.value = true
    lastMouseX.value = event.clientX
    lastMouseY.value = event.clientY
    document.body.style.cursor = 'grabbing'
  }
}

const onMouseUp = () => {
  isDraggingBoard.value = false
  document.body.style.cursor = currentTool.value === 'select' ? 'grab' : (currentTool.value === 'pen' ? 'crosshair' : (currentTool.value === 'sticky-note' ? 'copy' : 'default'))
}

const onMouseMove = (event: MouseEvent) => {
  if (!isDraggingBoard.value) return

  const deltaX = event.clientX - lastMouseX.value
  const deltaY = event.clientY - lastMouseY.value

  translateX.value += deltaX
  translateY.value += deltaY

  lastMouseX.value = event.clientX
  lastMouseY.value = event.clientY
}

const onMouseWheel = (event: WheelEvent) => {
  event.preventDefault()

  const scaleAmount = 1.1
  const mouseX = event.clientX
  const mouseY = event.clientY

  const oldScale = scale.value
  let newScale = scale.value

  if (event.deltaY < 0) {
    newScale *= scaleAmount
  } else {
    newScale /= scaleAmount
  }

  if (newScale < 0.1) newScale = 0.1
  if (newScale > 5) newScale = 5

  scale.value = newScale

  translateX.value = mouseX - (mouseX - translateX.value) * (newScale / oldScale)
  translateY.value = mouseY - (mouseY - translateY.value) * (newScale / oldScale)
}

// Pen tool drawing logic
const startDrawing = (event: MouseEvent) => {
  if (currentTool.value !== 'pen' || !ctx) return
  isDrawing.value = true

  const canvasRect = drawingCanvas.value!.getBoundingClientRect()
  lastX = (event.clientX - canvasRect.left) / scale.value - translateX.value / scale.value
  lastY = (event.clientY - canvasRect.top) / scale.value - translateY.value / scale.value
  ctx.beginPath()
  ctx.moveTo(lastX, lastY)
}

const draw = (event: MouseEvent) => {
  if (!isDrawing.value || currentTool.value !== 'pen' || !ctx) return

  const canvasRect = drawingCanvas.value!.getBoundingClientRect()
  const currentX = (event.clientX - canvasRect.left) / scale.value - translateX.value / scale.value
  const currentY = (event.clientY - canvasRect.top) / scale.value - translateY.value / scale.value

  ctx.lineTo(currentX, currentY)
  ctx.stroke()

  lastX = currentX
  lastY = currentY
}

const stopDrawing = () => {
  isDrawing.value = false
  if (ctx) {
    ctx.closePath()
  }
}

// Sticky Note Logic
const addStickyNote = (clientX: number, clientY: number) => {
  const boardRect = (drawingCanvas.value?.parentElement as HTMLElement).getBoundingClientRect() // Get board div rect
  const x = (clientX - boardRect.left - translateX.value) / scale.value
  const y = (clientY - boardRect.top - translateY.value) / scale.value

  boardStore.addElement({
    type: 'sticky-note',
    x,
    y,
    width: 150, // Default width
    height: 150, // Default height
    content: 'New Note',
  })
}

const updateElementPosition = (id: string, newX: number, newY: number) => {
  boardStore.updateElementPosition(id, newX, newY)
}

const updateElementContent = (id: string, newContent: string) => {
  boardStore.updateElementContent(id, newContent)
}

const updateElementSize = (id: string, newWidth: number, newHeight: number) => {
  boardStore.updateElementSize(id, newWidth, newHeight)
}

const deleteElement = (id: string) => {
  boardStore.deleteElement(id)
}
</script>

<style scoped>
.miro-board-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  position: relative;
  background-color: #f0f0f0;
  /* Cursor managed by JS */
}

.miro-board {
  position: absolute;
  transform-origin: 0 0;
  min-width: 100%;
  min-height: 100%;
}

.drawing-canvas {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1; /* Above board, below toolbar */
}
</style>