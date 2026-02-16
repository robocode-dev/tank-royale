## 1. Schema Updates

- [x] 1.1 Add `isTeam: boolean` field to `results-for-observer.schema.yaml`
- [x] 1.2 Mark field as optional for backward compatibility
- [x] 1.3 Add field description explaining team vs. bot distinction
- [x] 1.4 Validate schema changes with JSON schema tools

## 2. Server Implementation

- [x] 2.1 Update observer result generation to populate `isTeam` field
- [x] 2.2 Fix rank calculation to use competition ranking (1224 style: tied scores share rank, subsequent ranks skip positions)
- [x] 2.3 Add unit tests for team vs. bot result generation
- [x] 2.4 Add unit tests for rank calculation edge cases (tied scores, multiple tie groups, all equal)

## 3. Client Updates

- [x] 3.1 Update GUI observer result display to use `isTeam` field
- [x] 3.2 Add fallback logic for backward compatibility with older servers
- [x] 3.3 Ensure proper visual distinction between team and bot results

## 4. Documentation

- [x] 4.1 Update protocol documentation to mention `isTeam` field
- [x] 4.2 Document ID sign convention as alternative identification method
- [x] 4.3 Reference ADR-0015 for ID namespace separation details
- [x] 4.4 Add examples showing both team and bot results

## 5. Testing

- [x] 5.1 Create an integration test with mixed team/bot battle
- [x] 5.2 Verify observer receives correct `isTeam` values
- [x] 5.3 Verify rank values follow competition ranking (tied scores get same rank)
- [x] 5.4 Test backward compatibility with existing observers
