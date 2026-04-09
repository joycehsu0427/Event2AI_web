/**
 * Event Storming Sticky Note Colors
 * Mapping semantic role names to specific hex codes.
 */
export const EVENT_STORMING_COLORS = [
  { name: 'blue', hex: '#a2d2ff', role: 'UseCase' },
  { name: 'green', hex: '#b9fbc0', role: 'Input' },
  { name: 'light_yellow', hex: '#fef9c3', role: 'Aggregate Name' },
  { name: 'yellow', hex: '#ffeb3b', role: 'Actor' },
  { name: 'gray', hex: '#e5e7eb', role: 'Comment' },
  { name: 'orange', hex: '#ffcc80', role: 'Domain Event' },
  { name: 'light_blue', hex: '#caf0f8', role: 'Reactor' },
  { name: 'violet', hex: '#dec9e9', role: 'Policy' },
  { name: 'light_green', hex: '#d8f3dc', role: 'Attributes' },
  { name: 'dark_green', hex: '#74c69d', role: 'Aggregate with Attributes' },
  { name: 'pink', hex: '#ffc2d1', role: 'Method' },
] as const;

/**
 * Utility to get HEX color by Name
 */
export const getHexByName = (name: string): string => {
  const color = EVENT_STORMING_COLORS.find(c => c.name === name);
  return color ? color.hex : '#ffeb3b'; // Default to yellow hex
};

/**
 * Utility to get Name by HEX color
 */
export const getNameByHex = (hex: string): string => {
  const color = EVENT_STORMING_COLORS.find(c => c.hex.toLowerCase() === hex.toLowerCase());
  return color ? color.name : 'yellow'; // Default to yellow name
};

/**
 * List of available color names
 */
export const STICKY_NOTE_COLOR_NAMES = EVENT_STORMING_COLORS.map(c => c.name);

/**
 * List of available color hex codes (for UI selection)
 */
export const STICKY_NOTE_COLOR_PALETTE = EVENT_STORMING_COLORS.map(c => c.hex);

