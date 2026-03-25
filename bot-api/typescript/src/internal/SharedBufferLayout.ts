/**
 * SharedArrayBuffer slot indices for synchronization between main thread and bot worker.
 *
 * Layout (Int32Array):
 *   [0] TURN_SIGNAL  — main thread increments and calls Atomics.notify() each tick
 *   [1] STOP_FLAG    — main thread sets to 1 when bot should stop; worker checks on wake
 */
export const SAB_SLOT_TURN = 0;
export const SAB_SLOT_STOP = 1;
export const SAB_LENGTH = 2;
