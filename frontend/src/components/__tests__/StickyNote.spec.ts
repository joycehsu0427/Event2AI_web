import { mount } from '@vue/test-utils'
import StickyNote from '../StickyNote.vue'
import { describe, it, expect, vi } from 'vitest'

describe('StickyNote.vue', () => {
  it('renders content', () => {
    const wrapper = mount(StickyNote, {
      props: {
        id: 'note-1',
        x: 10,
        y: 20,
        width: 150,
        height: 150,
        content: 'Test content',
        zIndex: 1,
      },
    })
    expect(wrapper.text()).toContain('Test content')
  })

  it('emits delete event when delete button is clicked', async () => {
    const wrapper = mount(StickyNote, {
      props: {
        id: 'note-1',
        x: 10,
        y: 20,
        width: 150,
        height: 150,
        content: 'Test content',
        zIndex: 1,
      },
    })

    await wrapper.find('.delete-button').trigger('mousedown')

    expect(wrapper.emitted().delete).toBeTruthy()
    expect(wrapper.emitted().delete[0]).toEqual(['note-1'])
  })

  it('enters editing mode on double click', async () => {
    const wrapper = mount(StickyNote, {
      props: {
        id: 'note-1',
        x: 10,
        y: 20,
        width: 150,
        height: 150,
        content: 'Test content',
        zIndex: 1,
      },
    })

    expect(wrapper.find('textarea').exists()).toBe(false)
    await wrapper.trigger('dblclick')
    expect(wrapper.find('textarea').exists()).toBe(true)
  })

  it('emits update:content when editing is finished', async () => {
    const wrapper = mount(StickyNote, {
      props: {
        id: 'note-1',
        x: 10,
        y: 20,
        width: 150,
        height: 150,
        content: 'Test content',
        zIndex: 1,
      },
    })

    await wrapper.trigger('dblclick')
    const textarea = wrapper.find('textarea')
    await textarea.setValue('New content')
    await textarea.trigger('blur')

    expect(wrapper.emitted()['update:content']).toBeTruthy()
    expect(wrapper.emitted()['update:content'][0]).toEqual(['note-1', 'New content'])
  })
})
