import { defineStore } from 'pinia';

interface TimerStoreState {
  intervalId: ReturnType<typeof setInterval> | null;
  isRunning: boolean;
}

export const useTimerStore = defineStore('timer', {
  state: (): TimerStoreState => ({
    intervalId: null,
    isRunning: false,
  }),
  actions: {
    start(callback: () => void | Promise<void>, intervalMs: number) {
      this.stop();
      this.intervalId = setInterval(() => {
        void callback();
      }, intervalMs);
      this.isRunning = true;
    },

    stop() {
      if (this.intervalId) {
        clearInterval(this.intervalId);
        this.intervalId = null;
      }
      this.isRunning = false;
    },
  },
});
