"""
Tests for the orchestrator module.
"""

import sys
import unittest

sys.path.insert(0, "../src")

from core.orchestrator import Orchestrator


class MockAgent:
    """Mock agent for testing."""

    def execute(self, task):
        """Execute a task."""
        return {"agent_result": task.get("id", "unknown")}


class TestOrchestrator(unittest.TestCase):
    """Test cases for Orchestrator class."""

    def setUp(self):
        """Set up test fixtures."""
        self.orchestrator = Orchestrator()
        self.mock_agent = MockAgent()
        self.orchestrator.register_agent("test-agent", self.mock_agent)

    def test_orchestrator_initialization(self):
        """Test orchestrator initialization."""
        self.assertEqual(len(self.orchestrator.agents), 1)
        self.assertIn("test-agent", self.orchestrator.agents)

    def test_register_agent(self):
        """Test agent registration."""
        new_agent = MockAgent()
        self.orchestrator.register_agent("new-agent", new_agent)
        self.assertEqual(len(self.orchestrator.agents), 2)
        self.assertIn("new-agent", self.orchestrator.agents)

    def test_workflow_execution(self):
        """Test workflow execution."""
        tasks = [
            {"id": "task-1", "type": "code_generation", "agent": "test-agent"},
            {"id": "task-2", "type": "testing", "agent": "test-agent"},
        ]

        results = self.orchestrator.execute_workflow("workflow-1", tasks)

        self.assertEqual(len(results), 2)
        status = self.orchestrator.get_workflow_status("workflow-1")
        self.assertEqual(status["status"], "completed")
        self.assertEqual(status["tasks_completed"], 2)


if __name__ == "__main__":
    unittest.main()
