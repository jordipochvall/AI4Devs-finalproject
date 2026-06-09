/** Outcome of evaluating whether the auto-spin loop should continue after a spin. */
export type AutoDecision = 'continue' | 'done' | 'insufficient' | 'threshold'

/**
 * Pure safeguard logic for auto-spin (HU-9). Decides what to do before the next spin given the
 * spins left, the current balance, the bet and an optional "stop if balance below" threshold.
 * Safeguards take precedence over completion so a pause is shown when a limit is crossed.
 *
 * @param remaining       spins still to run (0 means the batch is finished)
 * @param balanceCents    current balance
 * @param betCents        total bet per spin
 * @param stopBelowCents  stop if the balance would be below this (0 disables the check)
 * @returns {@code insufficient}/{@code threshold} to stop with a responsible-gaming pause,
 *          {@code done} to stop quietly, or {@code continue} to spin again
 */
export function nextAutoDecision(remaining: number, balanceCents: number,
                                 betCents: number, stopBelowCents: number): AutoDecision {
  if (balanceCents < betCents) return 'insufficient'           // AC5
  if (stopBelowCents > 0 && balanceCents < stopBelowCents) return 'threshold' // AC3
  if (remaining <= 0) return 'done'                            // AC2
  return 'continue'                                            // AC1
}
