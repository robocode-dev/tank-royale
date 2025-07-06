"""Ensures that the main modules can be imported correctly."""

import sys
import os
import unittest

# Add current directory and key subdirectories to sys.path to help find the local package
# This is important after 'pip install -e .' and for locally generated schema.
sys.path.insert(0, os.getcwd())
sys.path.insert(0, os.path.join(os.getcwd(), "src"))
sys.path.insert(0, os.path.join(os.getcwd(), "generated"))  # For the schema


class ImportTestCase(unittest.TestCase):
    def test_imports(self):
        """
        This test checks if the necessary modules can be imported.
        """
        print(f"Python sys.path: {sys.path}")
        print(f"Current working directory: {os.getcwd()}")
        print(f"Listing CWD: {os.listdir('.')}")

        if os.path.exists("src"):
            print(f"Listing src: {os.listdir('src')}")
        else:
            print("'src' directory NOT FOUND in CWD")

        if os.path.exists("generated"):
            print(f"Listing generated: {os.listdir('generated')}")
        else:
            print("'generated' directory NOT FOUND in CWD")

        # Check for the specific schema path that setup.py uses
        schema_path_expected_by_setup = (
            "generated/robocode_tank_royale/tank_royale/schema"
        )
        if os.path.exists(schema_path_expected_by_setup):
            print(
                f"Listing {schema_path_expected_by_setup}: {os.listdir(schema_path_expected_by_setup)}"
            )
        else:
            print(f"Schema directory {schema_path_expected_by_setup} NOT FOUND")

        try:
            # Test robocode_tank_royale.schema classes can be imported.
            from robocode_tank_royale.schema import ServerHandshake  # type: ignore

            print("Imported robocode_tank_royale.schema.ServerHandshake successfully.")

            # Test robocode_tank_royale.bot_api modules can be imported.
            from robocode_tank_royale.bot_api.internal import base_bot_internal_data  # type: ignore

            print(
                "Imported robocode_tank_royale.bot_api.internal.base_bot_internal_data successfully."
            )
            from robocode_tank_royale.bot_api.internal import event_queue  # type: ignore

            print(
                "Imported robocode_tank_royale.bot_api.internal.event_queue successfully."
            )
            from robocode_tank_royale.bot_api.internal import websocket_handler  # type: ignore

            print(
                "Imported robocode_tank_royale.bot_api.internal.websocket_handler successfully."
            )
            from robocode_tank_royale.bot_api.internal import base_bot_internals  # type: ignore

            print(
                "Imported robocode_tank_royale.bot_api.internal.base_bot_internals successfully."
            )
            from robocode_tank_royale.bot_api import base_bot  # type: ignore

            print("Imported robocode_tank_royale.bot_api.base_bot successfully.")
            from robocode_tank_royale.bot_api import bot  # type: ignore

            print("Imported robocode_tank_royale.bot_api.bot successfully.")

            # Test robocode_tank_royale.bot_api classes can be imported.
            from robocode_tank_royale.bot_api import Bot  # type: ignore

            print("Imported robocode_tank_royale.bot_api.Bot successfully.")
            from robocode_tank_royale.bot_api import BotABC  # type: ignore

            print("Imported robocode_tank_royale.bot_api.BotABC successfully.")
            from robocode_tank_royale.bot_api import BaseBot  # type: ignore

            print("Imported robocode_tank_royale.bot_api.BaseBot successfully.")
            from robocode_tank_royale.bot_api import BaseBotABC  # type: ignore

            print("Imported robocode_tank_royale.bot_api.BaseBotABC successfully.")

            print("All main refactored modules imported successfully.")

        except ImportError as e:
            print(f"ImportError: {e}")
            import traceback

            traceback.print_exc()
            assert False, f"Failed to import modules: {e}"
        except Exception as e:
            print(f"An unexpected error occurred during import: {e}")
            import traceback

            traceback.print_exc()
            assert False, f"An unexpected error occurred during import: {e}"


if __name__ == "__main__":
    unittest.main()
