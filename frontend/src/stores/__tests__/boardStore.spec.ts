import { setActivePinia, createPinia } from 'pinia'
import { useBoardStore } from '../boardStore'
import { describe, it, expect, beforeEach } from 'vitest'

describe('Board Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('adds a new element', () => {
    const boardStore = useBoardStore()
    expect(boardStore.elements.length).toBe(0)

    boardStore.addElement({
      type: 'sticky-note',
      x: 10,
      y: 20,
      content: 'Test Note',
      width: 150,
      height: 150,
    })

    expect(boardStore.elements.length).toBe(1)
    const element = boardStore.elements[0]
    expect(element.type).toBe('sticky-note')
    expect(element.x).toBe(10)
    expect(element.y).toBe(20)
    expect(element.content).toBe('Test Note')
    expect(element.width).toBe(150)
    expect(element.height).toBe(150)
    expect(element.id).toBe('sticky-note-1')
    expect(element.zIndex).toBe(1)
  })

  it('updates an element position', () => {
    const boardStore = useBoardStore()
    boardStore.addElement({ type: 'sticky-note', x: 0, y: 0, content: '' })

    const elementId = boardStore.elements[0].id
    boardStore.updateElementPosition(elementId, 100, 200)

    expect(boardStore.elements[0].x).toBe(100)
    expect(boardStore.elements[0].y).toBe(200)
  })

  it('updates an element content', () => {
    const boardStore = useBoardStore()
    boardStore.addElement({ type: 'sticky-note', x: 0, y: 0, content: 'Initial' })

    const elementId = boardStore.elements[0].id
    boardStore.updateElementContent(elementId, 'Updated')

    expect(boardStore.elements[0].content).toBe('Updated')
  })

  it('updates an element size', () => {
    const boardStore = useBoardStore()
    boardStore.addElement({ type: 'sticky-note', x: 0, y: 0, content: '', width: 100, height: 100 })

    const elementId = boardStore.elements[0].id
    boardStore.updateElementSize(elementId, 200, 300)

    expect(boardStore.elements[0].width).toBe(200)
    expect(boardStore.elements[0].height).toBe(300)
  })

  it('deletes an element', () => {
    const boardStore = useBoardStore()
    boardStore.addElement({ type: 'sticky-note', x: 0, y: 0, content: '' })
    expect(boardStore.elements.length).toBe(1)

    const elementId = boardStore.elements[0].id
    boardStore.deleteElement(elementId)

    expect(boardStore.elements.length).toBe(0)
  })

  it('brings an element to the front', () => {
    const boardStore = useBoardStore()
    boardStore.addElement({ type: 'sticky-note', x: 0, y: 0, content: '' })
    boardStore.addElement({ type: 'sticky-note', x: 0, y: 0, content: '' })

    const firstElement = boardStore.elements[0]
    const secondElement = boardStore.elements[1]
    expect(firstElement.zIndex).toBeLessThan(secondElement.zIndex)

    boardStore.bringElementToFront(firstElement.id)

    expect(firstElement.zIndex).toBeGreaterThan(secondElement.zIndex)
  })
})
