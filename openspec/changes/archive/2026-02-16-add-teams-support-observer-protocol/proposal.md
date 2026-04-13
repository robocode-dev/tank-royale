# Change: Add Teams Support in Observer Protocol

## Why

Observers (web viewers, GUIs) cannot reliably distinguish team results from solo bot results in `game-ended-event-for-observer`. This creates confusion when displaying battle results and leaderboards.

Current issues identified:
1. No explicit field indicates if a result is for a team or individual bot
2. `rank` values are inconsistent (sometimes 0 for teams, sometimes inherited from bot ranks)
3. Observers must rely on implicit ID sign convention which is not documented in protocol

## What Changes

- Add explicit `isTeam: boolean` field to `results-for-observer.schema.yaml`
- Fix rank calculation to ensure sequential 1..N placement values
- Maintain backward compatibility by making `isTeam` an optional field
- Document the dual identification methods (explicit `isTeam` field and ID sign convention from ADR-0015)

## Impact

- Affected specs: `protocol` (observer message formats)
- Affected code: 
  - `schema/schemas/results-for-observer.schema.yaml`
  - Server-side result generation for observers
  - GUI observer result display logic
- **BREAKING**: None - new optional field maintains backward compatibility
- **Enhancement**: Improves observer UX and result clarity
