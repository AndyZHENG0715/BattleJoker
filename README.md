# COMP3015 Group Project: Battle Joker

<details>
<summary>Project Overview</summary>

- **Game Concept**: Inspired by 2048 using poker cards.
- **Objective**: Combine cards to create a JOKER card.
- **Multiplayer Goal**: Enhance for online play.

</details>

## Project Requirements

### Team Formation
- [x] Form a group of 3 members.

### Grading Breakdown (Total 100 Marks)
- [x] Core Part: Up to 60%
- [x] Additional Features: Up to 25%
- [ ] Project Report & Video: Up to 15%

### Core Part Implementation

#### Version 1 (Up to 40%)

- **Server-Client Architecture**:
  - [x] Move card logic to server.
  - [x] Manage database on server.
  - [x] Use SQLite libraries.
- **Client Requirements**:
  - [x] Prompt for name, server IP, and port.
  - [x] Display and manipulate puzzle.
  - [x] Transmit moves to server.
- **End Game**:
  - [x] Server records and shares scores.
  - [x] Display top 10 scores if needed.

#### Version 2 (Up to 50%)

- [x] Includes all Version 1 requirements.
- **Multiplayer Functionality**:
  - [x] Handle up to 4 players.
  - [x] Allow turns with 4 moves each.
  - [ ] Notify players about turn order.
  - [x] Start new game for waiting players.

#### Version 3 (Up to 60%)

- [ ] Includes all Version 2 requirements.
- **Concurrent Games**:
  - [x] Support multiple games simultaneously.

### Additional Features (Up to 25%)

#### Suggested Features
- [x] **Multicast Scoreboard**: Display top 10 scores via multicast.
- [x] **Puzzle Saving/Uploading**: Save and upload puzzle data.
- [x] **Cancel Last Action**: Undo last action once per turn.
- [ ] **Debugging**: Fix bugs and document them.

### Project Report & Video Demonstration (Up to 15%)

- [ ] Complete a project report using the template.
- [ ] Record a video demonstration:
  - [ ] Show team members.
  - [ ] Demonstrate server and client interactions.
  - [ ] Use multiple computers for gameplay.

### Submission
- **Program Zip**: Include entire project folder.
- **Video File**: MP4 format, < 500GB.
- **Deadline**: Submit all files before the deadline.