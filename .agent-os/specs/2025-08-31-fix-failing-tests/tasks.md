# Fix Failing Tests and Complete Implementation

## Tasks

- [ ] 1. Fix Failing Test Issues
  - [ ] 1.1 Analyze all failing tests and identify root causes
  - [ ] 1.2 Fix SettingsCache TTL expiration test timing issues
  - [ ] 1.3 Fix SettingsCache compute if absent test
  - [ ] 1.4 Fix SettingsController get settings by pattern test
  - [ ] 1.5 Fix SettingsChangeListener notifier registration test
  - [ ] 1.6 Verify all tests pass after fixes

- [ ] 2. Complete Missing Integration Components
  - [ ] 2.1 Add missing Spring configuration beans
  - [ ] 2.2 Wire up SettingsChangeNotifier properly with Spring context
  - [ ] 2.3 Configure ObjectMapper bean if missing
  - [ ] 2.4 Add missing service dependencies
  - [ ] 2.5 Verify Spring context loads correctly

- [ ] 3. Implement File Threshold Validator
  - [ ] 3.1 Write tests for FileThresholdValidator
  - [ ] 3.2 Implement FileThresholdValidator using SettingsService
  - [ ] 3.3 Write tests for QueueManager threshold integration
  - [ ] 3.4 Integrate threshold check into QueueManager.enqueueFile()
  - [ ] 3.5 Add threshold exceeded metrics
  - [ ] 3.6 Verify threshold enforcement works correctly

- [ ] 4. Add E2E Tests for Complete Flow
  - [ ] 4.1 Write E2E test for settings propagation across instances
  - [ ] 4.2 Write E2E test for threshold enforcement during scanning
  - [ ] 4.3 Write E2E test for REST API settings management
  - [ ] 4.4 Write E2E test for cache synchronization
  - [ ] 4.5 Verify all E2E tests pass

- [ ] 5. Final Validation and Cleanup
  - [ ] 5.1 Run full test suite and ensure 100% pass rate
  - [ ] 5.2 Run application and verify it starts correctly
  - [ ] 5.3 Test REST endpoints manually with curl/Postman
  - [ ] 5.4 Document any configuration requirements
  - [ ] 5.5 Clean up any TODO comments or unused code