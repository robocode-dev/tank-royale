## 1. Schema Updates

- [ ] 1.1 Add `isTeam: boolean` field to `results-for-observer.schema.yaml`
- [ ] 1.2 Mark field as optional for backward compatibility
- [ ] 1.3 Add field description explaining team vs. bot distinction
- [ ] 1.4 Validate schema changes with JSON schema tools

## 2. Server Implementation

- [ ] 2.1 Update observer result generation to populate `isTeam` field
- [ ] 2.2 Fix rank calculation algorithm to ensure sequential 1..N values
- [ ] 2.3 Add unit tests for team vs. bot result generation
- [ ] 2.4 Add unit tests for rank calculation edge cases

## 3. Client Updates

- [ ] 3.1 Update GUI observer result display to use `isTeam` field
- [ ] 3.2 Add fallback logic for backward compatibility with older servers
- [ ] 3.3 Ensure proper visual distinction between team and bot results

## 4. Documentation

- [ ] 4.1 Update protocol documentation to mention `isTeam` field
- [ ] 4.2 Document ID sign convention as alternative identification method
- [ ] 4.3 Reference ADR-0015 for ID namespace separation details
- [ ] 4.4 Add examples showing both team and bot results

## 5. Testing

- [ ] 5.1 Create integration test with mixed team/bot battle
- [ ] 5.2 Verify observer receives correct `isTeam` values
- [ ] 5.3 Verify rank values are sequential 1..N
- [ ] 5.4 Test backward compatibility with existing observers
