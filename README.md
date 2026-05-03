## Assumptions

### 1. Trip Lifecycle
- TAP ON starts a new ACTIVE trip (One ACTIVE trip per PAN)
- TAP OFF completes the latest ACTIVE trip for the same PAN
- Missing TAP OFF results in INCOMPLETE trip

### 2. Cancellation Rule
- TAP ON and TAP OFF at same stop → CANCELLED trip

### 3. Fare Rules
- Fare is based on origin → destination mapping
- INCOMPLETE trips are charged maximum possible fare from origin
- Same stop travel has zero fare

### 4. System Constraints
- In-memory repository only
- Single JVM execution
- UTC timezone assumed for all timestamps (extendable)
- No multi-currency support
- Input CSV is well-formed
		- StopId is always present.
		- Negative duration due to invalid clock skew is permitted.

---

## How to Run

### Build
```bash
mvn clean package
```

### Run Application
```bash
java -jar target/transit-trips.jar input/taps.csv output/trips.csv
```

### Default Paths
- Input: src/main/resources/input/taps.csv
- Output: output/trips.csv

---

## Running Tests

```bash
mvn test
```

---