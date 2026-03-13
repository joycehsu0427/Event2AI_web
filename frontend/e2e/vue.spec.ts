import { test, expect, type Page } from '@playwright/test'

type BoardElement = {
  id: string
  type: 'stickyNote' | 'text'
  x: number
  y: number
  text: string
}

type BoardStoreSnapshot = {
  editingElementId: string | null
  elements: BoardElement[]
}

type ScreenPoint = {
  x: number
  y: number
}

const readBoardStore = async (page: Page): Promise<BoardStoreSnapshot> =>
  page.evaluate(() => {
    const component = document.querySelector('.board-view') as {
      __vueParentComponent?: {
        setupState: {
          boardStore: {
            editingElementId: string | null
            elements: Array<{ id: string; type: 'stickyNote' | 'text'; x: number; y: number; text: string }>
          }
        }
      }
    } | null

    const boardStore = component?.__vueParentComponent?.setupState.boardStore
    return {
      editingElementId: boardStore?.editingElementId ?? null,
      elements:
        boardStore?.elements.map((element) => ({
          id: element.id,
          type: element.type,
          x: element.x,
          y: element.y,
          text: element.text,
        })) ?? [],
    }
  })

const getElementScreenCenter = async (page: Page, elementId: string): Promise<ScreenPoint> =>
  page.evaluate((targetElementId) => {
    const boardComponent = document.querySelector('.miro-board') as {
      __vueParentComponent?: {
        setupState: {
          stageRef?: {
            getStage: () => {
              findOne: (selector: string) => {
                getClientRect: (options?: { skipTransform?: boolean }) => {
                  x: number
                  y: number
                  width: number
                  height: number
                }
              } | null
            }
          }
        }
      }
    } | null

    const stage = boardComponent?.__vueParentComponent?.setupState.stageRef?.getStage()
    const node = stage?.findOne(`#${targetElementId}`)
    if (!node) {
      throw new Error(`Element ${targetElementId} not found on stage`)
    }

    const rect = node.getClientRect({ skipTransform: false })
    return {
      x: rect.x + rect.width / 2,
      y: rect.y + rect.height / 2,
    }
  }, elementId)

const dragFromScreenPoints = async (
  page: Page,
  from: ScreenPoint,
  to: ScreenPoint,
) => {
  await page.mouse.move(from.x, from.y)
  await page.mouse.down()
  await page.mouse.move(to.x, to.y, { steps: 16 })
  await page.waitForTimeout(50)
  await page.mouse.up()
}

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('token', 'playwright-token')
    localStorage.removeItem('miro-board-state')
  })
})

test('sticky note supports editing and dragging after commit', async ({ page }) => {
  await page.goto('/board')
  await page.waitForSelector('.board-view')
  await page.getByRole('button', { name: 'Sticky' }).click()

  const beforeEdit = await readBoardStore(page)
  const sticky = beforeEdit.elements.find((element) => element.type === 'stickyNote')
  expect(sticky).toBeDefined()

  const stickyCenter = await getElementScreenCenter(page, sticky!.id)
  await page.mouse.dblclick(stickyCenter.x, stickyCenter.y)

  const textarea = page.locator('.board-text-editor__textarea')
  await expect(textarea).toBeVisible()
  await expect(textarea).toBeFocused()
  await textarea.fill('Edited Sticky Note')
  await textarea.press('Enter')
  await expect(textarea).toBeHidden()

  const afterCommit = await readBoardStore(page)
  expect(afterCommit.editingElementId).toBeNull()
  expect(afterCommit.elements.find((element) => element.id === sticky!.id)?.text).toBe('Edited Sticky Note')

  const stickyDragStart = await getElementScreenCenter(page, sticky!.id)
  await dragFromScreenPoints(
    page,
    stickyDragStart,
    { x: stickyDragStart.x + 140, y: stickyDragStart.y + 70 },
  )

  const afterDrag = await readBoardStore(page)
  const movedSticky = afterDrag.elements.find((element) => element.id === sticky!.id)
  expect(movedSticky?.x).not.toBe(sticky!.x)
  expect(movedSticky?.y).not.toBe(sticky!.y)
})

test('text element supports cancel, blur save, and dragging after edit', async ({ page }) => {
  await page.goto('/board')
  await page.waitForSelector('.board-view')
  await page.getByRole('button', { name: 'Text' }).click()

  const initialState = await readBoardStore(page)
  const textElement = initialState.elements.find((element) => element.type === 'text')
  expect(textElement).toBeDefined()

  const textCenter = await getElementScreenCenter(page, textElement!.id)
  await page.mouse.dblclick(textCenter.x, textCenter.y)

  const textarea = page.locator('.board-text-editor__textarea')
  await expect(textarea).toBeVisible()
  await textarea.fill('Cancelled Text')
  await textarea.press('Escape')
  await expect(textarea).toBeHidden()

  const afterCancel = await readBoardStore(page)
  expect(afterCancel.editingElementId).toBeNull()
  expect(afterCancel.elements.find((element) => element.id === textElement!.id)?.text).toBe('New Text')

  const textCenterAfterCancel = await getElementScreenCenter(page, textElement!.id)
  await page.mouse.dblclick(textCenterAfterCancel.x, textCenterAfterCancel.y)
  await expect(textarea).toBeVisible()
  await textarea.fill('Committed Text')
  await page.mouse.click(20, 220)
  await expect(textarea).toBeHidden()

  const afterBlurCommit = await readBoardStore(page)
  expect(afterBlurCommit.editingElementId).toBeNull()
  expect(afterBlurCommit.elements.find((element) => element.id === textElement!.id)?.text).toBe('Committed Text')

  const textDragStart = await getElementScreenCenter(page, textElement!.id)
  await dragFromScreenPoints(
    page,
    textDragStart,
    { x: textDragStart.x + 140, y: textDragStart.y + 100 },
  )

  const afterDrag = await readBoardStore(page)
  const movedText = afterDrag.elements.find((element) => element.id === textElement!.id)
  expect(movedText?.x).not.toBe(textElement!.x)
  expect(movedText?.y).not.toBe(textElement!.y)
})
