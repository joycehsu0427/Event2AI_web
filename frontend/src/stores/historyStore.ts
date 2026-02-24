import { defineStore } from 'pinia';
import { useBoardStore } from './boardStore';
import type { BoardState } from '@/interfaces/board';

interface HistoryStoreState {
  history: BoardState[];
  historyPointer: number;
  maxHistory: number; // Maximum number of history states to keep
  isApplyingHistory: boolean; // Flag to prevent new history states during undo/redo
}

export const useHistoryStore = defineStore('history', {
  state: (): HistoryStoreState => ({
    history: [],
    historyPointer: -1, // -1 means no history yet, 0 is the first state
    maxHistory: 100, // Keep last 100 states
    isApplyingHistory: false,
  }),
  getters: {
    canUndo: (state) => state.historyPointer > 0,
    canRedo: (state) => state.historyPointer < state.history.length - 1,
  },
  actions: {
    /**
     * Adds a new state to the history.
     * Call this action after any significant change to the board state.
     */
    addState() {
      if (this.isApplyingHistory) return; // Do not add state if currently undoing/redoing

      const boardStore = useBoardStore();
      const currentState = boardStore.getCurrentBoardState();

      // If we are not at the end of history (i.e., we've undone some actions),
      // clear all "future" states before adding the new one.
      if (this.historyPointer < this.history.length - 1) {
        this.history.splice(this.historyPointer + 1);
      }

      this.history.push(currentState);
      this.historyPointer = this.history.length - 1;

      // Enforce maxHistory limit
      if (this.history.length > this.maxHistory) {
        this.history.shift(); // Remove the oldest state
        this.historyPointer--; // Adjust pointer since the first element is removed
      }
    },

    /**
     * Undoes the last action by reverting to the previous state.
     */
    undo() {
      if (!this.canUndo) return;

      this.isApplyingHistory = true;
      this.historyPointer--;
      const boardStore = useBoardStore();
      boardStore.loadBoardState(this.history[this.historyPointer]);
      this.isApplyingHistory = false;
    },

    /**
     * Redoes the previously undone action by advancing to the next state.
     */
    redo() {
      if (!this.canRedo) return;

      this.isApplyingHistory = true;
      this.historyPointer++;
      const boardStore = useBoardStore();
      boardStore.loadBoardState(this.history[this.historyPointer]);
      this.isApplyingHistory = false;
    },

    /**
     * Clears all history. Useful for starting a new board or after loading from a file.
     */
    clearHistory() {
      this.history = [];
      this.historyPointer = -1;
    },

    /**
     * Initializes history with the current board state.
     * Should be called once when the board is first loaded or created.
     */
    initializeHistory() {
      this.clearHistory();
      this.addState();
    }
  },
});
