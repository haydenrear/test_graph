@multi_agent_ide @ui @worktrees @visualization
Feature: Worktree and Computation Graph UI Visualization
  As a UI consumer
  I want to see both the computation graph and worktree hierarchy visualized together
  So that I can understand the relationship between logical tasks and physical code changes

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events
    And a mock UI client is registered to receive events
    And the UI is configured to display both graph and worktree views

  @visualization @core
  Scenario: UI displays parallel graph and worktree hierarchy
    Given a goal with planned WorkNodes creating multiple worktrees
    When the UI is rendered
    Then the left panel should show the computation graph
    And the right panel should show the worktree tree structure
    And both should be synchronized and updated together
    And parent-child relationships should be visually clear in both views

  @visualization @node_worktree_linking
  Scenario: Graph nodes are linked visually to their worktrees
    Given multiple WorkNodes and their associated worktrees
    When a user hovers or clicks on a node in the graph
    Then the corresponding worktree should be highlighted in the worktree view
    And vice versa: clicking a worktree highlights its node in the graph
    And a tooltip or indicator should show the relationship
    And the link should persist as state changes

  @visualization @double_click_detail
  Scenario: Double-clicking a node opens file detail view
    Given a WorkNode in the computation graph
    When the node is double-clicked
    Then the detail panel should replace the graph view
    And the file browser should show the worktree contents
    And modified files should be highlighted
    And a unified diff view should display changes alongside file tree
    And the view should show file-by-file diffs with syntax highlighting

  @visualization @back_navigation
  Scenario: Back command returns to graph view
    Given a user is in file detail view
    When the back button or command is selected
    Then the detail view should close
    And the computation graph should be displayed again
    And graph state should be preserved (scrolling position, selected nodes)
    And the transition should be smooth and animated

  @visualization @splittable_windows
  Scenario: Detail view can be split to show multiple nodes
    Given a user is viewing file details for node A
    When a split command is executed (e.g., split vertical or horizontal)
    Then the view should split into two panes
    And pane 1 should continue showing node A's files
    And pane 2 should allow selection of a different node (B)
    And both panes should show independent file details and diffs
    And the split ratio should be adjustable by dragging the divider

  @visualization @multi_pane_independent
  Scenario: Multiple panes can show different nodes independently
    Given a split view with 3 panes showing nodes A, B, and C
    When scrolling in pane A
    Then only pane A should scroll
    And panes B and C should remain at their current positions
    And each pane should have its own file selection and diff view
    And closing one pane should not affect the others

  @visualization @shared_context_menu
  Scenario: Context menu provides reusable actions across panes
    Given a file detail pane is displayed
    When a right-click context menu is opened
    Then the menu should include:
      | option              | description                              |
      | Send Message        | Open dialog to send message to agent     |
      | Interrupt Agent     | Send interrupt signal                    |
      | Approve Changes     | If in review state                       |
      | Reject Changes      | If in review state with options          |
      | Branch Node         | Create alternative branch                |
      | Edit Prompt         | Modify the node prompt                   |
      | View Full Diff      | Expand diff view to full screen          |
      | Copy Changes        | Copy diff to clipboard                   |
      | Close Pane          | Close this detail pane                   |
    And this menu should be consistent across all panes

  @visualization @status_color_indicators
  Scenario Outline: Node colors indicate current status
    Given a WorkNode with status "<status>"
    When the node is rendered in the graph
    Then the node should be colored "<color>"
    And the color should be consistent across graph and worktree tree views
    And the color should update immediately when status changes

    Examples:
      | status              | color           |
      | READY               | Blue            |
      | RUNNING             | Green           |
      | WAITING_REVIEW      | Orange          |
      | WAITING_INPUT       | Red             |
      | COMPLETED           | Gray (darker)   |
      | FAILED              | Dark Red        |
      | PRUNED              | Light Gray      |

  @visualization @human_input_red
  Scenario: Nodes requiring human input are highlighted in red
    Given a WorkNode transitions to WAITING_INPUT state
    And human review is required (for approval, conflict resolution, etc.)
    When the node is rendered
    Then the node should be displayed in red
    And the color should persist until human action is taken
    And a badge or icon should indicate "action required"
    And the node should stand out visually among other nodes

  @visualization @human_input_menu
  Scenario: Red nodes show context-specific menu on right-click
    Given a red node in WAITING_INPUT state with specific reason
    When the node is right-clicked
    Then a context menu should appear with options specific to the reason:
      | reason              | menu_options                           |
      | Review              | Done Reviewing, Approve, Reject        |
      | Merge Conflict      | Auto-Merge, Manual Resolution, Revert  |
      | User Edit           | Continue, Discard Changes, Restart     |
      | Interruption        | Resume, Abandon, Modify and Resume     |

  @visualization @approve_reject_options
  Scenario: Review state menu includes approval decisions
    Given a node in WAITING_REVIEW state (approval required)
    When the context menu is opened
    Then the menu should show:
      | option              | action                                      |
      | Done Reviewing      | Close review pane, mark as COMPLETED        |
      | Approve             | Send approval message, transition to COMPLETED |
      | Approve and Merge   | Approve and auto-merge if applicable        |
      | Reject              | Open rejection dialog with feedback options |
      | Request Changes     | Keep in review but request modifications    |
    And each option should send appropriate message to backend

  @visualization @merge_conflict_menu
  Scenario: Merge conflict menu provides resolution options
    Given a node in WAITING_INPUT state due to merge conflict
    And the file diff shows conflicted sections
    When the context menu is opened
    Then the menu should show:
      | option                | action                              |
      | Auto-Merge (ours)     | Keep parent worktree changes        |
      | Auto-Merge (theirs)   | Keep child worktree changes         |
      | Manual Resolution     | Open conflict editor inline         |
      | Mark Resolved         | After manual resolution is done     |
      | Revert Merge          | Cancel merge, return to pre-merge   |
      | Three-Way View        | Show base/parent/child side-by-side |

  @visualization @interrupt_menu
  Scenario: Running nodes show interrupt option in menu
    Given a WorkNode in RUNNING state
    When the context menu is opened
    Then the menu should show:
      | option               | action                               |
      | Interrupt Agent      | Send interrupt signal, go to WAITING_INPUT |
      | View Streaming       | Show real-time streaming output      |
      | Edit Prompt          | Pause and edit prompt for next run   |
      | View Full Diff       | Show accumulated diff so far         |
    And "Interrupt Agent" should be prominent/highlighted

  @visualization @send_message_dialog
  Scenario: Send message dialog is available from any context menu
    Given any node is displayed in detail view
    When "Send Message" is selected from context menu
    Then a message dialog should open with:
      | field              | content                              |
      | To                 | Node ID (auto-filled)                |
      | Message Type       | Dropdown (interrupt, edit, approve, etc) |
      | Content            | Text area for custom message         |
      | Buttons            | Send, Cancel                         |
    And the message should be sent via the standard messaging layer

  @visualization @double_click_context
  Scenario: Double-clicking node is equivalent to opening context menu
    Given a WorkNode in the detail view
    When the node is double-clicked
    Then the context menu should appear (equivalent to right-click)
    And for nodes in WAITING_INPUT, the action menu should open
    And for other nodes, the standard context menu should open
    And double-click should be convenient for keyboard accessibility

  @visualization @worktree_details
  Scenario: Worktree details are displayable in a detail panel
    Given a worktree selected in the UI
    When the worktree is clicked or selected
    Then a detail panel should appear showing:
      | field              | example                                |
      | Worktree ID        | work-abc123                            |
      | Path               | /repo/.worktrees/work-abc123           |
      | Parent ID          | work-parent                            |
      | Status             | ACTIVE                                 |
      | Base Branch        | main                                   |
      | Working Branch     | work-abc123                            |
      | Files Changed      | 3 modified, 1 new                      |
      | Last Commit        | a1b2c3d by system, 5 minutes ago       |
    And the details should update in real-time as the worktree changes

  @visualization @file_browser
  Scenario: File browser shows worktree contents with change indicators
    Given a worktree with modified files
    When the file browser is opened for that worktree
    Then the directory structure should be displayed
    And modified files should be highlighted with "M" indicator
    And new files should be indicated with "+" symbol
    And deleted files should show with "-" symbol
    And clicking a file should show its diff

  @visualization @file_diff_syntax_highlight
  Scenario: File diffs display with syntax highlighting
    Given a file is selected in the file browser
    When the diff view is displayed
    Then the code should be syntax-highlighted based on language
    And added lines should be green-highlighted
    And removed lines should be red-highlighted
    And line numbers should be shown on both sides
    And the diff should be navigable (jump to next change, etc.)

  @visualization @streaming_panel
  Scenario: Streaming output appears in node detail panel
    Given a WorkNode streaming code generation
    When NodeStreamDeltaEvent is received
    Then the node detail panel should show streaming output incrementally
    And tokens should appear in real-time as they arrive
    And syntax highlighting should apply to code being generated
    And the panel should scroll to show new content
    And a "Copy Output" button should allow copying the streamed content

  @visualization @diff_view
  Scenario: Diff view shows parent vs child worktree changes
    Given a WorkNode with completed work
    When the diff view is opened
    Then the changes compared to parent worktree should be displayed
    And file-by-file diffs should be viewable with tabs or list
    And +/- lines should be color-coded (green/red)
    And line numbers should allow navigation
    And the diff should match the actual git diff
    And a "View Full Diff" option should show raw unified diff

  @visualization @diff_action_buttons
  Scenario: Diff view includes action buttons for review
    Given a diff is displayed in detail view
    Then buttons should appear for:
      | button              | action                              |
      | Approve             | Send approval message               |
      | Request Changes     | Send change request                 |
      | Reject              | Send rejection with reason          |
      | Branch              | Create alternative branch           |
      | Interrupt           | Stop current execution              |
      | View Comments       | Show annotations/comments           |
      | Add Comment         | Add annotation to specific line     |

  @visualization @merge_preview
  Scenario: Merge conflicts are visualized before resolution
    Given a merge operation with conflicts detected
    When the conflict view is displayed
    Then conflicting files should be highlighted
    And for each conflict, a three-way merge view should show:
      | section        | content                   |
      | Base (ancestor)| Original common content   |
      | Parent         | Parent worktree version   |
      | Child          | Child worktree version    |
    And the user can resolve conflicts in the UI
    And inline conflict buttons should allow quick resolution

  @visualization @tree_expansion
  Scenario: Worktree tree view supports expansion/collapse
    Given a deep worktree hierarchy
    When the tree view is displayed
    Then parent worktrees should be expandable
    And clicking expand should show child worktrees
    And collapsed nodes should show a count of children
    And the expand/collapse state should persist during the session

  @visualization @event_driven_updates
  Scenario: UI updates synchronously with events
    Given a UI subscribed to all events
    When a NodeAddedEvent is received
    Then the new node should appear in the computation graph immediately
    And when a WorktreeCreatedEvent is received, the worktree should appear in the tree
    And when a WorktreeMergedEvent is received, the worktree should be marked as MERGED
    And all updates should be animated smoothly

  @visualization @status_indicators
  Scenario: Visual indicators show node and worktree status
    Given nodes and worktrees in various states
    When they are rendered
    Then each should display its status with color/icon:
      | status              | indicator        |
      | READY               | Blue circle      |
      | RUNNING             | Animated spinner |
      | WAITING_REVIEW      | Orange flag      |
      | WAITING_INPUT       | Red circle       |
      | COMPLETED           | Green checkmark  |
      | FAILED              | Red X            |
      | PRUNED              | Grayed out       |
    And tooltips should explain the status on hover

  @visualization @branch_visualization
  Scenario: Branching creates distinct visual branches
    Given a node with two branches
    When both branches are created
    Then the graph should display a branch point
    And the original and both branches should be visually distinct
    And lines connecting nodes should show hierarchy
    And merged branches should show merge point in the graph

  @visualization @timeline_view
  Scenario: Timeline view shows execution sequence
    Given a completed goal with ordered node execution
    When the timeline view is requested
    Then nodes should be displayed in chronological order
    And the timing of each node should be visible
    And parallel nodes should be shown side-by-side
    And execution duration should be indicated

  @visualization @graph_export
  Scenario: Graph and worktree visualization can be exported
    Given a complete computation graph with worktrees
    When an export is requested
    Then a static image or SVG should be generated
    And it should show both the graph and worktree hierarchy
    And all node statuses should be preserved
    And the export should be suitable for documentation or sharing

  @visualization @zoom_pan
  Scenario: UI supports zooming and panning for large graphs
    Given a complex computation graph with many nodes
    When the user zooms in/out or pans
    Then the graph should remain interactive and responsive
    And node details should still be readable at appropriate zoom levels
    And pan should be smooth and performant
    And zoom level should affect both graph and worktree views appropriately

  @visualization @search_navigation
  Scenario: Users can search and navigate to specific nodes
    Given a large computation graph
    When a search query is entered (e.g., "authentication")
    Then matching nodes should be highlighted
    And clicking a match should navigate to and center that node
    And the search should work across node descriptions and prompts
    And search results should include both graph nodes and worktree IDs

  @visualization @sync_responsiveness
  Scenario: UI remains responsive during concurrent updates
    Given multiple nodes executing in parallel
    When NodeStreamDeltaEvent, NodeStatusChangedEvent, and WorktreeCreatedEvent are all arriving
    Then the UI should not freeze or become unresponsive
    And all updates should be applied smoothly
    And no events should be lost or duplicate
    And user interactions should remain responsive

  @visualization @pane_close_behavior
  Scenario: Closing a pane preserves graph state
    Given a split view with 3 panes showing detail views
    When a pane is closed
    Then the remaining panes should adjust layout
    And the computation graph in the background should be preserved
    And returning to graph view should show same scroll/selection state
    And all open panes should be closable without affecting others

  @visualization @keyboard_shortcuts
  Scenario: Keyboard shortcuts provide quick navigation
    Given any view is displayed
    When keyboard shortcuts are used
    Then the following should work:
      | shortcut | action                           |
      | Esc      | Close detail view, return to graph |
      | Ctrl+F   | Open search dialog               |
      | Ctrl+/   | Toggle context menu              |
      | Tab      | Cycle between panes              |
      | Ctrl+|   | Split pane vertically            |
      | Ctrl+-   | Split pane horizontally          |
    And shortcuts should be documented in UI help

  @visualization @persistence
  Scenario: UI layout and selections persist across sessions
    Given a user configured multiple panes with specific selections
    When the session is closed and reopened
    Then the pane layout should be restored
    And the selected nodes should still be displayed
    And the graph view state should be preserved
    And scroll positions and zoom levels should be restored
