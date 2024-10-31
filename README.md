# COMP3015 Group Project: Battle Joker

## Project Overview
- **Game Concept**: Inspired by 2048 using poker cards.
- **Objective**: Combine cards to create a JOKER card.
- **Multiplayer Goal**: Enhance for online play.

## Project Requirements

### Team Formation
- [x] Form a group of 3 members.

### Grading Breakdown (Total 100 Marks)
- Core Part: Up to 60%
- Additional Features: Up to 25%
- Project Report & Video: Up to 15%

### Core Part Implementation

#### Version 1 (Up to 40%)
- **Server-Client Architecture**:
  - [x] Move card logic to server.
  - [x] Manage database on server.
  - [x] Use SQLite libraries.
- **Client Requirements**:
  - [x] Prompt for name, server IP, and port. //Bugs fixing needed
  - Display and manipulate puzzle.
  - Transmit moves to server.
- **End Game**:
  - Server records and shares scores.
  - Display top 10 scores if needed.

#### Version 2 (Up to 50%)
- Includes all Version 1 requirements.
- **Multiplayer Functionality**:
  - Handle up to 4 players.
  - Allow turns with 4 moves each.
  - Notify players about turn order.
  - Start new game for waiting players.

#### Version 3 (Up to 60%)
- Includes all Version 2 requirements.
- **Concurrent Games**:
  - Support multiple games simultaneously.

### Additional Features (Up to 25%)

#### Suggested Features
- **Multicast Scoreboard**: Display top 10 scores via multicast.
- **Puzzle Saving/Uploading**: Save and upload puzzle data.
- **Cancel Last Action**: Undo last action once per turn.
- **Debugging**: Fix bugs and document them.

### Project Report & Video Demonstration (Up to 15%)
- Complete a project report using the template.
- Record a video demonstration:
  - Show team members.
  - Demonstrate server and client interactions.
  - Use multiple computers for gameplay.

### Submission
- **Program Zip**: Include entire project folder.
- **Video File**: MP4 format, < 500GB.
- **Deadline**: Submit all files before the deadline.
