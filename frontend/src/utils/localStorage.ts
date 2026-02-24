// src/utils/localStorage.ts

const LOCAL_STORAGE_KEY = 'miro-board-state';

/**
 * Saves the given state to localStorage.
 * @param state The state object to save.
 */
export function saveStateToLocalStorage(state: any) {
  try {
    const serializedState = JSON.stringify(state);
    localStorage.setItem(LOCAL_STORAGE_KEY, serializedState);
    console.log('Board state saved to localStorage.');
  } catch (error) {
    console.error('Error saving state to localStorage:', error);
  }
}

/**
 * Loads the state from localStorage.
 * @returns The loaded state object, or undefined if no state is found or an error occurs.
 */
export function loadStateFromLocalStorage(): any | undefined {
  try {
    const serializedState = localStorage.getItem(LOCAL_STORAGE_KEY);
    if (serializedState === null) {
      return undefined;
    }
    console.log('Board state loaded from localStorage.');
    return JSON.parse(serializedState);
  } catch (error) {
    console.error('Error loading state from localStorage:', error);
    return undefined;
  }
}

/**
 * Clears the saved state from localStorage.
 */
export function clearLocalStorage() {
  try {
    localStorage.removeItem(LOCAL_STORAGE_KEY);
    console.log('Board state cleared from localStorage.');
  } catch (error) {
    console.error('Error clearing state from localStorage:', error);
  }
}
